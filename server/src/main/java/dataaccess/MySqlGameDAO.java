package dataaccess;

import model.GameData;
import com.google.gson.Gson;
import chess.ChessGame;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class MySqlGameDAO {

    public int insertGame(GameData game) throws DataAccessException {
        String sql = "INSERT INTO games (whiteUsername, blackUsername, gameName, game, gameOver) VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, game.whiteUsername());
            stmt.setString(2, game.blackUsername());
            stmt.setString(3, game.gameName());
            stmt.setString(4, new Gson().toJson(game.game()));
            stmt.setBoolean(5, game.gameOver());

            stmt.executeUpdate();

            ResultSet rs = stmt.getGeneratedKeys();
            if (rs.next()) {
                return rs.getInt(1); // return generated gameID
            } else {
                throw new DataAccessException("Unable to retrieve generated game ID");
            }
        } catch (SQLException e) {
            throw new DataAccessException("Unable to insert game", e);
        }
    }

    public GameData findGame(int gameID) throws DataAccessException {
        String sql = "SELECT * FROM games WHERE gameID = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, gameID);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                String jsonGame = rs.getString("game");
                ChessGame chessGame = new Gson().fromJson(jsonGame, ChessGame.class);
                boolean gameOver = rs.getBoolean("gameOver");

                return new GameData(
                        rs.getInt("gameID"),
                        rs.getString("whiteUsername"),
                        rs.getString("blackUsername"),
                        rs.getString("gameName"),
                        chessGame,
                        gameOver
                );
            }
            return null;
        } catch (SQLException e) {
            throw new DataAccessException("Unable to find game", e);
        }
    }

    public void updateGame(GameData game) throws DataAccessException {
        String sql = "UPDATE games SET gameName=?, whiteUsername=?, blackUsername=?, game=?, gameOver=? WHERE gameID=?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, game.gameName());
            stmt.setString(2, game.whiteUsername());
            stmt.setString(3, game.blackUsername());
            stmt.setString(4, new Gson().toJson(game.game()));
            stmt.setBoolean(5, game.gameOver());
            stmt.setInt(6, game.gameID());

            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("Unable to update game", e);
        }
    }

    public List<GameData> listGames() throws DataAccessException {
        List<GameData> games = new ArrayList<>();
        String sql = "SELECT * FROM games";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                games.add(new GameData(
                        rs.getInt("gameID"),
                        rs.getString("whiteUsername"),
                        rs.getString("blackUsername"),
                        rs.getString("gameName"),
                        new Gson().fromJson(rs.getString("game"), ChessGame.class),
                        rs.getBoolean("gameOver")
                ));
            }
        } catch (SQLException e) {
            throw new DataAccessException("Unable to list games", e);
        }
        return games;
    }

    public void clear() throws DataAccessException {
        try (Connection conn = DatabaseManager.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.executeUpdate("DELETE FROM games");
        } catch (SQLException e) {
            throw new DataAccessException("Unable to clear games", e);
        }
    }

    public void setWhiteUsername(int gameID, String username) throws DataAccessException {
        String sql = "UPDATE games SET whiteUsername = ? WHERE gameID = ?";
        try (var conn = DatabaseManager.getConnection();
             var stmt = conn.prepareStatement(sql)) {
            if (username == null) {
                stmt.setNull(1, java.sql.Types.VARCHAR);
            } else {
                stmt.setString(1, username);
            }
            stmt.setInt(2, gameID);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("Failed to set whiteUsername", e);
        }
    }

    public void setBlackUsername(int gameID, String username) throws DataAccessException {
        String sql = "UPDATE games SET blackUsername = ? WHERE gameID = ?";
        try (var conn = DatabaseManager.getConnection();
             var stmt = conn.prepareStatement(sql)) {
            if (username == null) {
                stmt.setNull(1, java.sql.Types.VARCHAR);
            } else {
                stmt.setString(1, username);
            }
            stmt.setInt(2, gameID);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("Failed to set blackUsername", e);
        }
    }

}

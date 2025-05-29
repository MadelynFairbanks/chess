package dataaccess;

import model.GameData;
import com.google.gson.Gson;
import chess.ChessGame;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class MySqlGameDAO {

    public void insertGame(GameData game) throws DataAccessException {
        String sql = "INSERT INTO games (gameID, gameName, whiteUsername, blackUsername, game) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, game.gameID());
            stmt.setString(2, game.gameName());
            stmt.setString(3, game.whiteUsername());
            stmt.setString(4, game.blackUsername());
            stmt.setString(5, new Gson().toJson(game.game()));
            stmt.executeUpdate();
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

                return new GameData(
                        rs.getInt("gameID"),
                        rs.getString("whiteUsername"),
                        rs.getString("blackUsername"),
                        rs.getString("gameName"),
                        chessGame
                );
            }
            return null;
        } catch (SQLException e) {
            throw new DataAccessException("Unable to find game", e);
        }
    }


    public void updateGame(GameData game) throws DataAccessException {
        String sql = "UPDATE games SET gameName=?, whiteUsername=?, blackUsername=?, game=? WHERE gameID=?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, game.gameName());
            stmt.setString(2, game.whiteUsername());
            stmt.setString(3, game.blackUsername());
            stmt.setString(4, new Gson().toJson(game.game()));
            stmt.setInt(5, game.gameID());
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
                        rs.getString("whiteUsername"),       // ✅ Correct order
                        rs.getString("blackUsername"),
                        rs.getString("gameName"),
                        new Gson().fromJson(rs.getString("game"), ChessGame.class)
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
}

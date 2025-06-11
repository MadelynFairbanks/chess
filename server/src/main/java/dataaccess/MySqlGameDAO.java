package dataaccess;

import model.GameData;
import com.google.gson.Gson;
import chess.ChessGame;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class MySqlGameDAO {

    // Shared helper to update either white or black username
    private void setUsernameField(int gameID, String username, String columnName) throws DataAccessException {
        String sql = "UPDATE games SET " + columnName + " = ? WHERE gameID = ?";
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
            throw new DataAccessException("Failed to set " + columnName, e);
        }
    }

    // Add a new game to the DB and return its auto-generated ID
    public int insertGame(GameData game) throws DataAccessException {
        String sql = "INSERT INTO games (whiteUsername, blackUsername, gameName, game, gameOver) VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, game.whiteUsername());
            stmt.setString(2, game.blackUsername());
            stmt.setString(3, game.gameName());

            String gameJson = game.game() == null
                    ? new Gson().toJson(new ChessGame())
                    : new Gson().toJson(game.game());
            stmt.setString(4, gameJson);

            stmt.setBoolean(5, game.gameOver());

            stmt.executeUpdate();
            ResultSet rs = stmt.getGeneratedKeys();
            if (rs.next()) {
                int id = rs.getInt(1);

                // ✅ This is the key part: update the original game object with the generated ID
                // (You can either mutate it if it's mutable, or log this if not used downstream)
                System.out.println("🟢 Inserted game with ID: " + id);
                return id;
            } else {
                throw new DataAccessException("Unable to retrieve generated game ID");
            }
        } catch (SQLException e) {
            throw new DataAccessException("Unable to insert game", e);
        }
    }




    // Get one specific game using its ID
    public GameData findGame(int gameID) throws DataAccessException {
        String sql = "SELECT * FROM games WHERE gameID = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, gameID);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                // Convert the stored JSON game string back into a ChessGame object
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
            return null; // Game not found
        } catch (SQLException e) {
            throw new DataAccessException("Unable to find game", e);
        }
    }

    // Replace an existing game's data with new stuff
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

    // Grab a list of *all* the games from the DB
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

    // For nuking all the games from the DB
    public void clear() throws DataAccessException {
        try (Connection conn = DatabaseManager.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.executeUpdate("DELETE FROM games");
        } catch (SQLException e) {
            throw new DataAccessException("Unable to clear games", e);
        }
    }

    // Set the white player for a game (can be null to unassign)
    public void setWhiteUsername(int gameID, String username) throws DataAccessException {
        setUsernameField(gameID, username, "whiteUsername");
    }

    // Same thing but for black player
    public void setBlackUsername(int gameID, String username) throws DataAccessException {
        setUsernameField(gameID, username, "blackUsername");
    }


}

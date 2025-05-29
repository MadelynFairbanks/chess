package dataaccess;

import com.google.gson.Gson;
import model.GameData;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class MySqlGameDAO {
    private final Gson gson = new Gson();

    public void insertGame(GameData game) throws DataAccessException {
        String sql = "INSERT INTO games (gameID, gameName, whiteUsername, blackUsername, game) VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, game.gameID());
            stmt.setString(2, game.gameName());
            stmt.setString(3, game.whiteUsername());
            stmt.setString(4, game.blackUsername());
            stmt.setString(5, gson.toJson(game.game()));

            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("Unable to insert game", e);
        }
    }

    public void clear() throws DataAccessException {
        String sql = "DELETE FROM games";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("Unable to clear games", e);
        }
    }

    public GameData findGame(int gameID) throws DataAccessException {
        String sql = "SELECT * FROM games WHERE gameID = ?";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, gameID);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                String gameName = rs.getString("gameName");
                String whiteUsername = rs.getString("whiteUsername");
                String blackUsername = rs.getString("blackUsername");
                String gameJson = rs.getString("game");

                Object gameObject = gson.fromJson(gameJson, Object.class); // Replace Object if using a custom game model
                return new GameData(gameID, whiteUsername, blackUsername, gameName, gameObject);
            } else {
                return null;
            }
        } catch (SQLException e) {
            throw new DataAccessException("Unable to find game", e);
        }
    }

    public void updateGame(GameData game) throws DataAccessException {
        String sql = "UPDATE games SET whiteUsername = ?, blackUsername = ?, game = ? WHERE gameID = ?";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, game.whiteUsername());
            stmt.setString(2, game.blackUsername());
            stmt.setString(3, gson.toJson(game.game()));
            stmt.setInt(4, game.gameID());

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
                int gameID = rs.getInt("gameID");
                String gameName = rs.getString("gameName");
                String whiteUsername = rs.getString("whiteUsername");
                String blackUsername = rs.getString("blackUsername");
                String gameJson = rs.getString("game");

                Object gameObject = gson.fromJson(gameJson, Object.class); // Replace Object if needed
                games.add(new GameData(gameID, whiteUsername, blackUsername, gameName, gameObject));
            }

        } catch (SQLException e) {
            throw new DataAccessException("Unable to list games", e);
        }

        return games;
    }
}

package dataaccess;

import model.AuthData;
import dataaccess.DataAccessException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class MySqlAuthTokenDAO {

    public void insertAuth(AuthData auth) throws DataAccessException {
        insertHelper(auth);
    }

    private static void insertHelper(AuthData auth) throws DataAccessException {
        String sql = "INSERT INTO auth_tokens (token, username) VALUES (?, ?)";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, auth.authToken());
            stmt.setString(2, auth.username());
            stmt.executeUpdate();

        } catch (SQLException e) {
            throw new DataAccessException("Unable to insert auth token", e);
        }
    }

    public void clear() throws DataAccessException {
        String sql = "DELETE FROM auth_tokens";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("Unable to clear auth tokens", e);
        }
    }

    public AuthData findAuth(String token) throws DataAccessException {
        return getAuthData(token);      // Notice: calls this class’s own helper
    }

    public static AuthData getAuthData(String token) throws DataAccessException {
        String sql = "SELECT * FROM auth_tokens WHERE token = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, token);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                String username = rs.getString("username");
                return new AuthData(token, username);
            } else {
                return null;
            }
        } catch (SQLException e) {
            throw new DataAccessException("Unable to find auth token", e);
        }
    }

    public void deleteAuth(String token) throws DataAccessException {
        String sql = "DELETE FROM auth_tokens WHERE token = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, token);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("Unable to delete auth token", e);
        }
    }
}

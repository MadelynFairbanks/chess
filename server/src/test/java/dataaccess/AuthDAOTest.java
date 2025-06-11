package dataaccess;

import model.AuthData;
import model.UserData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import static org.junit.jupiter.api.Assertions.*;

public class AuthDAOTest {

    private MySqlAuthDAO authDAO;

    @BeforeEach
    public void setup() throws DataAccessException {
        // Create database and tables
        DatabaseManager.configureDatabase();

        // Clear tables in correct order (auth_tokens depends on users)
        try (Connection conn = DatabaseManager.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.executeUpdate("DELETE FROM auth_tokens");
            stmt.executeUpdate("DELETE FROM users");
        } catch (SQLException e) {
            throw new DataAccessException("Failed to clear tables", e);
        }

        // Insert users
        MySqlUserDAO userDAO = new MySqlUserDAO();
        userDAO.insertUser(new UserData("alice", "alice@gmail.com", "password123"));
        userDAO.insertUser(new UserData("bob", "bob@gmail.com", "password123"));
        userDAO.insertUser(new UserData("carol", "carol@gmail.com", "password123"));
        userDAO.insertUser(new UserData("dave", "dave@gmail.com", "password123"));

        // Debug: print inserted users
        try (Connection conn = DatabaseManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM users")) {
            while (rs.next()) {
                System.out.println("User in DB: " + rs.getString("username"));
            }
        } catch (SQLException e) {
            throw new DataAccessException("Failed to print debug users", e);
        }

        authDAO = new MySqlAuthDAO();
    }

    @Test
    public void insertAuthSuccess() throws DataAccessException {
        AuthData auth = new AuthData("token123", "alice");
        authDAO.insertAuth(auth);

        AuthData retrieved = authDAO.findAuth("token123");
        assertNotNull(retrieved);
        assertEquals("alice", retrieved.username());
    }

    @Test
    public void insertAuthDuplicateToken() throws DataAccessException {
        AuthData auth1 = new AuthData("dupeToken", "bob");
        AuthData auth2 = new AuthData("dupeToken", "carol");

        authDAO.insertAuth(auth1);
        assertThrows(DataAccessException.class, () -> authDAO.insertAuth(auth2));
    }

    @Test
    public void findAuthNotFound() throws DataAccessException {
        AuthData result = authDAO.findAuth("nonexistent");
        assertNull(result);
    }

    @Test
    public void deleteAuthSuccess() throws DataAccessException {
        AuthData auth = new AuthData("toDelete", "dave");
        authDAO.insertAuth(auth);

        authDAO.deleteAuth("toDelete");
        assertNull(authDAO.findAuth("toDelete"));
    }

    @Test
    public void deleteAuthNoEffectIfMissing() throws DataAccessException {
        // Should not throw, even if token doesn’t exist
        assertDoesNotThrow(() -> authDAO.deleteAuth("missingToken"));
    }
}

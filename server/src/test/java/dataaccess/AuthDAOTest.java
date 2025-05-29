package dataaccess;

import model.AuthData;
import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;

public class AuthDAOTest {

    private MySqlAuthDAO authDAO;

    @BeforeEach
    public void setup() throws DataAccessException {
        DatabaseManager.createDatabase();
        DatabaseManager.createTables();
        authDAO = new MySqlAuthDAO();
    }

    @Test
    public void insertAuth_success() throws DataAccessException {
        AuthData auth = new AuthData("token123", "alice");
        authDAO.insertAuth(auth);

        AuthData retrieved = authDAO.findAuth("token123");
        assertNotNull(retrieved);
        assertEquals("alice", retrieved.username());
    }

    @Test
    public void insertAuth_duplicateToken() throws DataAccessException {
        AuthData auth1 = new AuthData("dupeToken", "bob");
        AuthData auth2 = new AuthData("dupeToken", "carol");

        authDAO.insertAuth(auth1);
        assertThrows(DataAccessException.class, () -> authDAO.insertAuth(auth2));
    }

    @Test
    public void findAuth_notFound() throws DataAccessException {
        AuthData result = authDAO.findAuth("nonexistent");
        assertNull(result);
    }

    @Test
    public void deleteAuth_success() throws DataAccessException {
        AuthData auth = new AuthData("toDelete", "dave");
        authDAO.insertAuth(auth);

        authDAO.deleteAuth("toDelete");
        assertNull(authDAO.findAuth("toDelete"));
    }

    @Test
    public void deleteAuth_noEffectIfMissing() throws DataAccessException {
        // Should not throw, even if token doesn’t exist
        assertDoesNotThrow(() -> authDAO.deleteAuth("missingToken"));
    }
}

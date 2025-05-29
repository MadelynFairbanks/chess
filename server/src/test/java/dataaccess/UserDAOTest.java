package dataaccess;

import model.UserData;
import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;

public class UserDAOTest {

    private MySqlUserDAO userDAO;

    @BeforeEach
    public void setup() throws DataAccessException {
        DatabaseManager.createDatabase();
        DatabaseManager.createTables();
        userDAO = new MySqlUserDAO();
    }

    @Test
    public void insertUser_success() throws DataAccessException {
        UserData user = new UserData("alice", "password123", "alice@example.com");
        userDAO.insertUser(user);

        UserData retrieved = userDAO.findUser("alice");

        assertNotNull(retrieved);
        assertEquals("alice", retrieved.username());
        assertEquals("alice@example.com", retrieved.email());
        assertNotEquals("password123", retrieved.password()); // password should be hashed
    }

    @Test
    public void verifyPassword_success() throws DataAccessException {
        UserData user = new UserData("bob", "mySecret!", "bob@example.com");
        userDAO.insertUser(user);

        assertTrue(userDAO.verifyPassword("bob", "mySecret!"));
    }

    @Test
    public void verifyPassword_failure() throws DataAccessException {
        UserData user = new UserData("carol", "carolPass", "carol@example.com");
        userDAO.insertUser(user);

        assertFalse(userDAO.verifyPassword("carol", "wrongPass"));
    }

    @Test
    public void insertUser_duplicateUsername() throws DataAccessException {
        UserData user1 = new UserData("dave", "one", "dave@one.com");
        UserData user2 = new UserData("dave", "two", "dave@two.com");

        userDAO.insertUser(user1);
        assertThrows(DataAccessException.class, () -> userDAO.insertUser(user2));
    }
}

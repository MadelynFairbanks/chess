package passoff.server;

import dataaccess.MemoryDataAccess;
import dataaccess.DataAccessException;
import model.AuthData;
import model.UserData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import service.UserService;

import static org.junit.jupiter.api.Assertions.*;

public class UserServiceTest {
    private UserService userService;

    @BeforeEach
    public void setup() {
        userService = new UserService(new MemoryDataAccess());
    }

    @Test
    public void register_positive() throws DataAccessException {
        UserData user = new UserData("alice", "password", "alice@email.com");
        AuthData auth = userService.register(user);
        assertEquals("alice", auth.username());
        assertNotNull(auth.authToken());
    }

    @Test
    public void register_negative_duplicateUsername() throws DataAccessException {
        UserData user = new UserData("bob", "pass", "bob@email.com");
        userService.register(user);
        assertThrows(DataAccessException.class, () -> userService.register(user));
    }

    @Test
    public void login_positive() throws DataAccessException {
        UserData user = new UserData("carol", "pw123", "c@email.com");
        userService.register(user);
        AuthData auth = userService.login(user);
        assertEquals("carol", auth.username());
    }

    @Test
    public void login_negative_wrongPassword() throws DataAccessException {
        UserData user = new UserData("dave", "1234", "d@email.com");
        userService.register(user);
        UserData wrong = new UserData("dave", "wrong", "d@email.com");
        assertThrows(DataAccessException.class, () -> userService.login(wrong));
    }

    @Test
    public void logout_positive() throws DataAccessException {
        UserData user = new UserData("john", "secret", "john@example.com");
        AuthData auth = userService.register(user);
        assertDoesNotThrow(() -> userService.logout(auth.authToken()));
    }

    @Test
    public void logout_negative_invalidToken() {
        assertThrows(DataAccessException.class, () -> userService.logout("fake-token"));
    }
}

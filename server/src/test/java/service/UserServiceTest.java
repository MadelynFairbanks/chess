package service;

import dataaccess.MemoryDataAccess;
import dataaccess.DataAccessException;
import model.AuthData;
import model.UserData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class UserServiceTest {
    private UserService userService;

    @BeforeEach
    public void setup() {
        userService = new UserService(new MemoryDataAccess());
    }

    @Test
    public void registerPositive() throws DataAccessException {
        UserData user = new UserData("alice", "password", "alice@email.com");
        AuthData auth = userService.register(user);
        assertEquals("alice", auth.username());
        assertNotNull(auth.authToken());
    }

    @Test
    public void registerNegativeDuplicateUsername() throws DataAccessException {
        UserData user = new UserData("bob", "pass", "bob@email.com");
        userService.register(user);
        assertThrows(DataAccessException.class, () -> userService.register(user));
    }

    @Test
    public void loginPositive() throws DataAccessException {
        UserData user = new UserData("carol", "pw123", "c@email.com");
        userService.register(user);
        AuthData auth = userService.login(user);
        assertEquals("carol", auth.username());
    }

    @Test
    public void loginNegativeWrongPassword() throws DataAccessException {
        UserData user = new UserData("dave", "1234", "d@email.com");
        userService.register(user);
        UserData wrong = new UserData("dave", "wrong", "d@email.com");
        assertThrows(DataAccessException.class, () -> userService.login(wrong));
    }

    @Test
    public void logoutPositive() throws DataAccessException {
        UserData user = new UserData("john", "secret", "john@example.com");
        AuthData auth = userService.register(user);
        assertDoesNotThrow(() -> userService.logout(auth.authToken()));
    }

    @Test
    public void logoutNegativeInvalidToken() {
        assertThrows(DataAccessException.class, () -> userService.logout("fake-token"));
    }
}

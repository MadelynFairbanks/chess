package service;

import dataaccess.DataAccess;
import dataaccess.DataAccessException;
import model.AuthData;
import model.UserData;
import java.util.UUID;

public class UserService {
    private final DataAccess dataAccess;

    public UserService(DataAccess dataAccess) {
        this.dataAccess = dataAccess;
    }

    public AuthData register(UserData user) throws DataAccessException {
        // Basic validation
        if (user.username() == null || user.password() == null || user.email() == null) {
            throw new DataAccessException("Error: bad request");
        }

        if (dataAccess.getUser(user.username()) != null) {
            throw new DataAccessException("Error: already taken");
        }

        // Store user
        dataAccess.createUser(user);

        // Create auth token
        String token = UUID.randomUUID().toString();
        AuthData auth = new AuthData(token, user.username());
        dataAccess.createAuth(auth);

        return auth;
    }

    public AuthData login(UserData user) throws DataAccessException {
        if (user == null || user.username() == null || user.password() == null) {
            throw new DataAccessException("Error: bad request");
        }

        UserData existing = dataAccess.getUser(user.username());
        if (existing == null || !existing.password().equals(user.password())) {
            throw new DataAccessException("Error: unauthorized");
        }

        AuthData auth = new AuthData(UUID.randomUUID().toString(), user.username());
        dataAccess.createAuth(auth);
        return auth;
    }

    public void logout(String authToken) throws DataAccessException {
        if (authToken == null || dataAccess.getAuth(authToken) == null) {
            throw new DataAccessException("Error: unauthorized");
        }
        dataAccess.deleteAuth(authToken);
    }

}

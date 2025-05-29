package service;

import dataaccess.DataAccess;
import dataaccess.DataAccessException;
import model.AuthData;
import model.UserData;

import java.util.UUID;
import org.mindrot.jbcrypt.BCrypt;

public class UserService {
    private final DataAccess dataAccess;

    public UserService(DataAccess dataAccess) {
        this.dataAccess = dataAccess;
    }

    public AuthData register(UserData user) throws DataAccessException {
        try {
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

        } catch (DataAccessException e) {
            throw new DataAccessException("Error: " + e.getMessage(), e);
        }
    }

    public AuthData login(UserData user) throws DataAccessException {
        try {
            if (user == null ||
                    user.username() == null || user.username().trim().isEmpty() || user.username().equalsIgnoreCase("null") ||
                    user.password() == null || user.password().trim().isEmpty() || user.password().equalsIgnoreCase("null")) {
                throw new DataAccessException("Error: bad request");
            }

            UserData existing = dataAccess.getUser(user.username());
            if (existing == null || !BCrypt.checkpw(user.password(), existing.password())) {
                throw new DataAccessException("Error: unauthorized");
            }

            AuthData auth = new AuthData(UUID.randomUUID().toString(), user.username());
            dataAccess.createAuth(auth);
            return auth;

        } catch (DataAccessException e) {
            throw new DataAccessException("Error: " + e.getMessage(), e);
        }
    }

    public void logout(String authToken) throws DataAccessException {
        try {
            if (authToken == null || dataAccess.getAuth(authToken) == null) {
                throw new DataAccessException("Error: unauthorized");
            }
            dataAccess.deleteAuth(authToken);
        } catch (DataAccessException e) {
            throw new DataAccessException("Error: " + e.getMessage(), e);
        }
    }
}

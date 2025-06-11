package service;

import dataaccess.DataAccess;
import dataaccess.DataAccessException;
import model.AuthData;
import model.UserData;
import org.mindrot.jbcrypt.BCrypt;

import java.sql.SQLException;
import java.util.UUID;

public class UserService {
    private final DataAccess dataAccess;

    public UserService(DataAccess dataAccess) {
        this.dataAccess = dataAccess;
    }

    // Registers a new user (as long as everything checks out)
    public AuthData register(UserData user) throws DataAccessException {
        try {
            // Let's make sure all fields are filled
            if (user.username() == null || user.password() == null || user.email() == null) {
                throw new DataAccessException("Error: bad request");
            }

            // Let;s check if this username is already taken
            UserData existing;
            try {
                existing = dataAccess.getUser(user.username());
            } catch (DataAccessException e) {
                throw new DataAccessException("Error: internal server error", e);
            }

            if (existing != null) {
                throw new DataAccessException("Error: already taken");
            }

            // Time to save the user, baby
            dataAccess.createUser(user);

            // And then we're generating a new token and save it
            String token = UUID.randomUUID().toString();
            AuthData auth = new AuthData(token, user.username());
            dataAccess.createAuth(auth);

            return auth;
        } catch (DataAccessException e) {
            // If it's a DB issue or something unexpected, throw the right error
            if (e.getCause() instanceof SQLException) {
                throw new DataAccessException("Error: internal server error", e);
            }

            String msg = e.getMessage().toLowerCase();
            if (!msg.contains("already taken") && !msg.contains("bad request")) {
                throw new DataAccessException("Error: internal server error", e);
            }
            throw e;
        }
    }

    // Logs in a user (if their creds are valid)
    public AuthData login(UserData user) throws DataAccessException {
        // Gotta pass in a username + password
        if (user == null ||
                user.username() == null || user.username().trim().isEmpty() ||
                user.password() == null || user.password().trim().isEmpty()) {
            throw new DataAccessException("Error: bad request");
        }

        // NOw we gonna try and find this user in the DB
        UserData existing;
        try {
            existing = dataAccess.getUser(user.username());
        } catch (DataAccessException e) {
            throw new DataAccessException("Error: internal server error", e);
        }

        // User doesn’t exist? Nope. Cancelled.
        if (existing == null) {
            throw new DataAccessException("Error: unauthorized");
        }

        // Checking if the password matches the stored one (hashed or raw)
        String stored = existing.password();
        boolean matches;
        if (stored.startsWith("$2a$") || stored.startsWith("$2b$") || stored.startsWith("$2y$")) {
            // Yup, hashed with BCrypt
            matches = BCrypt.checkpw(user.password(), stored);
        } else {
            // Memory DAO might store it plain (for testing)
            matches = stored.equals(user.password());
        }

        if (!matches) {
            throw new DataAccessException("Error: unauthorized");
        }

        // If everything’s good it's time to give them a shiny new auth token
        try {
            AuthData auth = new AuthData(UUID.randomUUID().toString(), user.username());
            dataAccess.createAuth(auth);
            return auth;
        } catch (DataAccessException e) {
            throw new DataAccessException("Error: internal server error", e);
        }
    }

    // Logs out a user (aka deletes their token)
    public void logout(String authToken) throws DataAccessException {
        try {
            var auth = dataAccess.getAuth(authToken);
            if (auth == null) {
                throw new DataAccessException("Error: unauthorized");
            }

            dataAccess.deleteAuth(authToken);
        } catch (DataAccessException e) {
            if (e.getCause() instanceof SQLException) {
                throw new DataAccessException("Error: internal server error", e);
            }

            String msg = e.getMessage().toLowerCase();
            if (!msg.contains("unauthorized")) {
                throw new DataAccessException("Error: internal server error", e);
            }
            throw e;
        }
    }
}

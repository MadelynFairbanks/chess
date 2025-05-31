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

    public AuthData register(UserData user) throws DataAccessException {
        try {
            if (user.username() == null || user.password() == null || user.email() == null) {
                throw new DataAccessException("Error: bad request");
            }

            UserData existing;
            try {
                existing = dataAccess.getUser(user.username());
            } catch (DataAccessException e) {
                throw new DataAccessException("Error: internal server error", e);
            }

            if (existing != null) {
                throw new DataAccessException("Error: already taken");
            }

            dataAccess.createUser(user);

            String token = UUID.randomUUID().toString();
            AuthData auth = new AuthData(token, user.username());
            dataAccess.createAuth(auth);

            return auth;
        } catch (DataAccessException e) {
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

    public AuthData login(UserData user) throws DataAccessException {
        if (user == null ||
                user.username() == null || user.username().trim().isEmpty() ||
                user.password() == null || user.password().trim().isEmpty()) {
            throw new DataAccessException("Error: bad request");
        }

        UserData existing;
        try {
            existing = dataAccess.getUser(user.username());
        } catch (DataAccessException e) {
            throw new DataAccessException("Error: internal server error", e);
        }

        if (existing == null || !BCrypt.checkpw(user.password(), existing.password())) {
            throw new DataAccessException("Error: unauthorized");
        }

        try {
            AuthData auth = new AuthData(UUID.randomUUID().toString(), user.username());
            dataAccess.createAuth(auth);
            return auth;
        } catch (DataAccessException e) {
            throw new DataAccessException("Error: internal server error", e);
        }
    }

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

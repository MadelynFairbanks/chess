package dataaccess;

import model.AuthData;
import dataaccess.DataAccessException;

public class MySqlAuthDAO {

    // I'm using MySqlAuthTokenDAO as the actual worker for auth token logic
    private final MySqlAuthTokenDAO tokenDAO = new MySqlAuthTokenDAO();

    public void insertAuth(AuthData auth) throws DataAccessException {
        // Just passing this through to the real DAO underneath
        tokenDAO.insertAuth(auth);
    }

    public AuthData findAuth(String token) throws DataAccessException {
        // Same vibes in this one too — it's handing it off to the static method on the helper class
        return MySqlAuthTokenDAO.getAuthData(token);
    }

    public void deleteAuth(String token) throws DataAccessException {
        // Again, we're delegating to the helper to actually remove the token from the DB
        tokenDAO.deleteAuth(token);
    }
}

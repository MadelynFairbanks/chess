package dataaccess;

import model.AuthData;
import dataaccess.DataAccessException;

public class MySqlAuthDAO {

    private final MySqlAuthTokenDAO tokenDAO = new MySqlAuthTokenDAO();

    public void insertAuth(AuthData auth) throws DataAccessException {
        tokenDAO.insertAuth(auth);
    }

    public AuthData findAuth(String token) throws DataAccessException {
        // Delegate to the helper that lives in MySqlAuthTokenDAO:
        return MySqlAuthTokenDAO.getAuthData(token);
    }

    public void deleteAuth(String token) throws DataAccessException {
        tokenDAO.deleteAuth(token);
    }
}

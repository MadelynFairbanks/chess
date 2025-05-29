package dataaccess;

import model.AuthData;
import model.UserData;
import model.GameData;
import java.util.Collection;
import java.util.ArrayList;
import java.util.List;

public class MySqlDataAccess implements DataAccess {
    private final MySqlUserDAO userDAO = new MySqlUserDAO();
    private final MySqlAuthTokenDAO authDAO = new MySqlAuthTokenDAO(); // you’ll create this next

    @Override
    public int generateGameID() {
        // We'll implement this properly when we get to games
        return 0;
    }
    @Override
    public List<GameData> listGames() throws DataAccessException {
        return new ArrayList<>();
    }

    @Override
    public void clear() throws DataAccessException {
        // Implement later
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public void createGame(GameData game) throws DataAccessException {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public GameData getGame(int gameID) throws DataAccessException {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public void updateGame(GameData game) throws DataAccessException {
        throw new UnsupportedOperationException("Not implemented yet");
    }



    @Override
    public void createUser(UserData user) throws DataAccessException {
        userDAO.insertUser(user);
    }

    @Override
    public UserData getUser(String username) throws DataAccessException {
        return userDAO.findUser(username);
    }

    @Override
    public void createAuth(AuthData auth) throws DataAccessException {
        authDAO.insertAuth(auth);
    }

    @Override
    public AuthData getAuth(String token) throws DataAccessException {
        return authDAO.findAuth(token);
    }

    @Override
    public void deleteAuth(String token) throws DataAccessException {
        authDAO.deleteAuth(token);
    }

    // Add stub methods for games if required later
}

package dataaccess;

import model.AuthData;
import model.UserData;
import model.GameData;

import java.sql.*;
import java.util.List;

public class MySqlDataAccess implements DataAccess {
    private final MySqlUserDAO userDAO = new MySqlUserDAO();
    private final MySqlAuthTokenDAO authDAO = new MySqlAuthTokenDAO();
    private final MySqlGameDAO gameDAO = new MySqlGameDAO();


    @Override
    public List<GameData> listGames() throws DataAccessException {
        return gameDAO.listGames();
    }

    @Override
    public void clear() throws DataAccessException {
        gameDAO.clear();
        userDAO.clear();
        authDAO.clear();
    }

    @Override
    public int createGame(GameData game) throws DataAccessException {
        return gameDAO.insertGame(game);
    }

    @Override
    public GameData getGame(int gameID) throws DataAccessException {
        return gameDAO.findGame(gameID);
    }

    @Override
    public void updateGame(GameData game) throws DataAccessException {
        gameDAO.updateGame(game);
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
}

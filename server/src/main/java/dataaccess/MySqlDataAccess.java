package dataaccess;

import model.AuthData;
import model.UserData;
import model.GameData;

import java.sql.*;
import java.util.List;

public class MySqlDataAccess implements DataAccess {
    // Hooking into the actual DAOs that talk to the database
    private final MySqlUserDAO userDAO = new MySqlUserDAO();
    private final MySqlAuthTokenDAO authDAO = new MySqlAuthTokenDAO();
    private final MySqlGameDAO gameDAO = new MySqlGameDAO();

    // Getting all games that exist in the DB right now
    @Override
    public List<GameData> listGames() throws DataAccessException {
        return gameDAO.listGames();
    }

    // Wipe everything: users, tokens, games, the works
    @Override
    public void clear() throws DataAccessException {
        gameDAO.clear();
        userDAO.clear();
        authDAO.clear();
    }

    // Save a new game to the DB and return its ID
    @Override
    public int createGame(GameData game) throws DataAccessException {
        return gameDAO.insertGame(game);
    }

    // Pull a game from the DB by its ID
    @Override
    public GameData getGame(int gameID) throws DataAccessException {
        return gameDAO.findGame(gameID);
    }

    // Update a game (like if someone made a move or joined)
    @Override
    public void updateGame(GameData game) throws DataAccessException {
        gameDAO.updateGame(game);
    }

    // Add a new user to the DB
    @Override
    public void createUser(UserData user) throws DataAccessException {
        userDAO.insertUser(user);
    }

    // Look up a user by their username
    @Override
    public UserData getUser(String username) throws DataAccessException {
        return userDAO.findUser(username);
    }

    // Save a new auth token (aka login session)
    @Override
    public void createAuth(AuthData auth) throws DataAccessException {
        authDAO.insertAuth(auth);
    }

    // Get the auth token info (used to check who's logged in)
    @Override
    public AuthData getAuth(String token) throws DataAccessException {
        return authDAO.findAuth(token);
    }

    // Delete someone's auth token (logs them out)
    @Override
    public void deleteAuth(String token) throws DataAccessException {
        authDAO.deleteAuth(token);
    }
}

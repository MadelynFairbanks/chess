package dataaccess;

import model.UserData;
import model.AuthData;
import model.GameData;
import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.Map;

public class MemoryDataAccess implements DataAccess {

    // Just using HashMaps to simulate the database for users, auth tokens, and games
    private final Map<String, UserData> users = new HashMap<>();
    private final Map<String, AuthData> authTokens = new HashMap<>();
    private final Map<Integer, GameData> games = new HashMap<>();

    // Used to simulate auto-incrementing game IDs
    private int nextGameID = 1;


    @Override
    public void clear() {
        // Reset everything back to the beginning
        users.clear();
        authTokens.clear();
        games.clear();
        nextGameID = 1;
    }

    @Override
    public void createUser(UserData user) throws DataAccessException {
        // Don't allow duplicate usernames
        if (users.containsKey(user.username())) {
            throw new DataAccessException("User already exists.");
        }
        users.put(user.username(), user);
    }

    @Override
    public UserData getUser(String username) {
        // Simple lookup for a user by username
        return users.get(username);
    }

    @Override
    public void createAuth(AuthData auth) {
        // Adds a new auth token for a logged-in user
        authTokens.put(auth.authToken(), auth);
    }

    @Override
    public AuthData getAuth(String authToken) {
        // Finds an auth token (to check if someone’s logged in)
        return authTokens.get(authToken);
    }

    @Override
    public void deleteAuth(String authToken) {
        // Logs out — (removes the token)
        authTokens.remove(authToken);
    }

    @Override
    public int createGame(GameData game) {
        // Saves the new game — this assumes gameID is already set correctly
        games.put(game.gameID(), game);
        return game.gameID();
    }

    @Override
    public GameData getGame(int gameID) {
        // This looks up a game by its ID
        return games.get(gameID);
    }

    @Override
    public void updateGame(GameData game) throws DataAccessException {
        // Let's make sure the game exists before updating it
        if (!games.containsKey(game.gameID())) {
            throw new DataAccessException("Game does not exist.");
        }
        games.put(game.gameID(), game);
    }

    @Override
    public List<GameData> listGames() {
        // Returns a list of all the games we've saved
        return new ArrayList<>(games.values());
    }


}

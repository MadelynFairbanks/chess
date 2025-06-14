package dataaccess;

import chess.ChessGame;
import model.AuthData;
import model.GameData;
import model.GameList;
import model.UserData;
import org.mindrot.jbcrypt.BCrypt;

import java.util.*;

/**
 * 🚀 This class is like a fake database that lives in RAM.
 * Great for testing and vibing without touching MySQL.
 */
public class MemoryDataAccessMethods implements DataAccessInterface {

    private static final Map<String, UserData> REGISTEREDUSERS = new HashMap<>();
    private static final Map<Integer, GameData> CREATEDGAMES = new HashMap<>();
    private static final Map<String, AuthData> AUTHDATA = new HashMap<>();

    /**
     * 🧹 Wipes everything from memory — factory reset time.
     */
    public String clear() throws DataAccessException {
        try {
            REGISTEREDUSERS.clear();
            CREATEDGAMES.clear();
            AUTHDATA.clear();
        } catch (Exception e) {
            throw new DataAccessException(e.getMessage(), 500);
        }
        return "";
    }

    /**
     * 👀 Fetch user by username.
     */
    public UserData getUser(String username) {
        return REGISTEREDUSERS.get(username);
    }

    /**
     * 🔐 Hash that password like a security pro.
     */
    private String hashPassword(String clearTextPassword) {
        return BCrypt.hashpw(clearTextPassword, BCrypt.gensalt());
    }

    /**
     * ✍️ Registers a user and stores them in memory.
     */
    public void createUser(UserData userData) {
        var hashed = hashPassword(userData.password());
        REGISTEREDUSERS.put(userData.username(), new UserData(userData.username(), hashed, userData.email()));
    }

    /**
     * 🔑 Stores the user's auth token.
     */
    public void createAuth(AuthData auth) {
        AUTHDATA.put(auth.authToken(), auth);
    }

    /**
     * 🕵️ Gets auth data for a session token.
     */
    public AuthData getAuth(String token) {
        return AUTHDATA.get(token);
    }

    /**
     * ❌ Logs out the user by deleting their auth token.
     */
    public void deleteAuth(String token) {
        AUTHDATA.remove(token);
    }

    /**
     * 🎲 Returns a list of all games that have been created.
     */
    public Collection<GameList> listGames() {
        Collection<GameList> gameList = new ArrayList<>();
        for (var game : CREATEDGAMES.values()) {
            gameList.add(new GameList(game.gameID(), game.whiteUsername(), game.blackUsername(), game.gameName()));
        }
        return gameList;
    }

    /**
     * 🏁 Creates a new chess game and puts it in memory.
     */
    public void createGame(int gameID, String gameName, ChessGame game) {
        CREATEDGAMES.put(gameID, new GameData(gameID, null, null, gameName, game));
    }

    /**
     * 🔍 Retrieves a chess game by ID.
     */
    public GameData getGame(int gameID) {
        return CREATEDGAMES.get(gameID);
    }

    /**
     * 🔄 Updates the game's players, name, or state.
     */
    public void updateGame(int gameID, String whiteUsername, String blackUsername,
                           String gameName, ChessGame game) {

        System.out.println("🎯 Updating game info in memory...");

        GameData updated = CREATEDGAMES.get(gameID);

        if (whiteUsername != null) {
            updated = updated.setWhiteUsername(whiteUsername);
            System.out.println("👻 white updated");
        }
        if (blackUsername != null) {
            updated = updated.setBlackUsername(blackUsername);
            System.out.println("🖤 black updated");
        }
        if (gameName != null) {
            updated = updated.setGameName(gameName);
        }
        if (game != null) {
            updated = updated.setGame(game);
        }

        CREATEDGAMES.put(gameID, updated);
    }

    /**
     * 🚫 You’re not supposed to call this on the in-memory DAO, but it’s required by the interface.
     */
    @Override
    public void updateGameUsernames(int gameID, String whiteUsername, String blackUsername) throws DataAccessException {
        System.out.println("🚫 Nope. updateGameUsernames not used here — " + gameID + whiteUsername + blackUsername);
    }
}

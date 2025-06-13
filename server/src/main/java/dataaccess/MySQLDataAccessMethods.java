package dataaccess;

import chess.ChessGame;
import com.google.gson.Gson;
import model.AuthData;
import model.GameData;
import model.GameList;
import model.UserData;
import org.mindrot.jbcrypt.BCrypt;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;

import static java.sql.Statement.RETURN_GENERATED_KEYS;

/**
 * 💽 Real-deal MySQL-based data access. No more messing around.
 * This is where your info lives... and dies (when truncated 😵).
 */
public class MySQLDataAccessMethods implements DataAccessInterface {

    public MySQLDataAccessMethods() throws DataAccessException {
        try {
            configureDatabase(); // 📦 Set up them tables baby
        } catch (DataAccessException e) {
            throw new DataAccessException(e.getMessage(), 400);
        }
    }

    /**
     * 💥 Boom — wipes the whole database clean.
     */
    public String clear() throws DataAccessException {
        try {
            executeUpdate("TRUNCATE TABLE AuthData");
            executeUpdate("TRUNCATE TABLE GameData");
            executeUpdate("TRUNCATE TABLE UserData");
        } catch (DataAccessException e) {
            throw new DataAccessException(e.getMessage(), 500);
        }
        return "";
    }

    /**
     * 🧑‍💻 Fetch user from the database. Username is your ticket in.
     */
    public UserData getUser(String username) throws DataAccessException {
        try (var conn = DatabaseManager.getConnection()) {
            var sql = "SELECT username, password, email FROM UserData WHERE username=?";
            try (var ps = conn.prepareStatement(sql)) {
                ps.setString(1, username);
                try (var rs = ps.executeQuery()) {
                    if (rs.next()) {
                        return new UserData(rs.getString("username"), rs.getString("password"), rs.getString("email"));
                    } else {
                        return null; // 🫠 Username who? Never heard of them.
                    }
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException(e.getMessage(), 400);
        }
    }

    /**
     * 🔒 Hash that password like it owes you money.
     */
    private String hashPassword(String password) {
        return BCrypt.hashpw(password, BCrypt.gensalt());
    }

    /**
     * 🆕 Register a new user in the DB.
     */
    public void createUser(UserData user) throws DataAccessException {
        if (user.username() == null || user.password() == null || user.email() == null) {
            throw new DataAccessException("Bad data when creating user", 500);
        }
        var sql = "INSERT INTO UserData (username, password, email) VALUES (?, ?, ?)";
        executeUpdate(sql, user.username(), hashPassword(user.password()), user.email());
    }

    /**
     * ✨ Save the auth token so the user can vibe around the site.
     */
    public void createAuth(AuthData auth) throws DataAccessException {
        if (auth.username() == null || auth.authToken() == null) {
            throw new DataAccessException("Bad data when creating auth", 500);
        }
        var sql = "INSERT INTO AuthData (authToken, username) VALUES (?, ?)";
        executeUpdate(sql, auth.authToken(), auth.username());
    }

    /**
     * 👁️ Fetch auth token info from DB.
     */
    public AuthData getAuth(String token) throws DataAccessException {
        try (var conn = DatabaseManager.getConnection()) {
            var sql = "SELECT authToken, username FROM AuthData WHERE authToken=?";
            try (var ps = conn.prepareStatement(sql)) {
                ps.setString(1, token);
                try (var rs = ps.executeQuery()) {
                    if (rs.next()) {
                        return new AuthData(rs.getString("authToken"), rs.getString("username"));
                    } else {
                        throw new DataAccessException("Unauthorized logout attempt", 401);
                    }
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException(e.getMessage(), 400);
        }
    }

    /**
     * 🧼 Remove the auth token — logout time.
     */
    public void deleteAuth(String token) throws DataAccessException {
        if (token == null || token.isEmpty()) {
            throw new DataAccessException("Bad data during logout", 500);
        }
        var sql = "DELETE FROM AuthData WHERE authToken=?";
        executeUpdate(sql, token);
    }

    /**
     * 📜 Lists all the available games in the DB.
     */
    public Collection<GameList> listGames() {
        var games = new ArrayList<GameList>();
        try (var conn = DatabaseManager.getConnection()) {
            var sql = "SELECT gameID, whiteUsername, blackUsername, gameName FROM GameData";
            try (var ps = conn.prepareStatement(sql);
                 var rs = ps.executeQuery()) {
                while (rs.next()) {
                    games.add(new GameList(
                            rs.getInt("gameID"),
                            rs.getString("whiteUsername"),
                            rs.getString("blackUsername"),
                            rs.getString("gameName")
                    ));
                }
            }
        } catch (SQLException | DataAccessException e) {
            throw new RuntimeException(e); // 🚨 We'll let this one bubble up.
        }
        return games;
    }

    /**
     * 🧪 Insert a new game into the DB.
     */
    public void createGame(int gameID, String gameName, ChessGame game) throws DataAccessException {
        if (gameName.isEmpty() || game == null) {
            throw new DataAccessException("Bad data when creating game", 500);
        }
        var gameJson = new Gson().toJson(game);
        var sql = "INSERT INTO GameData (gameID, whiteUsername, blackUsername, gameName, gameJson) VALUES (?, ?, ?, ?, ?)";
        executeUpdate(sql, gameID, null, null, gameName, gameJson);
    }

    /**
     * 🔍 Retrieve game info by ID.
     */
    public GameData getGame(int gameID) throws DataAccessException {
        try (var conn = DatabaseManager.getConnection()) {
            var sql = "SELECT * FROM GameData WHERE gameID=?";
            try (var ps = conn.prepareStatement(sql)) {
                ps.setInt(1, gameID);
                try (var rs = ps.executeQuery()) {
                    if (rs.next()) {
                        var game = new Gson().fromJson(rs.getString("gameJson"), ChessGame.class);
                        return new GameData(
                                rs.getInt("gameID"),
                                rs.getString("whiteUsername"),
                                rs.getString("blackUsername"),
                                rs.getString("gameName"),
                                game
                        );
                    } else {
                        throw new DataAccessException("Game not found", 400);
                    }
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException(e.getMessage(), 400);
        }
    }

    /**
     * 🛠️ Update the game entry with new info.
     */
    public void updateGame(int gameID, String white, String black, String name, ChessGame game) throws DataAccessException {
        var updated = getGame(gameID);
        if (white != null) {
            updated = updated.setWhiteUsername(white);
        }
        if (black != null) {
            updated = updated.setBlackUsername(black);
        }
        if (name != null) {
            updated = updated.setGameName(name);
        }
        if (game != null) {
            updated = updated.setGame(game);
        }

        var gameJson = new Gson().toJson(updated.game());
        var sql = "UPDATE GameData SET whiteUsername=?, blackUsername=?, gameName=?, gameJson=? WHERE gameID=?";
        executeUpdate(sql, updated.whiteUsername(), updated.blackUsername(), updated.gameName(), gameJson, gameID);
    }

    /**
     * 🙅‍♀️ Clear player usernames from a game.
     */
    public void updateGameUsernames(int gameID, String white, String black) throws DataAccessException {
        var updated = getGame(gameID);
        if (white != null) {
            updated = updated.setWhiteUsername(null);
        }
        if (black != null) {
            updated = updated.setBlackUsername(null);
        }

        var gameJson = new Gson().toJson(updated.game());
        var sql = "UPDATE GameData SET whiteUsername=?, blackUsername=?, gameName=?, gameJson=? WHERE gameID=?";
        executeUpdate(sql, updated.whiteUsername(), updated.blackUsername(), updated.gameName(), gameJson, gameID);
    }

    /**
     * 🏗️ DB structure — we build this when initializing.
     */
    private final String[] createStatements = {
            """
            CREATE TABLE IF NOT EXISTS UserData (
                username VARCHAR(75) PRIMARY KEY,
                password VARCHAR(100),
                email VARCHAR(100)
            );
            """,
            """
            CREATE TABLE IF NOT EXISTS GameData (
                gameID INT PRIMARY KEY,
                whiteUsername VARCHAR(100),
                blackUsername VARCHAR(100),
                gameName VARCHAR(150),
                gameJson TEXT DEFAULT NULL
            );
            """,
            """
            CREATE TABLE IF NOT EXISTS AuthData (
                authToken VARCHAR(100) PRIMARY KEY,
                username VARCHAR(100)
            );
            """
    };

    /**
     * 🧱 Bootstraps all tables. Run once at startup.
     */
    private void configureDatabase() throws DataAccessException {
        DatabaseManager.createDatabase();
        try (var conn = DatabaseManager.getConnection()) {
            for (var sql : createStatements) {
                try (var ps = conn.prepareStatement(sql)) {
                    ps.executeUpdate();
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException(e.getMessage(), 500);
        }
    }

    /**
     * 🔁 Utility to simplify updates. Clean, reusable, slay.
     */
    private static void executeUpdate(String sql, Object... params) throws DataAccessException {
        try (var conn = DatabaseManager.getConnection();
             var ps = conn.prepareStatement(sql, RETURN_GENERATED_KEYS)) {
            for (int i = 0; i < params.length; i++) {
                ps.setObject(i + 1, params[i]);
            }
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException(e.getMessage(), 500);
        }
    }
}

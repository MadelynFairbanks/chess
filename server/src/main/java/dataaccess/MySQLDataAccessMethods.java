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

public class MySQLDataAccessMethods implements DataAccessInterface {
    public MySQLDataAccessMethods() throws DataAccessException {
        try {
            configureDatabase();
        } catch (DataAccessException e) {
            throw new DataAccessException(e.getMessage(),400);
        }
    }

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


    public UserData getUser(String username) throws DataAccessException {
//        search DB for username
        try (var connection = DatabaseManager.getConnection()) {
            var statement = "SELECT username, password, email FROM UserData WHERE username=?";
            try (var ps = connection.prepareStatement(statement)) {
                ps.setString(1, username);
                try (var rs = ps.executeQuery()) {
                    if (rs.next()) {
                        return new UserData(
                                rs.getString("username"),
                                rs.getString("password"),
                                rs.getString("email"));
                    } else {
                        return null; // or throw an exception if preferred
                    }
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException(e.getMessage(), 400);
        }

    }


    private static void executeUpdate(String statement, Object... params) throws DataAccessException {
        try (var conn = DatabaseManager.getConnection()) {
            try (var ps = conn.prepareStatement(statement, RETURN_GENERATED_KEYS)) {
                for (int i = 0; i < params.length; i++) {
                    ps.setObject(i + 1, params[i]);
                }
                ps.executeUpdate();
            }
        } catch (SQLException e) {
            throw new DataAccessException(e.getMessage(), 500);
        }
    }

    String hashPassword(String clearTextPassword) {
        return BCrypt.hashpw(clearTextPassword, BCrypt.gensalt());
    }


    public void createUser(UserData userData) throws DataAccessException {
//    create user object, add to db
        if (userData.username() == null || userData.password() == null || userData.email() == null) {
            throw new DataAccessException("Bad data internal server error creating user", 500);
        }
        var statement = "INSERT INTO UserData (username, password, email) VALUES (?, ?, ?)";
        executeUpdate(statement, userData.username(), hashPassword(userData.password()), userData.email());
    }

    public void createAuth(AuthData authData) throws DataAccessException {
        if (authData.username() == null || authData.authToken() == null) {
            throw new DataAccessException(" Bad data internal server error creating auth", 500);
        }
        var statement = "INSERT INTO AuthData (authToken, username) VALUES (?, ?)";
        executeUpdate(statement, authData.authToken(), authData.username());
    }

    public AuthData getAuth(String token) throws DataAccessException {
        try (var connection = DatabaseManager.getConnection()) {
            var statement = "SELECT authToken, username FROM AuthData WHERE authToken=?";
            try (var ps = connection.prepareStatement(statement)) {
                ps.setString(1, token);
                try (var rs = ps.executeQuery()) {
                    if (rs.next()) {
                        return new AuthData(rs.getString("authToken"), rs.getString("username"));
                    } else {
                        throw new DataAccessException(" unauthorized log out", 401);
                    }
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException(e.getMessage(), 400);
        }
    }


    public void deleteAuth(String token) throws DataAccessException {
        if (token == null || token.isEmpty()) {
            throw new DataAccessException(" Bad data internal server error", 500);
        }
        var statement = "DELETE FROM AuthData WHERE authToken=?";
        executeUpdate(statement, token);
    }

    public Collection<GameList> listGames() {
        Collection<model.GameList> gameList = new ArrayList<>();
        try (var conn = DatabaseManager.getConnection()) {
            var statement = "SELECT gameID, whiteUsername, blackUsername, gameName FROM GameData";
            try (var ps = conn.prepareStatement(statement)) {
                try (var rs = ps.executeQuery()) {
                    while (rs.next()) {
                        int gameID = rs.getInt("gameID");
                        String whiteUsername = rs.getString("whiteUsername");
                        String blackUsername = rs.getString("blackUsername");
                        String gameName = rs.getString("gameName");

                        gameList.add(new GameList(gameID, whiteUsername, blackUsername, gameName));
                    }
                }
            }
        } catch (SQLException | DataAccessException e) {
            throw new RuntimeException(e);
        }
        return gameList;
    }


    public void createGame(int gameID, String gameName, ChessGame game) throws DataAccessException {
        if (gameName.isEmpty() || game == null) {
            throw new DataAccessException(" Bad data internal server error", 500);
        }
        var gameJson = new Gson().toJson(game);
        var statement =
                "INSERT INTO GameData (gameID, whiteUsername, blackUsername, gameName, gameJson) " +
                        "VALUES (?, ?, ?, ?, ?)";
        executeUpdate(statement, gameID, null, null,gameName, gameJson);
    }

    public GameData getGame(int gameID) throws DataAccessException {
        try (var connection = DatabaseManager.getConnection()) {
            var statement =
                    "SELECT gameID, whiteUsername, blackUsername, gameName, gameJson FROM GameData WHERE gameID=?";
            try (var ps = connection.prepareStatement(statement)) {
                ps.setInt(1, gameID);
                try (var rs = ps.executeQuery()) {
                    if (rs.next()) {
                        int thisGameID = (rs.getInt("gameID"));
                        String thisWhiteUsername = rs.getString("whiteUsername");
                        String thisBlackUsername = rs.getString("blackUsername");
                        String thisGameName = rs.getString("gameName");
                        String thisGameJson = rs.getString("gameJson");
                        ChessGame game = new Gson().fromJson(thisGameJson, ChessGame.class);
                        return new GameData(thisGameID, thisWhiteUsername, thisBlackUsername, thisGameName, game);
                    } else {
                        throw new DataAccessException(" You dumb from get game", 400);
                        // or throw an exception if preferred
                    }
                }
            }
        } catch (SQLException | DataAccessException e) {
            throw new DataAccessException(e.getMessage(), 400);
        }
    }


    public void updateGame(int gameID, String whiteUsername, String blackUsername,
                           String gameName, ChessGame game) throws DataAccessException {
        GameData origonalGameData = getGame(gameID);
        if(whiteUsername != null){
            origonalGameData = origonalGameData.setWhiteUsername(whiteUsername);
        }
        if(blackUsername != null){
            origonalGameData = origonalGameData.setBlackUsername(blackUsername);
        }
        if(gameName != null){
            origonalGameData = origonalGameData.setGameName(gameName);
        }
        if(game != null){
            origonalGameData = origonalGameData.setGame(game);
        }

        var gameJson = new Gson().toJson(origonalGameData.game());
        var statement = "UPDATE GameData SET whiteUsername=?, blackUsername=?, gameName=?, gameJson=? WHERE gameID=?";
        executeUpdate(statement,
                origonalGameData.whiteUsername(),
                origonalGameData.blackUsername(),
                origonalGameData.gameName(),
                gameJson,
                origonalGameData.gameID());
    }


    public void updateGameUsernames(int gameID, String whiteUsername, String blackUsername) throws DataAccessException {
        GameData origonalGameData = getGame(gameID);
        if(whiteUsername != null){
            origonalGameData = origonalGameData.setWhiteUsername(null);
        }
        if(blackUsername != null){
            origonalGameData = origonalGameData.setBlackUsername(null);
        }

        var gameJson = new Gson().toJson(origonalGameData.game());
        var statement = "UPDATE GameData SET whiteUsername=?, blackUsername=?, gameName=?, gameJson=? WHERE gameID=?";
        executeUpdate(statement,
                origonalGameData.whiteUsername(),
                origonalGameData.blackUsername(),
                origonalGameData.gameName(),
                gameJson,
                origonalGameData.gameID());
    }


    private final String[] createStatements = {
            """
            CREATE TABLE IF NOT EXISTS UserData (
            username varchar(75) PRIMARY KEY,
            password varchar(100),
            email varchar(100));
            """,
            """
            CREATE TABLE IF NOT EXISTS GameData (
            gameID INT PRIMARY KEY,
            whiteUsername varchar(100),
            blackUsername varchar(100),
            gameName varchar(150),
            gameJson TEXT DEFAULT NULL);
            """,
            """
            CREATE TABLE IF NOT EXISTS AuthData (
            authToken varchar(100) PRIMARY KEY,
            username varchar(100));
            """
    };

    private void configureDatabase() throws DataAccessException {
        DatabaseManager.createDatabase();
        try (var conn = DatabaseManager.getConnection()) {
            for (var statement : createStatements) {
                try (var preparedStatement = conn.prepareStatement(statement)) {
                    preparedStatement.executeUpdate();
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException(e.getMessage(), 500);
        }
    }

}



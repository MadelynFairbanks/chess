package service;

import chess.ChessGame;
import dataaccess.DataAccessException;
import dataaccess.DataAccessInterface;
import model.*;
import org.mindrot.jbcrypt.BCrypt;

import java.util.Collection;
import java.util.UUID;

public class TheChessService {

    private static DataAccessInterface dataAccess;

    public TheChessService(DataAccessInterface dataAccess) {
        this.dataAccess = dataAccess;
    }


    public static String clear() throws DataAccessException {
        return dataAccess.clear();
    }

    public static AuthData register(UserData registerRequest) throws DataAccessException {
        if (registerRequest.password() == null || registerRequest.username() == null) {
            throw new DataAccessException("Error: Invalid User", 400);
        } else if (registerRequest.username().isEmpty() ||
                registerRequest.password().isEmpty() || registerRequest.email().isEmpty()) {
            throw new DataAccessException("Error: Incomplete Information!!!", 400);
        }
        if (dataAccess.getUser(registerRequest.username()) == null) {
            dataAccess.createUser(registerRequest);
            AuthData registerResult = new AuthData(generateAuthToken(), registerRequest.username());
            dataAccess.createAuth(registerResult);
            return registerResult;
        } else {
            throw new DataAccessException("Error: Username " + registerRequest.username() + " is taken.", 403);
        }
    }


    private static String generateAuthToken() {
        return UUID.randomUUID().toString();
    }


    public static AuthData logIn(UserData logInRequest) throws DataAccessException {
        if (logInRequest.username() == null || logInRequest.password() == null) {
            throw new DataAccessException("Error: Incomplete Information!!!", 400);
        }
        if (logInRequest.username().isEmpty() || logInRequest.password().isEmpty()) {
            throw new DataAccessException("Error: Incomplete Information!!!", 400);
        }

        UserData userData = dataAccess.getUser(logInRequest.username());
        if (userData == null) {
            throw new DataAccessException("Error: Invalid User", 401);
        } else if (!(BCrypt.checkpw(logInRequest.password(), userData.password())) || userData.username().isEmpty()) {
            throw new DataAccessException("Error: invalid credentials", 401);
        } else {
            AuthData logInResult = new AuthData(generateAuthToken(), logInRequest.username());
            dataAccess.createAuth(logInResult);
            return logInResult;
        }
    }

    public static Object logOut(String token) throws DataAccessException {
        AuthData authData = dataAccess.getAuth(token);
        if (authData == null) {
            throw new DataAccessException("Error: unauthorized log out", 401);
        }
        dataAccess.deleteAuth(token);
        return "";
    }


    public static Object listGames(String token) throws DataAccessException {
        AuthData authData = dataAccess.getAuth(token);
        if (authData == null) {
            throw new DataAccessException("Error: unauthorized request to list games", 401);
        }
        return dataAccess.listGames();
    }


    public static int createGame(String token, GameData createGameRequest) throws DataAccessException {
        AuthData authData = dataAccess.getAuth(token);
        if (authData == null) {
            throw new DataAccessException("Error: unauthorized", 401);
        }

        String gameName = createGameRequest.gameName();
        if (gameName == null || gameName.isEmpty()) {
            throw new DataAccessException("Error: bad request, no game name", 400);
        }

        ChessGame game = new ChessGame();
        int gameID = generateGameID();
        dataAccess.createGame(gameID, gameName, game);

        return gameID;
    }


    private static int generateGameID() {
        Collection<GameList> gameDataList = dataAccess.listGames();
        int idnum = 0;
        for (GameList ignored : gameDataList) {
            idnum++;
        }
        return idnum + 1;
    }


    public static void joinGame(String token, JoinGame joinGameRequest) throws DataAccessException {
        AuthData authData = dataAccess.getAuth(token);
        if (authData == null) {
            throw new DataAccessException("Error: unauthorized", 401);
        }
        GameData game = dataAccess.getGame(joinGameRequest.gameID());
        if (game == null) {
            throw new DataAccessException("Error:  bad request", 400);
        }
        if (joinGameRequest.playerColor() == null) {
            throw new DataAccessException("Error: bad request", 400);
        }

        switch (joinGameRequest.playerColor()) {
            case "WHITE":
                if (game.whiteUsername() == null) {
                    dataAccess.updateGame(game.gameID(), authData.username(), null, null, null);
                } else {
                    throw new DataAccessException("Error: already taken", 403);
                }
                break;
            case "BLACK":
                if (game.blackUsername() == null) {
                    dataAccess.updateGame(game.gameID(), null, authData.username(), null, null);
                } else {
                    throw new DataAccessException("Error: already taken", 403);
                }
                break;
            default: {
                throw new DataAccessException("Error: Forbidden", 400);
            }
        }
    }


    public static GameData getGame(String token, GameID getGameRequest) throws DataAccessException {
        AuthData authData = dataAccess.getAuth(token);
        if (authData == null) {
            throw new DataAccessException("Error: unauthorized", 401);
        }

        GameData game = dataAccess.getGame(getGameRequest.gameID());
        if (game == null) {
            throw new DataAccessException("Error: bad request", 400);
        }

        return game;

    }

    public static String updateGame(String token, GameData updateGameRequest) throws DataAccessException {
        AuthData authData = dataAccess.getAuth(token);
        if (authData == null) {
            throw new DataAccessException("Error: unauthorized", 401);
        }

        GameData game = dataAccess.getGame(updateGameRequest.gameID());
        if (game == null) {
            throw new DataAccessException("Error: bad request", 400);
        }

        if(!game.gameName().equals(updateGameRequest.gameName())){
            throw new DataAccessException("Error: bad request", 400);
        }

        dataAccess.updateGame(updateGameRequest.gameID(), null, null, null, updateGameRequest.game());
        System.out.println("success updating game oo");
        return "success updating game o";
    }

    public static String updateGameWhiteUsername(String token, String username, int gameID) throws DataAccessException {
        AuthData authData = dataAccess.getAuth(token);
        if (authData == null) {
            throw new DataAccessException("Error: unauthorized", 401);
        }
        dataAccess.updateGameUsernames(gameID, "username", null);
        System.out.println("success updating game ww");
        return "success updating game w";
    }

    public static String updateGameBlackUsername(String token, String username, int gameID) throws DataAccessException {
        AuthData authData = dataAccess.getAuth(token);
        if (authData == null) {
            throw new DataAccessException("Error: unauthorized", 401);
        }
        dataAccess.updateGameUsernames(gameID, null, "username");
        System.out.println("success updating game bb");
        return "success updating game b";
    }



    public static AuthData getAuthData(String token) throws DataAccessException {
        AuthData authData = dataAccess.getAuth(token);
        if (authData == null) {
            throw new DataAccessException("Error: unauthorized", 401);
        }
        return authData;
    }
}




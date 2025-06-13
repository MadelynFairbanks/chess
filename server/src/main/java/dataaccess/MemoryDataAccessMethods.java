package dataaccess;

import chess.ChessGame;
import model.AuthData;
import model.GameData;
import model.GameList;
import model.UserData;
import org.mindrot.jbcrypt.BCrypt;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class MemoryDataAccessMethods implements DataAccessInterface{
    private static Map<String, UserData> registeredUsers = new HashMap<>();
    private static Map<Integer, GameData> createdGames = new HashMap<>();
    private static Map<String, AuthData> authData = new HashMap<>();

    public String clear() throws DataAccessException {
        try {
            registeredUsers.clear();
            createdGames.clear();
            authData.clear();
        } catch (Exception e){
            throw new DataAccessException(e.getMessage(), 500);
        }
        return "";
    }

    public UserData getUser(String username) {
//        search DB for username
        return registeredUsers.get(username);
    }
    String hashPassword(String clearTextPassword) {
        return BCrypt.hashpw(clearTextPassword, BCrypt.gensalt());
    }

    public void createUser(UserData userData) {
//    create user object, add to db
        registeredUsers.put(userData.username(), new UserData(userData.username(),hashPassword(userData.password()), userData.email()));
    }


    public void createAuth(AuthData authData) {
        MemoryDataAccessMethods.authData.put(authData.authToken(), authData);
    }

    public AuthData getAuth(String token) {
        return authData.get(token);
    }


    public void deleteAuth(String token) {
        authData.remove(token);
    }

    public Collection<GameList> listGames() {
        Collection<model.GameList> gameList = new ArrayList<>();
        for (GameData gameData : createdGames.values()){
            gameList.add(new model.GameList(gameData.gameID(), gameData.whiteUsername(),
                    gameData.blackUsername(), gameData.gameName()));
        }
        return gameList;
    }


    public void createGame(int gameID, String gameName, ChessGame game) {
        createdGames.put(gameID, new GameData(gameID,null, null, gameName, game));
    }

    public GameData getGame(int gameID) {
        return createdGames.get(gameID);
    }

    public void updateGame(int gameID, String whiteUsername, String blackUsername,
                           String gameName, ChessGame game) {
        System.out.println("update game running");

        GameData origonalGameData = createdGames.get(gameID);
        if(whiteUsername != null){
            origonalGameData = origonalGameData.setWhiteUsername(whiteUsername);
            System.out.println("white set to null");

        }
        if(blackUsername != null){
            origonalGameData = origonalGameData.setBlackUsername(blackUsername);
            System.out.println("black set to null");
        }
        if(gameName != null){
            origonalGameData = origonalGameData.setGameName(gameName);
        }
        if(game != null){
            origonalGameData = origonalGameData.setGame(game);
        }

        createdGames.put(gameID,origonalGameData);

    }

    @Override
    public void updateGameUsernames(int gameID, String whiteUsername, String blackUsername) throws DataAccessException {
        System.out.println("No way jose -- " + gameID + whiteUsername + blackUsername);

    }



}
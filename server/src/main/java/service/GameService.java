package service;

import chess.ChessMove;
import dataaccess.DataAccess;
import dataaccess.DataAccessException;
import model.AuthData;
import model.GameData;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.List;
import request.CreateGameRequest;
import result.CreateGameResult;
import request.JoinGameRequest;
import chess.ChessGame;

public class GameService {
    private final DataAccess dataAccess;
    private final AtomicInteger idCounter = new AtomicInteger(1); // auto-increment ID

    public GameService(DataAccess dataAccess) {
        this.dataAccess = dataAccess;
    }

    public int createGame(String authToken, String gameName) throws DataAccessException {
        System.out.println("✅ Entered GameService.createGame()");
        System.out.println("authToken: " + authToken + ", gameName: " + gameName);

        if (authToken == null || gameName == null || gameName.isEmpty()) {
            System.out.println("❌ Bad request: null/empty input");
            throw new DataAccessException("Error: bad request");
        }

        AuthData auth = dataAccess.getAuth(authToken);
        if (auth == null) {
            System.out.println("❌ Invalid auth token: " + authToken);
            throw new DataAccessException("Error: unauthorized");
        }

        int gameID = idCounter.getAndIncrement();
        System.out.println("✅ Generated game ID: " + gameID);

        GameData game = new GameData(gameID, null, null, gameName, new chess.ChessGame());
        System.out.println("✅ GameData object created");

        try {
            dataAccess.createGame(game);
            System.out.println("✅ Game stored in database");
        } catch (Exception e) {
            System.out.println("❌ Failed to store game in database");
            e.printStackTrace(); // <== this will finally show us the root cause
            throw new DataAccessException("Error: failed to store game", e);
        }
        System.out.println("✅ Game successfully added to database");

        return gameID;
    }


    public List<GameData> listGames(String authToken) throws DataAccessException {
        if (authToken == null) {
            throw new DataAccessException("Error: unauthorized");
        }

        var auth = dataAccess.getAuth(authToken);
        if (auth == null) {
            throw new DataAccessException("Error: unauthorized");
        }

        return dataAccess.listGames();
    }

    public CreateGameResult createGame(String authToken, CreateGameRequest request) throws DataAccessException {
        System.out.println("✅ Entered createGame (auth + request)");
        System.out.println("authToken = " + authToken);
        System.out.println("request = " + request);

        AuthData auth = dataAccess.getAuth(authToken);
        if (auth == null) {
            System.out.println("❌ Invalid auth token");
            throw new DataAccessException("Error: unauthorized");
        }

        if (request == null || request.gameName() == null || request.gameName().isBlank()) {
            System.out.println("❌ Invalid game name");
            throw new DataAccessException("Error: bad request");
        }

        GameData newGame = new GameData(-1, null, null, request.gameName(), new ChessGame());
        int gameID = dataAccess.createGame(newGame);  // Create + return generated ID



        System.out.println("✅ About to return CreateGameResult: gameID = " + gameID);
        return new CreateGameResult(gameID);
    }



    public void joinGame(String authToken, JoinGameRequest request) throws DataAccessException {
        // Validate token
        AuthData auth = dataAccess.getAuth(authToken);
        if (auth == null) {
            throw new DataAccessException("Error: unauthorized");
        }

        // Validate request
        if (request == null) {
            throw new DataAccessException("Error: bad request");
        }

        GameData game = dataAccess.getGame(request.gameID());
        if (game == null) {
            throw new DataAccessException("Error: bad request");
        }

        String color = request.playerColor();

        // If observing (null color), don't update game — just allow
        if (color == null) {
            return;
        }

        if (!color.equalsIgnoreCase("WHITE") && !color.equalsIgnoreCase("BLACK")) {
            throw new DataAccessException("Error: bad request");
        }

        // Check if spot already taken
        if (color.equalsIgnoreCase("WHITE") && game.whiteUsername() != null) {
            throw new DataAccessException("Error: already taken");
        }
        if (color.equalsIgnoreCase("BLACK") && game.blackUsername() != null) {
            throw new DataAccessException("Error: already taken");
        }

        // Update the game data
        GameData updatedGame = new GameData(
                game.gameID(),
                color.equalsIgnoreCase("WHITE") ? auth.username() : game.whiteUsername(),
                color.equalsIgnoreCase("BLACK") ? auth.username() : game.blackUsername(),
                game.gameName(),
                game.game()
        );

        dataAccess.updateGame(updatedGame);
    }


    public void makeMove(String authToken, int gameID, ChessMove move) throws DataAccessException {
        var auth = dataAccess.getAuth(authToken);
        if (auth == null) {
            throw new DataAccessException("unauthorized");
        }

        var game = dataAccess.getGame(gameID);
        if (game == null) {
            throw new DataAccessException("game not found");
        }

        try {
            game.game().makeMove(move);  // ✅ this works — you're calling it on the ChessGame inside GameData
        } catch (Exception e) {
            throw new DataAccessException("invalid move");
        }

        dataAccess.updateGame(game);  // Save game state after move
    }

}

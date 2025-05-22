package service;

import dataaccess.DataAccess;
import dataaccess.DataAccessException;
import model.AuthData;
import model.GameData;
import java.util.Map;
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
        if (authToken == null || gameName == null || gameName.isEmpty()) {
            throw new DataAccessException("Error: bad request");
        }

        AuthData auth = dataAccess.getAuth(authToken);
        if (auth == null) {
            throw new DataAccessException("Error: unauthorized");
        }

        int gameID = idCounter.getAndIncrement();
        GameData game = new GameData(gameID, null, null, gameName, new chess.ChessGame());

        dataAccess.createGame(game);
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
        // 1. Check if the auth token is valid
        AuthData auth = dataAccess.getAuth(authToken);
        if (auth == null) {
            throw new DataAccessException("Error: unauthorized");
        }

        // 2. Validate the game name
        if (request == null || request.gameName() == null || request.gameName().isBlank()) {
            throw new DataAccessException("Error: bad request");
        }

        // 3. Create the GameData
        int gameID = dataAccess.generateGameID();
        GameData newGame = new GameData(gameID, null, null, request.gameName(), new ChessGame());

        // 4. Store the game
        dataAccess.createGame(newGame);

        // 5. Return the result
        return new CreateGameResult(gameID);
    }

    public void joinGame(String authToken, JoinGameRequest request) throws DataAccessException {
        // Validate token
        AuthData auth = dataAccess.getAuth(authToken);
        if (auth == null) {
            throw new DataAccessException("Error: unauthorized");
        }

        // Validate request
        if (request == null || request.playerColor() == null ||
                (!request.playerColor().equalsIgnoreCase("WHITE") && !request.playerColor().equalsIgnoreCase("BLACK"))) {
            throw new DataAccessException("Error: bad request");
        }

        GameData game = dataAccess.getGame(request.gameID());
        if (game == null) {
            throw new DataAccessException("Error: bad request");
        }

        // Check if spot already taken
        if (request.playerColor().equalsIgnoreCase("WHITE") && game.whiteUsername() != null) {
            throw new DataAccessException("Error: already taken");
        }
        if (request.playerColor().equalsIgnoreCase("BLACK") && game.blackUsername() != null) {
            throw new DataAccessException("Error: already taken");
        }

        // Update the game data
        GameData updatedGame = new GameData(
                game.gameID(),
                request.playerColor().equalsIgnoreCase("WHITE") ? auth.username() : game.whiteUsername(),
                request.playerColor().equalsIgnoreCase("BLACK") ? auth.username() : game.blackUsername(),
                game.gameName(),
                game.game()
        );

        dataAccess.updateGame(updatedGame);
    }
}

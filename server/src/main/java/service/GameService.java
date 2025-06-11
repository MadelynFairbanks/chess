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

    public GameService(DataAccess dataAccess) {
        this.dataAccess = dataAccess;
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

        if (color == null || color.isBlank() ||
                !(color.equalsIgnoreCase("WHITE") || color.equalsIgnoreCase("BLACK"))) {
            throw new DataAccessException("Error: bad request");
        }


        // Check if spot already taken
        if (color != null && color.equalsIgnoreCase("WHITE") && game.whiteUsername() != null) {
            throw new DataAccessException("Error: already taken");
        }
        if (color != null && color.equalsIgnoreCase("BLACK") && game.blackUsername() != null) {
            throw new DataAccessException("Error: already taken");
        }

        // Update the game data
        GameData updatedGame = new GameData(
                game.gameID(),
                (color != null && color.equalsIgnoreCase("WHITE")) ? auth.username() : game.whiteUsername(),
                (color != null && color.equalsIgnoreCase("BLACK")) ? auth.username() : game.blackUsername(),
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

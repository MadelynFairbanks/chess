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

    // Gets the list of games if we're properly logged in
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

    // Handles creating a new game after verifying the user is legit
    public CreateGameResult createGame(String authToken, CreateGameRequest request) throws DataAccessException {
        System.out.println("✅ Entered createGame (auth + request)");
        System.out.println("authToken = " + authToken);
        System.out.println("request = " + request);

        // Gotta be logged in
        AuthData auth = dataAccess.getAuth(authToken);
        if (auth == null) {
            System.out.println("❌ Invalid auth token");
            throw new DataAccessException("Error: unauthorized");
        }

        // Game name has to exist and not be blank
        if (request == null || request.gameName() == null || request.gameName().isBlank()) {
            System.out.println("❌ Invalid game name");
            throw new DataAccessException("Error: bad request");
        }

        // Set up a new game with default values
        GameData newGame = new GameData(-1, null, null, request.gameName(), new ChessGame(), false);

        int gameID = dataAccess.createGame(newGame);  // Create + return generated ID



        System.out.println("✅ About to return CreateGameResult: gameID = " + gameID);
        return new CreateGameResult(gameID);
    }


    // Handles letting a user join a game as either black or white
    public void joinGame(String authToken, JoinGameRequest request) throws DataAccessException {

        // Token check first
        AuthData auth = dataAccess.getAuth(authToken);
        if (auth == null) {
            throw new DataAccessException("Error: unauthorized");
        }

        // Request has to actually exist
        if (request == null) {
            throw new DataAccessException("Error: bad request");
        }

        GameData game = dataAccess.getGame(request.gameID());
        if (game == null) {
            throw new DataAccessException("Error: bad request");
        }

        String color = request.playerColor();

        // Color must be either WHITE or BLACK — nothing else allowed
        if (color == null || color.isBlank() ||
                !(color.equalsIgnoreCase("WHITE") || color.equalsIgnoreCase("BLACK"))) {
            throw new DataAccessException("Error: bad request");
        }


        // Check if spot already taken
        if (color != null && color.equalsIgnoreCase("WHITE") && game.whiteUsername() != null) {
            throw new DataAccessException("Error: forbidden");
        }
        if (color != null && color.equalsIgnoreCase("BLACK") && game.blackUsername() != null) {
            throw new DataAccessException("Error: forbidden");
        }

        // Update the game with the new player added to the right side
        GameData updatedGame = new GameData(
                game.gameID(),
                (color != null && color.equalsIgnoreCase("WHITE")) ? auth.username() : game.whiteUsername(),
                (color != null && color.equalsIgnoreCase("BLACK")) ? auth.username() : game.blackUsername(),
                game.gameName(),
                game.game(),
                game.gameOver()
        );

        dataAccess.updateGame(updatedGame);
    }


    // Applies a chess move to the current game (if it’s valid, and we're authorized)
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
            game.game().makeMove(move);  // The move is applied directly to the ChessGame object
        } catch (Exception e) {
            throw new DataAccessException("invalid move"); // Could be illegal or just not your turn
        }

        dataAccess.updateGame(game);  // Save the updated game back to the DB

    }

}

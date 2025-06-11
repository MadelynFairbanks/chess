package service;

import dataaccess.MemoryDataAccess;
import dataaccess.DataAccessException;
import model.AuthData;
import model.UserData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import request.CreateGameRequest;
import request.JoinGameRequest;
import chess.ChessGame;
import model.GameData;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;



public class GameServiceTest {
    private GameService gameService;
    private String authToken;
    private MemoryDataAccess dataAccess;

    @BeforeEach
    public void setup() throws DataAccessException {
        dataAccess = new MemoryDataAccess(); // ✅ FIXED: assign to field
        gameService = new GameService(dataAccess);
        var userService = new UserService(dataAccess);
        UserData user = new UserData("eve", "pw", "e@email.com");
        AuthData auth = userService.register(user);
        authToken = auth.authToken();
    }

    @Test
    public void createGamePositive() throws Exception {
        var result = gameService.createGame(authToken, new CreateGameRequest("FunGame"));
        assertTrue(result.gameID() > 0);
    }

    @Test
    public void createGameNegativeNullName() {
        assertThrows(DataAccessException.class,
                () -> gameService.createGame(authToken, new CreateGameRequest(null)));
    }

    @Test
    public void listGamesPositive() throws Exception {
        gameService.createGame(authToken, new CreateGameRequest("TestGame"));
        List<?> games = gameService.listGames(authToken);
        assertEquals(1, games.size());
    }

    @Test
    public void listGamesNegativeBadAuth() {
        assertThrows(DataAccessException.class, () -> gameService.listGames("bad-token"));
    }

    @Test
    public void joinGamePositive() throws Exception {
        // Manually create and register a user
        UserData user = new UserData("testUser", "pass", "email@example.com");
        dataAccess.createUser(user);

        // Create and store a valid auth token
        AuthData auth = new AuthData("valid-token", user.username());
        dataAccess.createAuth(auth);

        // Create and store a game manually
        ChessGame gameLogic = new ChessGame();
        GameData game = new GameData(1, null, null, "Simple Game", gameLogic);
        dataAccess.createGame(game);

        // Create the join request
        JoinGameRequest joinRequest = new JoinGameRequest("WHITE", 1);

        // Attempt to join game
        assertDoesNotThrow(() -> gameService.joinGame("valid-token", joinRequest));
    }

    @Test
    public void joinGameNegativeInvalidColor() throws Exception {
        var createResult = gameService.createGame(authToken, new CreateGameRequest("BadColorGame"));
        var badRequest = new JoinGameRequest(authToken, createResult.gameID());
        assertThrows(DataAccessException.class, () -> gameService.joinGame(authToken, badRequest));
    }
}

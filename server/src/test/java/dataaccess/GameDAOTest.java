package dataaccess;

import model.GameData;
import chess.ChessGame;
import model.UserData;
import org.junit.jupiter.api.*;


import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class GameDAOTest {
    private MySqlDataAccess dataAccess;

    private MySqlGameDAO gameDAO;
    private MySqlUserDAO userDAO;

    @BeforeEach
    public void setup() throws DataAccessException {
        DatabaseManager.createDatabase();
        DatabaseManager.createTables();

        dataAccess = new MySqlDataAccess(); // ✅ create full data access
        gameDAO = new MySqlGameDAO();
        userDAO = new MySqlUserDAO();

        gameDAO.clear();
        userDAO.clear();

        userDAO.insertUser(new UserData("whitePlayer", "pass123", "white@example.com"));
        userDAO.insertUser(new UserData("blackPlayer", "pass456", "black@example.com"));
    }



    @Test
    public void insertAndFindGameSuccess() throws DataAccessException {
        GameData game = new GameData(1, "whitePlayer", "blackPlayer", "Test Game", new chess.ChessGame(), false);
        gameDAO.insertGame(game);

        GameData retrieved = gameDAO.findGame(1);

        assertNotNull(retrieved);
        assertEquals("Test Game", retrieved.gameName());
        assertEquals("whitePlayer", retrieved.whiteUsername());
        assertEquals("blackPlayer", retrieved.blackUsername());
    }

    @Test
    void findGameNonexistentId() throws DataAccessException {
        var game = gameDAO.findGame(99999); // assuming this ID doesn't exist
        assertNull(game);
    }

    @Test
    public void listGamesContainsInsertedGame() throws DataAccessException {
        GameData game1 = new GameData(1, "whitePlayer", "blackPlayer", "Game One", new chess.ChessGame(), false);
        GameData game2 = new GameData(2, "whitePlayer", "blackPlayer", "Game Two", new chess.ChessGame(), false);
        gameDAO.insertGame(game1);
        gameDAO.insertGame(game2);

        List<GameData> games = gameDAO.listGames();

        assertEquals(2, games.size());

        boolean foundGame1 = games.stream().anyMatch(g ->
                g.gameID() == game1.gameID() &&
                        g.gameName().equals(game1.gameName()) &&
                        g.whiteUsername().equals(game1.whiteUsername()) &&
                        g.blackUsername().equals(game1.blackUsername())
        );

        boolean foundGame2 = games.stream().anyMatch(g ->
                g.gameID() == game2.gameID() &&
                        g.gameName().equals(game2.gameName()) &&
                        g.whiteUsername().equals(game2.whiteUsername()) &&
                        g.blackUsername().equals(game2.blackUsername())
        );

        assertTrue(foundGame1, "Expected game1 was not found in listGames result.");
        assertTrue(foundGame2, "Expected game2 was not found in listGames result.");
    }

    @Test
    void listGamesEmptyInitially() throws DataAccessException {
        gameDAO.clear();
        var games = gameDAO.listGames();
        assertTrue(games.isEmpty());
    }

    @Test
    void updateGameSuccess() throws DataAccessException {
        ChessGame game = new ChessGame();
        int gameID = 987654321; // 🛑 Must be a unique ID not used elsewhere

        // Insert game with required non-null gameName
        gameDAO.insertGame(new GameData(gameID, null, null, "Original Game", game, false));

        ChessGame updatedGame = new ChessGame();
        GameData updated = new GameData(gameID, "whitePlayer", "blackPlayer", "Updated Game", updatedGame, false);

        gameDAO.updateGame(updated);

        GameData retrieved = gameDAO.findGame(gameID);

        assertEquals("Updated Game", retrieved.gameName());
        assertEquals("whitePlayer", retrieved.whiteUsername());
        assertEquals("blackPlayer", retrieved.blackUsername());
        assertNotNull(retrieved.game());
    }



    @Test
    void updateGameNonexistentId() throws DataAccessException {
        int nonexistentId = 999999999; // deliberately non-existent ID
        ChessGame game = new ChessGame();
        GameData gameData = new GameData(nonexistentId, "ghostWhite", "ghostBlack", "Ghost Game", game, false);

        // Attempt to update (should not throw)
        assertDoesNotThrow(() -> gameDAO.updateGame(gameData));

        // Ensure it wasn't silently inserted
        GameData retrieved = gameDAO.findGame(nonexistentId);
        assertNull(retrieved, "Game should not exist after updating nonexistent ID");
    }

    @Test
    void insertGameDuplicateId() {
        assertThrows(DataAccessException.class, () -> {
            GameData duplicate = new GameData(987654324, "test", null, "Dup Test", new ChessGame(), false);
            gameDAO.insertGame(duplicate);
            gameDAO.insertGame(duplicate); // second insert triggers the exception
        });
    }



}

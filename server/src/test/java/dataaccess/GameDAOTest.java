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

        dataAccess = new MySqlDataAccess();
        gameDAO = new MySqlGameDAO();
        userDAO = new MySqlUserDAO();

        gameDAO.clear();
        userDAO.clear();

        userDAO.insertUser(new UserData("whitePlayer", "pass123", "white@example.com"));
        userDAO.insertUser(new UserData("blackPlayer", "pass456", "black@example.com"));
    }

    @Test
    public void insertAndFindGameSuccess() throws DataAccessException {
        GameData game = new GameData(-1, "whitePlayer", "blackPlayer", "Test Game", new ChessGame(), false);
        int gameID = gameDAO.insertGame(game);

        GameData retrieved = gameDAO.findGame(gameID);

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
        GameData game1 = new GameData(-1, "whitePlayer", "blackPlayer", "Game One", new ChessGame(), false);
        GameData game2 = new GameData(-1, "whitePlayer", "blackPlayer", "Game Two", new ChessGame(), false);
        int id1 = gameDAO.insertGame(game1);
        int id2 = gameDAO.insertGame(game2);

        // Update objects to include real DB-assigned IDs
        game1 = new GameData(id1, game1.whiteUsername(), game1.blackUsername(), game1.gameName(), game1.game(), game1.gameOver());
        game2 = new GameData(id2, game2.whiteUsername(), game2.blackUsername(), game2.gameName(), game2.game(), game2.gameOver());

        // 🔒 Make final copies for lambda access
        final GameData finalGame1 = game1;
        final GameData finalGame2 = game2;

        List<GameData> games = gameDAO.listGames();
        assertEquals(2, games.size());

        boolean foundGame1 = games.stream().anyMatch(g ->
                g.gameID() == finalGame1.gameID() &&
                        g.gameName().equals(finalGame1.gameName()) &&
                        g.whiteUsername().equals(finalGame1.whiteUsername()) &&
                        g.blackUsername().equals(finalGame1.blackUsername())
        );

        boolean foundGame2 = games.stream().anyMatch(g ->
                g.gameID() == finalGame2.gameID() &&
                        g.gameName().equals(finalGame2.gameName()) &&
                        g.whiteUsername().equals(finalGame2.whiteUsername()) &&
                        g.blackUsername().equals(finalGame2.blackUsername())
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
        GameData original = new GameData(-1, null, null, "Original Game", game, false);
        int gameID = gameDAO.insertGame(original);

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
        int nonexistentId = 999999999;
        ChessGame game = new ChessGame();
        GameData gameData = new GameData(nonexistentId, "ghostWhite", "ghostBlack", "Ghost Game", game, false);

        assertDoesNotThrow(() -> gameDAO.updateGame(gameData));

        GameData retrieved = gameDAO.findGame(nonexistentId);
        assertNull(retrieved, "Game should not exist after updating nonexistent ID");
    }

    @Test
    void insertGameDuplicateId() {
        assertThrows(DataAccessException.class, () -> {
            GameData duplicate = new GameData(987654324, "test", null, "Dup Test", new ChessGame(), false);
            gameDAO.insertGame(duplicate);
            gameDAO.insertGame(duplicate); // second insert should fail
        });
    }
}

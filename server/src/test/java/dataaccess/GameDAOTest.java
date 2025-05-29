package dataaccess;

import model.GameData;
import model.UserData;
import org.junit.jupiter.api.*;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class GameDAOTest {

    private MySqlGameDAO gameDAO;
    private MySqlUserDAO userDAO;

    @BeforeEach
    public void setup() throws DataAccessException {
        DatabaseManager.createDatabase();
        DatabaseManager.createTables();

        gameDAO = new MySqlGameDAO();
        userDAO = new MySqlUserDAO();

        gameDAO.clear();   // Clears the games table
        userDAO.clear();   // Clears the users table

        // Insert test users
        userDAO.insertUser(new UserData("whitePlayer", "pass123", "white@example.com"));
        userDAO.insertUser(new UserData("blackPlayer", "pass456", "black@example.com"));

        // 💡 Verify they're inserted (optional debug)
        assertNotNull(userDAO.findUser("whitePlayer"));
        assertNotNull(userDAO.findUser("blackPlayer"));
    }



    @Test
    public void insertAndFindGame_success() throws DataAccessException {
        GameData game = new GameData(1, "whitePlayer", "blackPlayer", "Test Game", new chess.ChessGame());
        gameDAO.insertGame(game);

        GameData retrieved = gameDAO.findGame(1);

        assertNotNull(retrieved);
        assertEquals("Test Game", retrieved.gameName());
        assertEquals("whitePlayer", retrieved.whiteUsername());
        assertEquals("blackPlayer", retrieved.blackUsername());
    }

    @Test
    public void listGames_containsInsertedGame() throws DataAccessException {
        GameData game1 = new GameData(1, "whitePlayer", "blackPlayer", "Game One", new chess.ChessGame());
        GameData game2 = new GameData(2, "whitePlayer", "blackPlayer", "Game Two", new chess.ChessGame());
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

}

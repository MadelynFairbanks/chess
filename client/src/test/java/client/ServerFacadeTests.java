package client;

import server.Server;
import client.ServerFacade;
import model.AuthData;
import model.GameData;
import result.CreateGameResult;
import result.ListGamesResult;

import org.junit.jupiter.api.*;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class ServerFacadeTests {

    private static Server server;
    private static ServerFacade facade;

    @BeforeAll
    public static void init() {
        server = new Server();
        int port = server.run(0);
        facade = new ServerFacade(port);
        System.out.println("Started test HTTP server on " + port);
    }

    @BeforeEach
    void clearDB() throws Exception {
        facade.clear();  // Ensure clean state
    }

    @AfterAll
    static void stopServer() {
        server.stop();
    }

    @Test
    void register_positive() throws Exception {
        AuthData auth = facade.register("madelyn", "password", "madelyn@byu.edu");
        assertNotNull(auth.authToken());
        assertEquals("madelyn", auth.username());
    }

    @Test
    void registerFail_DuplicateUser() throws Exception {
        facade.register("player1", "password", "p1@email.com");
        assertThrows(Exception.class, () -> facade.register("player1", "password", "p1@email.com"));
    }

    @Test
    void loginSuccess() throws Exception {
        facade.register("testuser", "pass123", "test@byu.edu");
        AuthData auth = facade.login("testuser", "pass123");
        assertNotNull(auth.authToken());
        assertEquals("testuser", auth.username());
    }

    @Test
    void loginFail_InvalidPassword() throws Exception {
        facade.register("testuser", "correctpass", "test@byu.edu");
        assertThrows(Exception.class, () -> facade.login("testuser", "wrongpass"));
    }

    @Test
    void logoutSuccess() throws Exception {
        AuthData auth = facade.register("logoutUser", "logoutpass", "logout@byu.edu");
        facade.logout(auth.authToken());
        assertThrows(Exception.class, () -> facade.createGame(auth.authToken(), "testGameAfterLogout"));
    }

    @Test
    void logoutFail_InvalidToken() {
        assertThrows(Exception.class, () -> facade.logout("invalid-token"));
    }

    @Test
    void clearSuccess() throws Exception {
        facade.register("willClear", "pass", "clear@byu.edu");
        facade.clear();
        assertThrows(Exception.class, () -> facade.login("willClear", "pass"));
    }

    @Test
    void createGameSuccess() throws Exception {
        AuthData auth = facade.register("creator", "pass", "creator@byu.edu");
        int gameID = facade.createGame(auth.authToken(), "MyGame").gameID();
        assertTrue(gameID > 0);
    }

    @Test
    void createGameFail_BadAuth() {
        assertThrows(Exception.class, () -> facade.createGame("bad-token", "failGame"));
    }

    @Test
    void listGamesSuccess() throws Exception {
        AuthData auth = facade.register("lister", "pass", "list@byu.edu");
        facade.createGame(auth.authToken(), "GameOne");

        ListGamesResult result = facade.listGames(auth.authToken());
        List<GameData> games = result.games();

        assertFalse(games.isEmpty());
    }

    @Test
    void listGamesFail_BadAuth() {
        assertThrows(Exception.class, () -> facade.listGames("bad-token"));
    }

    @Test
    void joinGameSuccess() throws Exception {
        AuthData auth = facade.register("joiner", "pass", "join@byu.edu");
        int gameID = facade.createGame(auth.authToken(), "JoinableGame").gameID();
        facade.joinGame(auth.authToken(), gameID, "WHITE");

        List<GameData> games = facade.listGames(auth.authToken()).games();
        GameData game = games.stream()
                .filter(g -> g.gameID() == gameID)
                .findFirst()
                .orElseThrow();

        assertEquals("joiner", game.whiteUsername());
    }

    @Test
    void joinGameFail_TakenColor() throws Exception {
        AuthData auth1 = facade.register("white1", "pass", "w1@byu.edu");
        AuthData auth2 = facade.register("white2", "pass", "w2@byu.edu");
        int gameID = facade.createGame(auth1.authToken(), "DoubleWhite").gameID();

        facade.joinGame(auth1.authToken(), gameID, "WHITE");
        assertThrows(Exception.class, () -> facade.joinGame(auth2.authToken(), gameID, "WHITE"));
    }
}

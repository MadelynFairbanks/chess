package client;


import dataaccess.DataAccessException;
import exception.ResponseException;
import model.AuthData;
import model.GameData;
import model.GameID;
import org.junit.jupiter.api.*;
import server.Server;
import service.TheChessService;
import ui.ServerFacade;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;


public class ServerFacadeTests {

    private static Server server;
    private static ServerFacade sf;

    @BeforeAll
    public static void init() {
        server = new Server();
        var port = server.run(0);
        System.out.println("Started test HTTP server on " + port);
        String url = "http://localhost:" + port;
        sf = new ServerFacade(url);
    }

    @AfterAll
    static void stopServer() {
        server.stop();
        try {
            TheChessService.clear();
        } catch (DataAccessException ignored) {}
    }

    @BeforeEach
    public void clearServer() throws DataAccessException {
        try {
            TheChessService.clear();
        } catch (DataAccessException e) {
            throw new RuntimeException(e);
        }
    }


    @Test
    @Order(1)
    void registerPositiveTest() throws ResponseException {
        Random rand = new Random();
        int randomInt = rand.nextInt(1000000);
        AuthData authData = sf.register("milly" + randomInt, "password", "email");
        assertNotNull(authData.authToken());
        assertEquals("milly" + randomInt, authData.username());
    }


    @Test
    @Order(2)
    void registerNegativeTest() throws ResponseException {
        sf.register("4315fgh2345", "password", "email");
        assertThrows( ResponseException.class, () -> sf.register("4315fgh2345", "password", "email"));
    }

    @Test
    @Order(3)
    void logInPositiveTest() throws ResponseException {
        sf.register("123124342563", "password", "email");
        AuthData authData = sf.logIn("123124342563", "password");
        assertNotNull(authData.authToken());
        assertEquals("123124342563", authData.username());
    }

    @Test
    @Order(4)
    void logInNegativeTest() throws ResponseException {
        sf.register("Jdk", "password", "email");
        assertThrows( ResponseException.class, () -> sf.logIn("Jdk", "PASSWORD"));
    }

    @Test
    @Order(5)
    void listGamesPositiveTest() throws ResponseException {
        AuthData authData = sf.register("098afd", "password", "email");
        assertNotNull(sf.listGames(authData.authToken()));
    }

    @Test
    @Order(6)
    void listGamesNegativeTest() throws ResponseException {
        sf.register("08793451ihfg", "password", "email");
        assertThrows(ResponseException.class, () -> sf.listGames("123456yui"));
    }

    @Test
    @Order(7)
    void createGamePositiveTest() throws ResponseException {
        AuthData authData = sf.register("la0th", "password", "email");
        GameID gameID = sf.createGame(authData.authToken(), "coolvids");
        assertEquals(1,gameID.gameID());
    }


    @Test
    @Order(8)
    void createGameNegativeTest() throws ResponseException {
        sf.register("asdf1234346", "password", "email");
        AuthData authData = sf.logIn("asdf1234346", "password");
        assertThrows(ResponseException.class, () -> sf.createGame("123456yui", "nameristhiser"));
        assertThrows(ResponseException.class, () -> sf.createGame(authData.authToken(), ""));

    }

    @Test
    @Order(9)
    void joinGamePositiveTest() throws ResponseException {
        AuthData authData = sf.register("asdf0789", "password", "email");
        GameID gameID = sf.createGame(authData.authToken(), "coolvids");
        String gameForTesting = sf.joinGame(authData.authToken(), "white", String.valueOf(gameID.gameID()));
        assertEquals(" Game Joined Successfully ", gameForTesting);
    }


    @Test
    @Order(10)
    void joinGameNegativeTest() throws ResponseException {
        AuthData authData = sf.register("asdfasdfasdfasdf", "password", "email");
        GameID gameID = sf.createGame(authData.authToken(), "coolvids");
        String gameForTesting = sf.joinGame(authData.authToken(), "white", String.valueOf(gameID.gameID()));
        assertEquals(" Game Joined Successfully ", gameForTesting);
        assertThrows(ResponseException.class, () -> sf.joinGame(authData.authToken(), "white", String.valueOf(gameID.gameID())));

    }

    @Test
    @Order(11)
    void logOutPositiveTest() throws ResponseException {
        sf.register("1234543425", "password", "email");
        AuthData authData = sf.logIn("1234543425", "password");
        assertEquals(" Successful Logout ", sf.logOut(authData.authToken()));
    }

    @Test
    @Order(12)
    void logOutNegativeTest() throws ResponseException {
        sf.register("67654646574", "password", "email");
        sf.logIn("67654646574", "password");
        assertThrows( ResponseException.class, () -> sf.logOut("asdlfkj;"));
    }



    @Test
    @Order(13)
    void getGamePositiveTest() throws ResponseException {
        sf.register("43578945892345", "password", "email");
        AuthData authData = sf.logIn("43578945892345", "password");
        GameID gameID = sf.createGame(authData.authToken(), "coolvids");
        GameData gameData = sf.getGame(authData.authToken(), String.valueOf(gameID.gameID()));
        assertNotNull(gameData);
        assertEquals("coolvids", gameData.gameName());

    }

    @Test
    @Order(14)
    void getGameNegativeTest() throws ResponseException {
        sf.register("18768d57658758", "password", "email");
        AuthData authData = sf.logIn("18768d57658758", "password");
        GameID gameID = sf.createGame(authData.authToken(), "coolvids32");
        assertThrows( ResponseException.class, () -> sf.getGame("asdfafd;lksdjf;", String.valueOf(gameID.gameID())));


    }




}




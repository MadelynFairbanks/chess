package client;
import server.Server;
import model.AuthData;


import org.junit.jupiter.api.*;

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
    void registerSuccess() throws Exception {
        var auth = facade.register("player1", "password", "p1@email.com");
        assertNotNull(auth.authToken());
    }

    @Test
    void registerFail_DuplicateUser() throws Exception {
        facade.register("player1", "password", "p1@email.com");
        assertThrows(Exception.class, () -> facade.register("player1", "password", "p1@email.com"));
    }

}

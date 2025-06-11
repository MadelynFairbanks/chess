package server;

import spark.*;
import dataaccess.MemoryDataAccess;
import service.ClearService;
import server.RegisterHandler;
import service.UserService;
import server.CreateGameHandler;
import service.GameService;
import dataaccess.MySqlDataAccess;
import dataaccess.DataAccessException;
import dataaccess.DatabaseManager;


public class Server {

    public int run(int desiredPort) {
        Spark.port(desiredPort);
        Spark.staticFiles.location("web");

        Spark.webSocket("/ws", WebSocketHandler.class);

        try {
            DatabaseManager.createDatabase();
            DatabaseManager.createTables(); // ✅ Ensure tables exist before any endpoint runs
        } catch (DataAccessException ex) {
            ex.printStackTrace();
        }

        MySqlDataAccess mySqlData = new MySqlDataAccess();


        // Register your endpoints and handle exceptions here.

        // Clear endpoint
        ClearService clearService = new ClearService(mySqlData);
        ClearHandler clearHandler = new ClearHandler(clearService);
        Spark.delete("/db", clearHandler);

        // Register endpoint
        UserService userService = new UserService(mySqlData);
        RegisterHandler registerHandler = new RegisterHandler(userService);
        LoginHandler loginHandler = new LoginHandler(userService);
        LogoutHandler logoutHandler = new LogoutHandler(userService);

        GameService gameService = new GameService(mySqlData);
        CreateGameHandler createGameHandler = new CreateGameHandler(gameService);
        ListGamesHandler listGamesHandler = new ListGamesHandler(gameService);
        JoinGameHandler joinGameHandler = new JoinGameHandler(gameService);
        MoveHandler moveHandler = new MoveHandler(gameService);

        Spark.post("/user", registerHandler);
        Spark.post("/session", loginHandler);
        Spark.delete("/session", logoutHandler);
        Spark.post("/game", createGameHandler);
        Spark.get("/game", listGamesHandler);
        Spark.put("/game", joinGameHandler);
        Spark.post("/game/move", moveHandler);

        Spark.exception(DataAccessException.class, (ex, req, res) -> {
            res.status(500);
            res.body("{\"message\":\"Database error\"}");
        });



        //This line initializes the server and can be removed once you have a functioning endpoint
        Spark.init();

        Spark.awaitInitialization();
        return Spark.port();
    }

    public void stop() {
        Spark.stop();
        Spark.awaitStop();
    }
}

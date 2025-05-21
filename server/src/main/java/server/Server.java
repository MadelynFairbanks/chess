package server;

import spark.*;
import dataaccess.MemoryDataAccess;
import service.ClearService;
import server.RegisterHandler;
import service.UserService;

public class Server {

    public int run(int desiredPort) {
        Spark.port(desiredPort);
        Spark.staticFiles.location("web");

        MemoryDataAccess memoryData = new MemoryDataAccess();

        // Register your endpoints and handle exceptions here.

        // Clear endpoint
        ClearService clearService = new ClearService(memoryData);
        ClearHandler clearHandler = new ClearHandler(clearService);
        Spark.delete("/db", clearHandler);

        // Register endpoint
        UserService userService = new UserService(memoryData);
        RegisterHandler registerHandler = new RegisterHandler(userService);
        LoginHandler loginHandler = new LoginHandler(userService);
        LogoutHandler logoutHandler = new LogoutHandler(userService);
        
        Spark.post("/user", registerHandler);
        Spark.post("/session", loginHandler);
        Spark.delete("/session", logoutHandler);

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

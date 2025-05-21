package server;

import spark.*;
import dataaccess.MemoryDataAccess;
import service.ClearService;

public class Server {

    public int run(int desiredPort) {
        Spark.port(desiredPort);
        Spark.staticFiles.location("web");

        MemoryDataAccess memoryData = new MemoryDataAccess();
        ClearService clearService = new ClearService(memoryData);
        ClearHandler clearHandler = new ClearHandler(clearService);

        Spark.delete("/db", clearHandler);

        // Register your endpoints and handle exceptions here.

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

package server;

import com.google.gson.*;
import dataaccess.*;
import model.*;
import service.TheChessService;
import spark.*;
import websocket.WebSocketHandler;

import java.util.Map;

/**
 * 🧠 Central server setup.
 * This is the control tower, baby.
 */
public class Server {

    DataAccessInterface dataAccess;
    {
        try {
            dataAccess = new MySQLDataAccessMethods(); // 🎯 Default: go full SQL mode
        } catch (DataAccessException e) {
            throw new RuntimeException(e); // 🚨 If the DB dies, so does the server
        }
    }

    TheChessService service = new TheChessService(dataAccess); // 🧙 Service that does the magic

    /**
     * 🚀 Spin up the server on the desired port.
     */
    public int run(int desiredPort) {
        Spark.port(desiredPort);
        Spark.staticFiles.location("web");
        Spark.webSocket("/ws", WebSocketHandler.class); // 🎤 Real-time gameplay vibes

        // 🔗 REST endpoints
        Spark.delete("/db", this::clearApplication);              // 🔥 Nukes the DB
        Spark.post("/user", this::registerUser);                  // 👤 Signup
        Spark.post("/session", this::logIn);                      // 🔓 Login
        Spark.delete("/session", this::logOut);                   // 🔒 Logout
        Spark.post("/game", this::createGame);                    // 🎮 Create game
        Spark.get("/game", this::listGames);                      // 📜 List games
        Spark.put("/game", this::joinGame);                       // 🤝 Join game
        Spark.post("/gameplay", this::updateGame);                // 🔁 Update game
        Spark.post("/gameRet", this::getGame);                    // 🕵️‍♀️ Get game

        Spark.init();              // 🏁 Start your engines
        Spark.awaitInitialization();

        return Spark.port();
    }

    // --------------------------------
    // 🔧 UTILITIES
    // --------------------------------

    /**
     * 🆘 Sends error messages in JSON with the right HTTP status.
     */
    private Object returnErrorHelper(Response res, DataAccessException e) {
        int status = e.getStatus();
        if (status < 100 || status > 599) {
            status = 500;
        }
        res.status(status);
        return new Gson().toJson(Map.of(
                "message", "Error: " + e.getMessage(),
                "status", status
        ));
    }

    // --------------------------------
    // 🔨 ENDPOINT HANDLERS
    // --------------------------------

    private Object clearApplication(Request req, Response res) {
        try {
            return service.clear(); // 💣 All gone
        } catch (DataAccessException e) {
            return returnErrorHelper(res, e);
        }
    }

    private Object registerUser(Request req, Response res) {
        var body = new Gson().fromJson(req.body(), UserData.class);
        try {
            var result = service.register(body);
            return new Gson().toJson(result);
        } catch (DataAccessException e) {
            return returnErrorHelper(res, e);
        }
    }

    private Object logIn(Request req, Response res) {
        var body = new Gson().fromJson(req.body(), UserData.class);
        try {
            var result = service.logIn(body);
            return new Gson().toJson(result);
        } catch (DataAccessException e) {
            return returnErrorHelper(res, e);
        }
    }

    String authHeader = "Authorization"; // 🔐 Token highway

    private Object logOut(Request req, Response res) {
        var token = req.headers(authHeader);
        try {
            service.logOut(token);
            return "";
        } catch (DataAccessException e) {
            return returnErrorHelper(res, e);
        }
    }

    private Object listGames(Request req, Response res) {
        var token = req.headers(authHeader);
        try {
            var result = service.listGames(token);
            var jsonArray = new Gson().toJsonTree(result).getAsJsonArray();
            var wrapper = new JsonObject();
            wrapper.add("games", jsonArray);
            return new Gson().toJson(wrapper);
        } catch (DataAccessException e) {
            return returnErrorHelper(res, e);
        }
    }

    private Object joinGame(Request req, Response res) {
        var token = req.headers(authHeader);
        var body = new Gson().fromJson(req.body(), JoinGame.class);
        try {
            service.joinGame(token, body);
            return "";
        } catch (DataAccessException e) {
            return returnErrorHelper(res, e);
        }
    }

    private Object createGame(Request req, Response res) {
        var token = req.headers(authHeader);
        var body = new Gson().fromJson(req.body(), GameData.class);
        try {
            int result = service.createGame(token, body);
            return new Gson().toJson(Map.of("gameID", result));
        } catch (DataAccessException e) {
            return returnErrorHelper(res, e);
        }
    }

    private Object getGame(Request req, Response res) {
        var token = req.headers(authHeader);
        var body = new Gson().fromJson(req.body(), GameID.class);
        try {
            GameData result = service.getGame(token, body);
            return new Gson().toJson(result);
        } catch (DataAccessException e) {
            return returnErrorHelper(res, e);
        }
    }

    private Object updateGame(Request req, Response res) {
        var token = req.headers(authHeader);
        var body = new Gson().fromJson(req.body(), GameData.class);
        try {
            String result = service.updateGame(token, body);
            return new Gson().toJson(result);
        } catch (DataAccessException e) {
            return returnErrorHelper(res, e);
        }
    }

    /**
     * ✋ Stop the server when it’s time to dip.
     */
    public void stop() {
        Spark.stop();
        Spark.awaitStop();
    }
}

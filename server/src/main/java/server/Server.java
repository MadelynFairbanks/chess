package server;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import dataaccess.DataAccessException;
import dataaccess.DataAccessInterface;
import dataaccess.MemoryDataAccessMethods;
import dataaccess.MySQLDataAccessMethods;
import model.GameData;
import model.JoinGame;
import service.ChessService;
import spark.*;
import websocket.WebSocketHandler;

import java.util.Map;

public class Server {
    DataAccessInterface dataAccess;
    {
        try {
            dataAccess = new MySQLDataAccessMethods();
        } catch (DataAccessException e) {
            throw new RuntimeException(e);
        }
    }
    ChessService service = new ChessService(dataAccess);

    public int run(int desiredPort) {
        Spark.port(desiredPort);
        Spark.staticFiles.location("web");
        Spark.webSocket("/ws", WebSocketHandler.class);


        // Register your endpoints and handle exceptions here.
        String stringVarToSatisfyQualityCode = "/game";
        Spark.post("/user", this::registerUser);
        Spark.delete("/db", this::clearApplication);
        Spark.post("/session", this::logIn);
        Spark.delete("/session", this::logOut);
        Spark.get(stringVarToSatisfyQualityCode, this::listGames);
        Spark.put(stringVarToSatisfyQualityCode, this::joinGame);
        Spark.post(stringVarToSatisfyQualityCode, this::createGame);

        Spark.post("/gameplay", this::updateGame);
        Spark.post("/gameRet", this::getGame);
        //This line initializes the server and can be removed once you have a functioning endpoint 
        Spark.init();

        Spark.awaitInitialization();
        return Spark.port();
    }




    private Object returnErrorHelper (Response response, DataAccessException e ){
        int status = e.getStatus();
        if (status < 100 || status > 599) {
            status = 500;
        }
        response.status(status);
        return new Gson().toJson(Map.of("message", "Error: "+ e.getMessage(), "status", e.getStatus()));
    }

    // Handlers
    private Object clearApplication(Request request, Response response) throws DataAccessException {
        try {
            return service.clear();
        } catch (DataAccessException e ){
            return returnErrorHelper(response,e);
        }
    }



    private Object registerUser(Request request, Response response) {
        Gson serializer = new Gson();
        var registerRequest = serializer.fromJson(request.body(), model.UserData.class);
        try {
            var registerResult = service.register(registerRequest);
            return serializer.toJson(registerResult);
        } catch (DataAccessException e ) {
            return returnErrorHelper(response,e);
        }
    }



    private Object logIn(Request request, Response response) {
        Gson serializer = new Gson();
        var logInRequest = serializer.fromJson(request.body(), model.UserData.class);
        try {
            var logInResult = service.logIn(logInRequest);
            return serializer.toJson(logInResult);
        } catch (DataAccessException e ) {
            return returnErrorHelper(response,e);
        }
    }

    String authStringVarToSatisfyQualityCode = "Authorization";

    private Object logOut(Request request, Response response) {
        String token = request.headers(authStringVarToSatisfyQualityCode);
        try {
            service.logOut(token);
            return "";
        } catch (DataAccessException e ) {
            return returnErrorHelper(response,e);
        }
    }

    private Object listGames(Request request, Response response) {
        Gson serializer = new Gson();
        JsonObject jsonSerializer = new JsonObject();
        String token = request.headers(authStringVarToSatisfyQualityCode);
        try {
            var listGamesResult = service.listGames(token);
            JsonArray arrayOfGames = serializer.toJsonTree(listGamesResult).getAsJsonArray();
            jsonSerializer.add("games", arrayOfGames);
            return serializer.toJson(jsonSerializer);
        } catch (DataAccessException e ) {
            return returnErrorHelper(response,e);
        }
    }

    private Object joinGame(Request request, Response response) {
        Gson serializer = new Gson();
        String token = request.headers(authStringVarToSatisfyQualityCode);
        JoinGame joinGameRequest = serializer.fromJson(request.body(), model.JoinGame.class);
        try {
            service.joinGame(token, joinGameRequest);
            return "";
        } catch (DataAccessException e ) {
            return returnErrorHelper(response,e);
        }
    }


    private Object createGame(Request request, Response response) {
        Gson serializer = new Gson();
        String token = request.headers(authStringVarToSatisfyQualityCode);
        var createGameRequest = serializer.fromJson(request.body(), model.GameData.class);
        try {
            int createGameResult = service.createGame(token, createGameRequest);
            return serializer.toJson(Map.of("gameID", createGameResult));
        } catch (DataAccessException e ) {
            return returnErrorHelper(response,e);
        }
    }

    private Object getGame(Request request, Response response) {
        Gson serializer = new Gson();
        String token = request.headers(authStringVarToSatisfyQualityCode);
        var getGameRequest = serializer.fromJson(request.body(), model.GameID.class);
        try {
            GameData getGameResult = service.getGame(token, getGameRequest);
            return serializer.toJson(getGameResult);
        } catch (DataAccessException e ) {
            return returnErrorHelper(response,e);
        }
    }


    private Object updateGame(Request request, Response response) {
        Gson serializer = new Gson();
        String token = request.headers(authStringVarToSatisfyQualityCode);
        var updateGameRequest = serializer.fromJson(request.body(), model.GameData.class);
        try {
            String updateGameResult = service.updateGame(token, updateGameRequest);
            return serializer.toJson(updateGameResult);
        } catch (DataAccessException e ) {
            return returnErrorHelper(response,e);
        }
    }

    public void stop() {
        Spark.stop();
        Spark.awaitStop();
    }
}

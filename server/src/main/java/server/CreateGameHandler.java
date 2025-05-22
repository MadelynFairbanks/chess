package server;

import com.google.gson.Gson;
import dataaccess.DataAccessException;
import request.CreateGameRequest;
import result.CreateGameResult;
import service.GameService;
import spark.Request;
import spark.Response;
import spark.Route;

import java.util.Map;

public class CreateGameHandler implements Route {
    private final GameService service;
    private final Gson gson = new Gson();

    public CreateGameHandler(GameService service) {
        this.service = service;
    }

    @Override
    public Object handle(Request req, Response res) {
        try {
            String authToken = req.headers("authorization");
            CreateGameRequest request = gson.fromJson(req.body(), CreateGameRequest.class);
            int gameID = service.createGame(authToken, request.gameName());

            res.status(200);
            return gson.toJson(new CreateGameResult(gameID));
        } catch (DataAccessException e) {
            if (e.getMessage().contains("bad request")) {
                res.status(400);
            } else if (e.getMessage().contains("unauthorized")) {
                res.status(401);
            } else {
                res.status(500);
            }
            return gson.toJson(Map.of("message", e.getMessage()));
        } catch (Exception e) {
            res.status(500);
            return gson.toJson(Map.of("message", "Error: " + e.getMessage()));
        }
    }
}


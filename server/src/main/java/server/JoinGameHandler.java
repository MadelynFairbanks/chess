package server;

import com.google.gson.Gson;
import dataaccess.DataAccessException;
import request.JoinGameRequest;
import service.GameService;
import spark.Request;
import spark.Response;
import spark.Route;

import java.util.Map;

public class JoinGameHandler implements Route {
    private final GameService service;
    private final Gson gson = new Gson();

    public JoinGameHandler(GameService service) {
        this.service = service;
    }

    @Override
    public Object handle(Request req, Response res) {
        try {
            String authToken = req.headers("Authorization");
            JoinGameRequest request = gson.fromJson(req.body(), JoinGameRequest.class);
            service.joinGame(authToken, request);

            res.status(200);
            return "{}"; // success response
        } catch (DataAccessException e) {
            if (e.getMessage().contains("unauthorized")) {
                res.status(401);
            } else if (e.getMessage().contains("already taken")) {
                res.status(403);
            } else {
                res.status(400);
            }
            return gson.toJson(Map.of("message", e.getMessage()));
        } catch (Exception e) {
            res.status(500);
            return gson.toJson(Map.of("message", "Error: " + e.getMessage()));
        }
    }
}


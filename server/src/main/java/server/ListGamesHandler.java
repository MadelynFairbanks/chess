package server;

import com.google.gson.Gson;
import dataaccess.DataAccessException;
import model.GameData;
import result.ListGamesResult;
import service.GameService;
import spark.Request;
import spark.Response;
import spark.Route;

import java.util.List;
import java.util.Map;

public class ListGamesHandler implements Route {
    private final GameService service;
    private final Gson gson = new Gson();

    public ListGamesHandler(GameService service) {
        this.service = service;
    }

    @Override
    public Object handle(Request req, Response res) {
        try {
            String authToken = req.headers("authorization");
            List<GameData> games = service.listGames(authToken);
            res.status(200);
            return gson.toJson(new ListGamesResult(games));
        } catch (DataAccessException e) {
            res.status(401);
            return gson.toJson(Map.of("message", e.getMessage()));
        } catch (Exception e) {
            res.status(500);
            return gson.toJson(Map.of("message", "Error: " + e.getMessage()));
        }
    }
}

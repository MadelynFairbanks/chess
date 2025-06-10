package server;

import com.google.gson.Gson;
import dataaccess.DataAccessException;
import request.MoveRequest;
import result.MoveResult;
import service.GameService;
import spark.Request;
import spark.Response;
import spark.Route;

public class MoveHandler implements Route {
    private final GameService gameService;
    private final Gson gson = new Gson();

    public MoveHandler(GameService gameService) {
        this.gameService = gameService;
    }

    @Override
    public Object handle(Request req, Response res) throws Exception {
        MoveRequest moveRequest = gson.fromJson(req.body(), MoveRequest.class);

        try {
            gameService.makeMove(moveRequest.authToken(), moveRequest.gameID(), moveRequest.move());
            res.status(200);
            return gson.toJson(new MoveResult("Move successful."));
        } catch (DataAccessException e) {
            res.status(400);
            return gson.toJson(new MoveResult("Error: " + e.getMessage()));
        }
    }
}

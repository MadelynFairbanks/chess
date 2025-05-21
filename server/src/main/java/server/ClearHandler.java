package server;

import com.google.gson.Gson;
import dataaccess.DataAccessException;
import service.ClearService;
import spark.Request;
import spark.Response;
import spark.Route;

import java.util.Map;

public class ClearHandler implements Route {
    private final ClearService service;

    public ClearHandler(ClearService service) {
        this.service = service;
    }

    @Override
    public Object handle(Request request, Response response) {
        try {
            service.clearApplication();
            response.status(200);
            return "{}";
        } catch (DataAccessException e) {
            response.status(500);
            return new Gson().toJson(Map.of("message", "Error: " + e.getMessage()));
        }
    }
}

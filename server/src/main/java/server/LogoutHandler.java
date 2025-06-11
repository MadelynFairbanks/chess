package server;

import com.google.gson.Gson;
import dataaccess.DataAccessException;
import service.UserService;
import spark.Request;
import spark.Response;
import spark.Route;

import java.util.Map;

public class LogoutHandler implements Route {
    private final UserService service;
    private final Gson gson = new Gson();

    public LogoutHandler(UserService service) {
        this.service = service;
    }

    @Override
    public Object handle(Request req, Response res) {
        try {
            String authToken = req.headers("authorization");
            service.logout(authToken);
            res.status(200);
            return gson.toJson(Map.of());
        } catch (DataAccessException e) {
            String msg = e.getMessage();
            String lower = msg.toLowerCase();

            if (lower.contains("unauthorized")) {
                res.status(401);
            } else if (lower.contains("internal server error")) {
                res.status(500);
            } else {
                res.status(500);
            }

            if (!msg.toLowerCase().startsWith("error")) {
                msg = "Error: " + msg;
            }

            return gson.toJson(Map.of("message", msg));
        } catch (Exception e) {
            res.status(500);
            return gson.toJson(Map.of("message", "Error: " + e.getMessage()));
        }
    }
}

package server;

import com.google.gson.Gson;
import dataaccess.DataAccessException;
import model.AuthData;
import model.UserData;
import request.RegisterRequest;
import result.RegisterResult;
import service.UserService;
import spark.Request;
import spark.Response;
import spark.Route;

import java.util.Map;

public class RegisterHandler implements Route {
    private final UserService service;
    private final Gson gson = new Gson();

    public RegisterHandler(UserService service) {
        this.service = service;
    }

    @Override
    public Object handle(Request req, Response res) {
        try {
            RegisterRequest request = gson.fromJson(req.body(), RegisterRequest.class);

            if (request == null ||
                    request.username() == null || request.username().isBlank() ||
                    request.password() == null || request.password().isBlank() ||
                    request.email() == null || request.email().isBlank()) {
                res.status(400);
                return gson.toJson(Map.of("message", "Error: bad request"));
            }

            UserData user = new UserData(request.username(), request.password(), request.email());
            AuthData auth = service.register(user);

            res.status(200);
            return gson.toJson(new RegisterResult(auth.username(), auth.authToken()));
        } catch (DataAccessException e) {
            String msg = e.getMessage();
            String lower = msg.toLowerCase();

            if (lower.contains("bad request")) {
                res.status(400);
            } else if (lower.contains("already taken")) {
                res.status(403);
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

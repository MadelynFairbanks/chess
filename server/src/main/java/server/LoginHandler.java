package server;

import com.google.gson.Gson;
import dataaccess.DataAccessException;
import model.AuthData;
import model.UserData;
import request.LoginRequest;
import result.LoginResult;
import service.UserService;
import spark.Request;
import spark.Response;
import spark.Route;

import java.util.Map;

public class LoginHandler implements Route {
    private final UserService service;
    private final Gson gson = new Gson();

    public LoginHandler(UserService service) {
        this.service = service;
    }

    @Override
    public Object handle(Request req, Response res) {
        try {
            LoginRequest loginRequest = gson.fromJson(req.body(), LoginRequest.class);
            UserData user = new UserData(loginRequest.username(), loginRequest.password(), null); // email is null
            AuthData auth = service.login(user);

            res.status(200);
            return gson.toJson(new LoginResult(auth.username(), auth.authToken()));
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


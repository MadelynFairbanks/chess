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
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.Map;

public class LoginHandler implements Route {
    private final UserService userService;
    private final Gson gson = new Gson();

    public LoginHandler(UserService userService) {
        this.userService = userService;
    }

    @Override
    public Object handle(Request req, Response res) {
        try {
            Type type = new TypeToken<Map<String, String>>(){}.getType();
            Map<String, String> jsonMap = gson.fromJson(req.body(), type);
            String username = jsonMap.get("username");
            String password = jsonMap.get("password");

            if (!jsonMap.containsKey("username") || !jsonMap.containsKey("password") ||
                    username == null || username.trim().isEmpty() ||
                    password == null || password.trim().isEmpty()) {
                res.status(400);
                return gson.toJson(Map.of("message", "Error: bad request"));
            }

            AuthData auth = userService.login(new UserData(username, password, null));
            res.status(200);
            return gson.toJson(new LoginResult(auth.username(), auth.authToken()));

        } catch (DataAccessException e) {
            String msg = e.getMessage();
            String lower = msg.toLowerCase();

            if (lower.contains("unauthorized")) {
                res.status(401);
            } else if (lower.contains("already taken")) {
                res.status(403);
            } else if (lower.contains("bad request")) {
                res.status(400);
            } else if (lower.contains("sql") || lower.contains("database") || lower.contains("connection")) {
                res.status(500);
            } else {
                res.status(500); // Default fallback
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

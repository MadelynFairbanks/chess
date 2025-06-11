package server;

import com.google.gson.Gson;
import spark.Response;

import java.util.Map;

public class JsonResponse {
    private static final Gson GSON = new Gson();

    public static String success(Response res, int statusCode, Object body) {
        res.status(statusCode);
        return GSON.toJson(body);
    }

    public static String error(Response res, String message) {
        String msg = message != null ? message : "Unknown error";
        String lower = msg.toLowerCase();

        if (!lower.startsWith("error")) {
            msg = "Error: " + msg;
        }

        if (lower.contains("bad request")) {
            res.status(400);
        } else if (lower.contains("unauthorized")) {
            res.status(401);
        } else if (lower.contains("already taken")) {
            res.status(403);
        } else {
            res.status(500);
        }

        return GSON.toJson(Map.of("message", msg));
    }

    public static String exception(Response res, Exception e) {
        res.status(500);
        return GSON.toJson(Map.of("message", "Error: " + e.getMessage()));
    }
}


package client;

import java.io.*;
import java.net.*;
import com.google.gson.Gson;
import request.*;
import result.*;
import model.*;

public class ServerFacade {
    private final String serverUrl;
    private final Gson gson = new Gson();

    public ServerFacade(int port) {
        this.serverUrl = "http://localhost:" + port;
    }

    public AuthData register(String username, String password, String email) throws Exception {
        var request = new RegisterRequest(username, password, email);
        return makeRequest("POST", "/user", request, AuthData.class, null);
    }

    public AuthData login(String username, String password) throws Exception {
        var request = new LoginRequest(username, password);
        return makeRequest("POST", "/session", request, AuthData.class, null);
    }

    public void logout(String authToken) throws Exception {
        makeRequest("DELETE", "/session", null, Void.class, authToken);
    }

    public void clear() throws Exception {
        makeRequest("DELETE", "/db", null, Void.class, null);
    }

    public CreateGameResult createGame(String authToken, String gameName) throws Exception {
        var request = new CreateGameRequest(gameName);
        return makeRequest("POST", "/game", request, CreateGameResult.class, authToken);
    }

    public ListGamesResult listGames(String authToken) throws Exception {
        return makeRequest("GET", "/game", null, ListGamesResult.class, authToken);
    }

    public void joinGame(String authToken, int gameID, String playerColor) throws Exception {
        var request = new JoinGameRequest(playerColor, gameID); // ✅ match record order
        makeRequest("PUT", "/game", request, Void.class, authToken);
    }


    private <T> T makeRequest(String method, String path, Object body, Class<T> responseClass, String authToken) throws Exception {
        URL url = new URL(serverUrl + path);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod(method);
        connection.setDoOutput(true);
        connection.setRequestProperty("Accept", "application/json");
        connection.setRequestProperty("Content-Type", "application/json");

        // ✅ Add auth token if present
        if (authToken != null) {
            connection.setRequestProperty("Authorization", authToken);
        }

        if (body != null) {
            String json = gson.toJson(body);
            try (OutputStream os = connection.getOutputStream()) {
                os.write(json.getBytes());
            }
        }

        int responseCode = connection.getResponseCode();
        if (responseCode >= 200 && responseCode < 300) {
            if (responseClass == Void.class) return null;
            try (InputStreamReader reader = new InputStreamReader(connection.getInputStream())) {
                return gson.fromJson(reader, responseClass);
            }
        } else {
            throw new RuntimeException("HTTP " + responseCode);
        }
    }
}

package ui;


import java.io.*;

import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.util.List;


import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import exception.ResponseException;
import model.*;

public class ServerFacade {
    private final String serverUrl;
    private static final String GAME = "/game";


    public ServerFacade(String url) {
        serverUrl = url;
    }

    public AuthData register(String... params) throws ResponseException  {
        String username = params[0];
        String password = params[1];
        String email    = params[2];
        UserData userData = new UserData(username,password,email);
        try {
            return this.makeRequest("POST", "/user", null, userData, AuthData.class);
        }catch (ResponseException | NullPointerException e){
            throw new ResponseException(404, "User Already Exists, Please Use a Different Username");
        }
    }

    public AuthData logIn(String... params) throws ResponseException {
        String username = params[0];
        String password = params[1];
        UserData userData = new UserData(username,password,null);
        try{
            return this.makeRequest("POST", "/session", null, userData, AuthData.class);
        }catch (ResponseException | NullPointerException e){
            throw new ResponseException(404, "Invalid Credential, Please try again.");
        }
    }


    public List<GameList> listGames(String authToken) throws ResponseException, NullPointerException{
        JsonObject response = this.makeRequest("GET", GAME, authToken, null,  JsonObject.class);
        JsonArray gamesArray;
        gamesArray = response.getAsJsonArray("games");
        return new Gson().fromJson(gamesArray, new TypeToken<List<GameList>>(){}.getType());
    }

    public GameID createGame(String authToken, String... params) throws ResponseException {
        var gameName = new GameCreationRequest(params[0]);
        try{
            return this.makeRequest("POST", GAME, authToken, gameName, GameID.class);
        }catch (ResponseException | NullPointerException e){
            throw new ResponseException(404, "Invalid Game Creation, Please try again.");
        }
    }


    public String joinGame(String authToken, String... params) throws ResponseException {
        String color;
        if(params[0].equals("white")){
            color = "WHITE";
        }else if(params[0].equals("black")){
            color = "BLACK";
        } else {
            return "Invalid Color Given, please try again";
        }
        var joinRequest = new JoinGame(color ,Integer.parseInt(params[1]));
        try {
            this.makeRequest("PUT", GAME, authToken, joinRequest, Void.class);
        }catch (ResponseException | NullPointerException e){
            throw new ResponseException(404, "Game Play Position Taken or Game does not exist, Please try again.");
        }

        return " Game Joined Successfully ";
    }

    public String logOut(String authToken) throws ResponseException {
        this.makeRequest("DELETE", "/session", authToken, null, Void.class);
        return " Successful Logout ";
    }



    public GameData getGame(String authToken, String... params) throws ResponseException {
        GameID gameID = new GameID(Integer.parseInt(params[0]));
        return this.makeRequest("POST", "/gameRet", authToken, gameID, GameData.class);
    }





    private <T> T makeRequest(String method, String path, String authToken, Object request, Class<T> responseClass) throws ResponseException {
        try {
            URL url = (new URI(serverUrl + path)).toURL();
            HttpURLConnection http = (HttpURLConnection) url.openConnection();
            http.setRequestMethod(method);
            http.setDoOutput(true);
            if (authToken != null) {
                http.setRequestProperty("Authorization", authToken);
            }
            writeBody(request, http);
            http.connect();
            throwIfNotSuccessful(http);
            return readBody(http, responseClass);
        } catch (RuntimeException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new ResponseException(500, ex.getMessage());
        }
    }


    private static void writeBody(Object request, HttpURLConnection http) throws IOException {
        if (request != null) {
            http.addRequestProperty("Content-Type", "application/json");
            String reqData = new Gson().toJson(request);
            try (OutputStream reqBody = http.getOutputStream()) {
                reqBody.write(reqData.getBytes());
            }
        }
    }

    private static <T> T readBody(HttpURLConnection http, Class<T> responseClass) throws IOException {
        T response = null;
        if (http.getContentLength() < 0) {
            try (InputStream respBody = http.getInputStream()) {
                InputStreamReader reader = new InputStreamReader(respBody);
                if (responseClass != null) {
                    response = new Gson().fromJson(reader, responseClass);
                }
            }
        }
        return response;
    }

    private void throwIfNotSuccessful(HttpURLConnection http) throws ResponseException, IOException {
        var status = http.getResponseCode();
        if (!isSuccessful(status)) {
            try (InputStream respErr = http.getErrorStream()) {
                if (respErr != null) {
                    throw ResponseException.fromJson(respErr);
                }
            }

            throw new ResponseException(500,"not implemented but yes, it is an error in server facade");
        }
    }

    private boolean isSuccessful(int status) {
        return status / 100 == 2;
    }



}
package ui.client;

import exception.ResponseException;
import model.AuthData;
import ui.ServerFacade;

import java.util.Arrays;

public class PreLogInClient {

    private ServerFacade server;
    private String serverUrl;

    public static String authToken;

    public  PreLogInClient(String serverUrl){
        server = new ServerFacade(serverUrl);
        this.serverUrl = serverUrl;
    }

    public static String getAuthToken() {
        return authToken;
    }

    public String eval(String input) {
        try {
            var tokens = input.toLowerCase().split(" ");
            var cmd = (tokens.length > 0) ? tokens[0] : "help";
            var params = Arrays.copyOfRange(tokens, 1, tokens.length);
            return switch (cmd) {
                case "register", "r" -> register(params);
                case "login", "l" -> logIn(params);
                case "quit", "q" -> "quit";
                default -> help();
            };
        } catch (Exception ex) {
            return ex.getMessage();
        }
    }

    public String register(String... params){
        if (params.length != 3){
            System.out.println("Invalid Registration, Please Try Again");
            return help();
        }
        try {
            AuthData authData = server.register(params);
            authToken = authData.authToken();
            return "Successful registration!!";

        } catch (ResponseException e){
            return e.getMessage();
        }

    }

    private String logIn(String... params) {
        if (params.length != 2){
            System.out.println("Invalid Login, Please Try Again");
            return help();
        }
        try {
            AuthData authData = server.logIn(params);
            authToken = authData.authToken();
            return "Successful login!!";

        } catch (ResponseException e){
            return e.getMessage();
        }
    }



    public String help(){
        return   """
                        Available commands:
                         Log In as Existing User - "login" , "l" <Username> <Password>
                         Register as a New User  - "register", "r" <Username> <Password> <Email>
                         Print Help Message      - "help", "h"
                         Exit Program            - "quit", "q"
                                
                """;

    }
}

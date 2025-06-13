package ui.client;

import exception.ResponseException;
import model.AuthData;
import ui.ServerFacade;

import java.util.Arrays;

public class OurPreLogInClient {

    private ServerFacade server;
    private String serverUrl;

    // 🔐 Global auth token once user logs in or registers
    public static String authToken;

    // 🚪 Constructor — setting up the server connection
    public OurPreLogInClient(String serverUrl) {
        server = new ServerFacade(serverUrl);
        this.serverUrl = serverUrl;
    }

    // 🧾 Get the saved auth token — so we know who's boss
    public static String getAuthToken() {
        return authToken;
    }

    // 🎮 Command interpreter (but like, pre-game lobby edition)
    public String eval(String input) {
        try {
            var tokens = input.toLowerCase().split(" ");
            var cmd = (tokens.length > 0) ? tokens[0] : "help";
            var params = Arrays.copyOfRange(tokens, 1, tokens.length);

            return switch (cmd) {
                case "register", "r" -> register(params);  // 📝 Sign up!
                case "login", "l"    -> logIn(params);     // 🔑 Enter the chat
                case "quit", "q"     -> "quit";            // 👋 Goodbye cruel world
                default              -> help();            // 🤔 You lost?
            };

        } catch (Exception ex) {
            return ex.getMessage();  // 💥 Something blew up
        }
    }

    // 📝 Register a new user (welcome to the squad)
    public String register(String... params) {
        if (params.length != 3) {
            System.out.println("🚫 Invalid registration. Try again 🫠");
            return help();
        }

        try {
            AuthData authData = server.register(params);
            authToken = authData.authToken();
            return "🎉 Successful registration!";
        } catch (ResponseException e) {
            return e.getMessage();
        }
    }

    // 🔑 Log in an existing user (you better know that password)
    private String logIn(String... params) {
        if (params.length != 2) {
            System.out.println("🚫 Invalid login. Try again 😬");
            return help();
        }

        try {
            AuthData authData = server.logIn(params);
            authToken = authData.authToken();
            return "🎉 Successful login!";
        } catch (ResponseException e) {
            return e.getMessage();
        }
    }

    // 📜 Handy little help menu for those in distress
    public String help() {
        return """
               Available commands:
                   Log In as Existing User  - "login", "l" <Username> <Password>
                   Register as a New User   - "register", "r" <Username> <Password> <Email>
                   Print Help Message       - "help", "h"
                   Exit Program             - "quit", "q"
               """;
    }
}

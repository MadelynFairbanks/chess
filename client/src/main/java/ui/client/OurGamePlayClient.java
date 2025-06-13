package ui.client;

import exception.ResponseException;
import model.GameID;
import ui.websocket.NotificationHandler;
import ui.websocket.WebSocketFacade;

import java.io.IOException;
import java.util.Arrays;
import java.util.Scanner;

public class OurGamePlayClient {

    private String serverUrl;  // 🛰️ where our server lives
    private final NotificationHandler notificationHandler;  // 🔔 for spicy game updates
    private WebSocketFacade webSocket;  // 🌐 our lil portal to the chess multiverse

    // 🚪 constructor — setting up the vibes
    public OurGamePlayClient(String serverUrl, NotificationHandler notificationHandler) {
        this.serverUrl = serverUrl;
        this.notificationHandler = notificationHandler;
    }

    // 🎮 command interpreter: this is where the magic begins
    public String eval(String input, String authToken) {
        try {
            var tokens = input.toLowerCase().split(" ");  // 📦 break it down
            var cmd = (tokens.length > 0) ? tokens[0] : "help";
            var params = Arrays.copyOfRange(tokens, 1, tokens.length);

            // 🔁 match the command with the correct action
            return switch (cmd) {
                case "m", "move", "make"     -> makeMove(authToken, params);            // ♟️ move that pawn, queen
                case "r", "redraw"           -> redrawBoard(authToken);                 // 🎨 refresh the masterpiece
                case "resign"                -> resign(authToken);                      // 🚩 wave the white flag
                case "leave"                 -> leave(authToken);                       // 🏃 dip out of the game
                case "hl", "highlight"       -> highlight(authToken, params);           // ✨ where can I go tho?
                case "quit", "q"             -> "quit";                                 // ❌ bounce from the app
                default                      -> help();                                 // 📜 when in doubt, read the manual
            };

        } catch (Exception ex) {
            return ex.getMessage();  // 💥 if it breaks, tell me why
        }
    }

    // 🎉 connect to the game and start playin’
    public void joinGame(String authToken, GameID gameID) throws ResponseException {
        webSocket = new WebSocketFacade(serverUrl, notificationHandler, gameID);
        webSocket.joinGame(authToken, gameID);  // 📞 ring ring! let me in!
    }

    // ♟️ attempt to move a piece... or crash trying
    public String makeMove(String authToken, String... params) throws ResponseException, IOException {
        if (params.length < 2 || params.length > 3) {
            System.out.println("🤨 Invalid move. Try again (source destination [promotion])");
            return "help";
        }
        return webSocket.makeMove(authToken, params);
    }

    // 🖼️ redraw the board like it's a Bob Ross painting
    public String redrawBoard(String authToken) {
        webSocket.redrawBoard(authToken);
        return "";
    }

    // 😭 if you’re donezo, time to resign
    public String resign(String authToken) throws ResponseException {
        System.out.print("Are you sure you want to resign? Y/N: ");
        String input = new Scanner(System.in).nextLine().toLowerCase();
        var response = input.split(" ")[0];

        if (response.equals("y") || response.equals("yes")) {
            webSocket.resign(authToken);  // 🫡 respect, soldier
        }

        return "";
    }

    // 🏃 leave the game like a ghost — poof
    public String leave(String authToken) throws ResponseException {
        webSocket.leave(authToken);
        return "quit";
    }

    // ✨ get those little blue highlights showing where you can flex
    public String highlight(String authToken, String... params) {
        if (params.length == 0) {
            return "Missing position to highlight 😬";
        }
        webSocket.highlight(authToken, params[0]);
        return "";
    }

    // 📜 your cheat sheet for surviving chess life
    public String help() {
        return """
               Options:
                   Highlight legal moves: "hl", "highlight"  <position> (e.g. f5)
                   Make a move: "m", "move", "make"           <source> <destination> [promotion] (e.g. f5 e4 q)
                   Redraw the chess board: "r", "redraw"
                   Resign from the game: "resign"
                   Leave the game: "leave"
               """;
    }
}

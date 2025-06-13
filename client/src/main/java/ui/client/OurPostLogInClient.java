package ui.client;

import exception.ResponseException;
import model.GameData;
import model.GameID;
import model.GameList;
import ui.ServerFacade;
import ui.websocket.WebSocketFacade;

import java.util.*;

public class OurPostLogInClient {

    // 🌐 WebSocket for in-game actions
    private WebSocketFacade ws;

    // 🧠 Used to map list number -> actual game ID
    public static Map<Integer, Integer> listNumberInterpreter;

    private ServerFacade server;
    private String serverUrl;

    // 🎯 The game we’ve currently joined
    public GameID gameID = new GameID(0);

    // 🎨 What color are we playing as (white/black)
    public static String color;

    // 🏗️ Constructor: We’re in, let’s go
    public OurPostLogInClient(String serverUrl) {
        server = new ServerFacade(serverUrl);
        this.serverUrl = serverUrl;
        listNumberInterpreter = new HashMap<>();
    }

    public GameID getGameID() {
        return this.gameID;
    }

    // 🎮 Main command interpreter
    public String eval(String input, String authToken) {
        try {
            var tokens = input.toLowerCase().split(" ");
            var cmd = (tokens.length > 0) ? tokens[0] : "help";
            var params = Arrays.copyOfRange(tokens, 1, tokens.length);

            return switch (cmd) {
                case "l", "list"    -> listGames(authToken);         // 📃 show available games
                case "c", "create"  -> createGame(authToken, params); // ✏️ make a new one
                case "j", "join"    -> joinGame(authToken, params);   // 🧑‍🤝‍🧑 join a squad
                case "w", "watch"   -> watchGame(authToken, params);  // 👀 be nosy
                case "logout"       -> logOut(authToken);             // 🚪 peace out
                case "quit", "q"    -> "quit";                         // ❌ exit
                default             -> help();                         // 📜 help me please
            };

        } catch (Exception ex) {
            return ex.getMessage();
        }
    }

    // 🔄 Preps the game list mapping (like a dropdown IRL)
    private void setListNumberInterpreter(String authToken) throws ResponseException {
        List<GameList> gameList = server.listGames(authToken);
        int i = 0;
        for (GameList game : gameList) {
            i++;
            listNumberInterpreter.put(i, game.gameID());
        }
    }

    // 📃 List all existing games — fancy style
    private String listGames(String authToken) {
        try {
            List<GameList> gameList = server.listGames(authToken);

            if (gameList.isEmpty()) {
                return "No Games Currently Created 😔";
            } else {
                StringBuilder uiList = new StringBuilder("Games:");
                int i = 0;

                for (GameList game : gameList) {
                    i++;
                    listNumberInterpreter.put(i, game.gameID());

                    String white = (game.whiteUsername() != null) ? game.whiteUsername() : "Empty";
                    String black = (game.blackUsername() != null) ? game.blackUsername() : "Empty";

                    uiList.append("\n")
                            .append(i).append(". ")
                            .append("\tGame Name: ").append(game.gameName())
                            .append("\t\t White: ").append(white)
                            .append("\tBlack: ").append(black);
                }

                return uiList.toString();
            }

        } catch (ResponseException e) {
            return e.getMessage();
        }
    }

    // ✏️ Make a new game with a cute lil name
    private String createGame(String authToken, String... params) {
        if (params.length != 1) {
            System.out.println("🚫 Invalid Game Name, Try Again");
            return help();
        }

        try {
            GameID newGame = server.createGame(authToken, params);
            if (newGame.gameID() > 0) {
                return "Game Created 🎉";
            }
        } catch (ResponseException e) {
            return e.getMessage();
        }

        return "Failure to create Game 💀";
    }

    // 👀 Just watching the chaos unfold
    private String watchGame(String authToken, String... params) {
        if (params.length != 1) {
            System.out.println("🚫 Invalid Game Input, Try Again");
            return help();
        }

        try {
            if (listNumberInterpreter == null) {
                setListNumberInterpreter(authToken);
            }
        } catch (Exception ignored) {}

        params[0] = String.valueOf(listNumberInterpreter.get(Integer.parseInt(params[0])));

        if (params[0] == null || params[0].equals("0")) {
            System.out.println("🚫 Game does not exist");
            return help();
        }

        gameID = new GameID(Integer.parseInt(params[0]));

        try {
            server.getGame(authToken, params[0]);
            return " Game Joined Successfully! 👀";
        } catch (ResponseException e) {
            return " Failure to Join Game 🫠";
        }
    }

    // 🧑‍🤝‍🧑 Tryna join as white or black
    private String joinGame(String authToken, String... params) {
        if (params.length != 2) {
            System.out.println("🚫 Invalid Join Input, Try Again");
            return help();
        }

        try {
            setListNumberInterpreter(authToken);
            params[1] = String.valueOf(listNumberInterpreter.get(Integer.parseInt(params[1])));

            if (params[1].equals("null")) {
                System.out.println("🚫 Game does not exist");
                return help();
            }
        } catch (Exception ignored) {}

        try {
            String result = server.joinGame(authToken, params);

            if (Objects.equals(result, " Game Joined Successfully ")) {
                GameData gameInfo = server.getGame(authToken, params[1]);
                this.gameID = new GameID(gameInfo.gameID());
                color = params[0]; // 🎨 Set our vibe
                return " Game Joined Successfully! 🎉";
            } else if (Objects.equals(result, "Invalid Color Given, please try again")) {
                return "Invalid Color Given, please try again 💅";
            }

        } catch (ResponseException e) {
            return "Failure to Join Game: " + e.getMessage();
        }

        return "Failure to Join Game 💀";
    }

    // 🚪 Get outta here (logout)
    private String logOut(String authToken) {
        try {
            if (Objects.equals(server.logOut(authToken), " Successful Logout ")) {
                return "GOODBYE!!! 👋";
            }
        } catch (ResponseException e) {
            return "Logout Failed: " + e.getMessage();
        }

        return "Logout Failed 😞";
    }

    // 📜 Just in case you forgot how to live
    public String help() {
        return """
               Options:
                   List current games:   "l", "list"
                   Create a new game:    "c", "create" <GAME NAME>
                   Join a game:          "j", "join" <WHITE/BLACK> <GAME ID>
                   Watch a game:         "w", "watch" <GAME ID>
                   Logout:               "logout"
               """;
    }
}

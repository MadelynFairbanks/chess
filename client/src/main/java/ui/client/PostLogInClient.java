package ui.client;

import exception.ResponseException;
import model.GameData;
import model.GameID;
import model.GameList;
import ui.DrawBoard;
import ui.ServerFacade;
import ui.LoadBoard;
import ui.websocket.NotificationHandler;
import ui.websocket.WebSocketFacade;

import java.util.*;

public class PostLogInClient {
    private WebSocketFacade ws;


    public static Map<Integer, Integer> listNumberInterpreter;
    private ServerFacade server;
    private String serverUrl;
    public GameID gameID = new GameID(0);
    public  static String color;



    public  PostLogInClient(String serverUrl){
        server = new ServerFacade(serverUrl);
        this.serverUrl = serverUrl;
        listNumberInterpreter = new HashMap<>();
    }

    public GameID getGameID() {
        return this.gameID;
    }


    public String eval(String input, String authToken) {
        try {
            var tokens = input.toLowerCase().split(" ");
            var cmd = (tokens.length > 0) ? tokens[0] : "help";
            var params = Arrays.copyOfRange(tokens, 1, tokens.length);
            return switch (cmd) {
                case "l", "list" -> listGames(authToken);
                case "c", "create" -> createGame(authToken, params);
                case "j", "join" -> joinGame(authToken, params);
                case "w", "watch" -> watchGame(authToken, params);
                case "logout" -> logOut(authToken);
                case "quit", "q" -> "quit";
                default -> help();
            };
        } catch (Exception ex) {
            return ex.getMessage();
        }
    }

    private void setListNumberInterpreter (String authToken) throws ResponseException {
        List<GameList> gameList = server.listGames(authToken);
        int i = 0;
        for (GameList game : gameList) {
            i++;
            listNumberInterpreter.put(i, game.gameID());
        }
    }


    private String listGames(String authToken) {
        try {

            List<GameList> gameList = server.listGames(authToken);
            if (gameList.isEmpty()) {
                return "No Games Currently Created";
            }else{
                StringBuilder uiList = new StringBuilder("Games: ");
                int i = 0;
                for(GameList game: gameList){
                    i++;
                    listNumberInterpreter.put(i, game.gameID());
                    String white = "Empty";
                    String black = "Empty";
                    if(game.whiteUsername() != null){
                        white = game.whiteUsername();
                    }
                    if (game.blackUsername() != null){
                        black = game.blackUsername();
                    }
                    uiList = new StringBuilder((uiList +
                            "\n" + i + ". " +
                            "\tGame Name: " + game.gameName()) +
                            "\t\t White: " + white +
                            "\tBlack: " + black);
                }
                return uiList.toString();
            }
        } catch (ResponseException e){
            return e.getMessage();
        }

    }


    private String createGame(String authToken, String... params) {
        if (params.length != 1){
            System.out.println("Invalid Game Name, Please Try Again");
            return help();
        }
        try {
            GameID gameID = server.createGame(authToken, params);
            if (gameID.gameID() > 0 ) {
                return "Game Created";
            }

        } catch (ResponseException e){
            return e.getMessage();
        }
        return "Failure to create Game";
    }



    private String watchGame(String authToken, String... params) {
        if (params.length != 1){
            System.out.println("Invalid Game Input, Please Try Again");
            return help();
        }

        try {
            if(listNumberInterpreter == null){
                setListNumberInterpreter(authToken);
            }
        } catch (Exception ignored){}
        params[0] = String.valueOf(listNumberInterpreter.get(Integer.parseInt(params[0])));

        if(params[0].equals("0") || params[0] == null){
            System.out.println("Invalid Game Input, Game Does Not Exist Please Try Again");
            return help();
        }
        gameID = new GameID(Integer.parseInt(params[0]));

        try {
            server.getGame(authToken, params[0]);

            return " Game Joined Successfully! ";
        } catch (ResponseException e) {
            return " Failure to Join Game " ;
        }
    }


    private String joinGame(String authToken, String... params) {
        if (params.length != 2){
            System.out.println("Invalid Game Input, Please Try Again");
            return help();
        }
        try {
            setListNumberInterpreter(authToken);
            params[1] = String.valueOf(listNumberInterpreter.get(Integer.parseInt(params[1])));
            if(params[1].equals("null")){
                System.out.println("Invalid Game Input, Game Does Not Exist Please Try Again");
                return help();
            }
        } catch (Exception ignored){}

        try {
            String result = server.joinGame(authToken, params);


            if (Objects.equals(result, " Game Joined Successfully ")){

                GameData gameInfo = server.getGame(authToken, params[1]);
                this.gameID = new GameID(gameInfo.gameID());
                this.color = params[0];
                return " Game Joined Successfully! ";

            } else if(Objects.equals(result, "Invalid Color Given, please try again")){
                return "Invalid Color Given, please try again";
            }
        } catch (ResponseException e) {
            return "Failure to Join Game " + e.getMessage();
        }
        return "Failure to Join Game ";
    }




    private String logOut(String authToken) {

        try {
            if (Objects.equals(server.logOut(authToken), " Successful Logout ")){
                return " GOODBYE!!! ";
            }
        } catch (ResponseException e) {
            return "Failure to Join Game " + e.getMessage();
        }
        return "Failure to Join Game ";
    }



    public String help(){
        return   """
                        Options:
                        List current games:  "l", "list"
                        Create a new game:   "c", "create" ‹GAME NAME>
                        Join a game:         "j", "join" ‹WHITE/BLACK›‹GAME ID>
                        Watch a game:        "w", "watch" <GAME ID>
                        Logout:              "logout"
                                
                """;

    }
}

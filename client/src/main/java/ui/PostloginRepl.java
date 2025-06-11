package ui;

import chess.ChessGame;
import client.ServerFacade;
import model.AuthData;
import model.GameData;
import chess.ChessMove;
import chess.ChessPosition;

import java.util.List;
import java.util.Scanner;

public class PostloginRepl {
    private final AuthData auth;
    private final ServerFacade facade;
    private List<GameData> gameList = null;

    public PostloginRepl(AuthData auth, ServerFacade facade) {
        this.auth = auth;
        this.facade = facade;
    }

    // Main command loop after login
    public void run() {
        System.out.println("Entered post-login UI for user: " + auth.username());
        Scanner scanner = new Scanner(System.in);

        while (true) {
            System.out.print("[Postlogin] >>> ");
            String command = scanner.nextLine().trim().toLowerCase();

            try {
                switch (command) {
                    case "help" -> printHelp();
                    case "create" -> handleCreate(scanner);
                    case "list" -> handleList();
                    case "observe" -> handleObserve(scanner);
                    case "play" -> handlePlay(scanner);
                    case "move" -> handleMove(scanner);
                    case "resign" -> handleResign();
                    case "logout" -> {
                        handleLogout();
                        return;
                    }
                    case "quit" -> {
                        System.out.println("Goodbye!");
                        System.exit(0);
                    }
                    default -> System.out.println("Unknown command. Type 'help' for options.");
                }
            } catch (Exception e) {
                System.out.println("Error: " + e.getMessage());
            }
        }
    }

    // Creating a new game
    private void handleCreate(Scanner scanner) throws Exception {
        System.out.print("Game name: ");
        String gameName = scanner.nextLine();
        facade.createGame(auth.authToken(), gameName);
        System.out.println("Game created: " + gameName);
    }

    private void handleList() throws Exception {
        gameList = facade.listGames(auth.authToken());
        if (gameList.isEmpty()) {
            System.out.println("No games available.");
        } else {
            System.out.println("Available Games:");
            for (int i = 0; i < gameList.size(); i++) {
                GameData game = gameList.get(i);
                System.out.printf("%d. Name: %s | ID: %d | White: %s | Black: %s%n",
                        i + 1,
                        game.gameName(),
                        game.gameID(),
                        game.whiteUsername() != null ? game.whiteUsername() : "(empty)",
                        game.blackUsername() != null ? game.blackUsername() : "(empty)");
            }
        }
    }

    private void handleObserve(Scanner scanner) {
        if (gameList == null || gameList.isEmpty()) {
            System.out.println("No games listed yet. Use 'list' first.");
            return;
        }

        System.out.print("Game number to observe: ");
        try {
            int num = Integer.parseInt(scanner.nextLine().trim());
            if (num < 1 || num > gameList.size()) {
                System.out.println("Invalid game number.");
                return;
            }

            GameData game = gameList.get(num - 1);
            facade.joinGame(auth.authToken(), game.gameID(), null);
            System.out.println("Now observing game \"" + game.gameName() + "\".");
            BoardPrinter.printBoard(game.game(), true);
        } catch (Exception e) {
            System.out.println("Error observing game: " + e.getMessage());
        }
    }

    private void handlePlay(Scanner scanner) throws Exception {
        if (gameList == null || gameList.isEmpty()) {
            System.out.println("No games listed yet. Use 'list' first.");
            return;
        }

        System.out.print("Game number to play: ");
        int gameNumber = Integer.parseInt(scanner.nextLine().trim());

        if (gameNumber < 1 || gameNumber > gameList.size()) {
            System.out.println("Invalid game number.");
            return;
        }

        GameData game = gameList.get(gameNumber - 1);
        int gameID = game.gameID();

        System.out.print("Color (white/black): ");
        String colorInput = scanner.nextLine().trim().toLowerCase();

        String playerColor;
        if (colorInput.equals("white")) {
            playerColor = "WHITE";
        } else if (colorInput.equals("black")) {
            playerColor = "BLACK";
        } else {
            System.out.println("Invalid color. Choose 'white' or 'black'.");
            return;
        }

        boolean alreadyJoined = (playerColor.equals("WHITE") && auth.username().equals(game.whiteUsername())) ||
                (playerColor.equals("BLACK") && auth.username().equals(game.blackUsername()));

        if (!alreadyJoined) {
            facade.joinGame(auth.authToken(), gameID, playerColor);
            System.out.println("Joined game " + gameID + " as " + playerColor);
        }

        boolean whitePerspective = playerColor.equals("WHITE");
        BoardPrinter.printBoard(game.game(), whitePerspective);
    }

    private void handleMove(Scanner scanner) throws Exception {
        if (gameList == null || gameList.isEmpty()) {
            System.out.println("No games listed yet. Use 'list' first.");
            return;
        }

        System.out.print("Game number to make a move in: ");
        int gameNumber = Integer.parseInt(scanner.nextLine().trim());

        if (gameNumber < 1 || gameNumber > gameList.size()) {
            System.out.println("Invalid game number.");
            return;
        }

        GameData game = gameList.get(gameNumber - 1);
        int gameID = game.gameID();

        System.out.print("Start square (e.g., e2): ");
        String from = scanner.nextLine().trim().toLowerCase();

        System.out.print("End square (e.g., e4): ");
        String to = scanner.nextLine().trim().toLowerCase();

        try {
            ChessMove move = new ChessMove(
                    ChessPosition.fromAlgebraic(from),
                    ChessPosition.fromAlgebraic(to),
                    null
            );

            facade.movePiece(auth.authToken(), gameID, move);
            System.out.println("Move executed: " + from + " to " + to);

            List<GameData> games = facade.listGames(auth.authToken());
            GameData updatedGame = games.stream().filter(g -> g.gameID() == gameID).findFirst().orElseThrow();
            BoardPrinter.printBoard(updatedGame.game(), true);

        } catch (Exception e) {
            System.out.println("Invalid move: " + e.getMessage());
        }
    }

    private void handleResign() {
        System.out.println("Feature not implemented yet. Stay tuned!");
    }

    private void handleLogout() throws Exception {
        facade.logout(auth.authToken());
        System.out.println("Logged out.");
    }

    private void printHelp() {
        System.out.println("Available Commands:");
        System.out.println("- help: Show this help message.");
        System.out.println("- create: Create a new game.");
        System.out.println("- list: List all available games.");
        System.out.println("- join: Join a game by ID and color.");
        System.out.println("- observe: Observe a game in progress.");
        System.out.println("- play: Join a game and view the board.");
        System.out.println("- move: Make a move in a game.");
        System.out.println("- resign: Resign from a game. (Not yet implemented)");
        System.out.println("- logout: Log out of the current session.");
        System.out.println("- quit: Exit the program.");
    }
}

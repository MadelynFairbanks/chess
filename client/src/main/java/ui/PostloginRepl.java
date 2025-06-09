package ui;

import chess.ChessGame;
import client.ServerFacade;
import model.AuthData;
import model.GameData;
import result.ListGamesResult;
import chess.ChessMove;
import chess.ChessPosition;


import java.util.List;
import java.util.Scanner;

public class PostloginRepl {
    private final AuthData auth;
    private final ServerFacade facade;

    public PostloginRepl(AuthData auth, ServerFacade facade) {
        this.auth = auth;
        this.facade = facade;
    }

    public void run() {
        System.out.println("Entered post-login UI for user: " + auth.username());

        Scanner scanner = new Scanner(System.in);
        while (true) {
            System.out.print("[Postlogin] >>> ");
            String command = scanner.nextLine().trim().toLowerCase();

            try {
                switch (command) {
                    case "help" -> printHelp();

                    case "create" -> {
                        System.out.print("Game name: ");
                        String gameName = scanner.nextLine();
                        facade.createGame(auth.authToken(), gameName);
                        System.out.println("Game created: " + gameName);
                    }

                    case "list" -> {
                        List<GameData> games = facade.listGames(auth.authToken());

                        if (games.isEmpty()) {
                            System.out.println("No games available.");
                        } else {
                            System.out.println("Available Games:");
                            for (GameData game : games) {
                                System.out.printf("- ID: %d | Name: %s | White: %s | Black: %s%n",
                                        game.gameID(),
                                        game.gameName(),
                                        game.whiteUsername() != null ? game.whiteUsername() : "(empty)",
                                        game.blackUsername() != null ? game.blackUsername() : "(empty)");
                            }
                        }
                    }

                    case "join" -> {
                        System.out.print("Game ID: ");
                        int gameID = Integer.parseInt(scanner.nextLine().trim());

                        System.out.print("Color (white/black/empty): ");
                        String colorInput = scanner.nextLine().trim().toLowerCase();

                        String playerColor;
                        if (colorInput.equals("white")) {
                            playerColor = "WHITE";
                        } else if (colorInput.equals("black")) {
                            playerColor = "BLACK";
                        } else if (colorInput.equals("empty")) {
                            playerColor = null;
                        } else {
                            System.out.println("Invalid color. Choose 'white', 'black', or 'empty'.");
                            continue;
                        }

                        facade.joinGame(auth.authToken(), gameID, playerColor);
                        System.out.println("Joined game " + gameID + " as " +
                                (playerColor != null ? playerColor : "observer"));
                    }

                    case "observe" -> {
                        System.out.print("Game ID to observe: ");
                        int gameID = Integer.parseInt(scanner.nextLine().trim());

                        List<GameData> games = facade.listGames(auth.authToken());
                        GameData game = games.stream().filter(g -> g.gameID() == gameID).findFirst().orElseThrow();

                        System.out.println("Observing game " + gameID + ":");
                        BoardPrinter.printBoard(game.game(), true);  // true = white's perspective
                        // true = white's perspective

                    }

                    case "play" -> {
                        System.out.print("Game ID to play: ");
                        int gameID = Integer.parseInt(scanner.nextLine().trim());

                        System.out.print("Color (white/black): ");
                        String colorInput = scanner.nextLine().trim().toLowerCase();

                        String playerColor;
                        if (colorInput.equals("white")) {
                            playerColor = "WHITE";
                        } else if (colorInput.equals("black")) {
                            playerColor = "BLACK";
                        } else {
                            System.out.println("Invalid color. Choose 'white' or 'black'.");
                            continue;
                        }

                        // Check if user is already in the game
                        List<GameData> games = facade.listGames(auth.authToken());
                        GameData game = games.stream().filter(g -> g.gameID() == gameID).findFirst().orElseThrow();

                        boolean alreadyJoined = (playerColor.equals("WHITE") && auth.username().equals(game.whiteUsername())) ||
                                (playerColor.equals("BLACK") && auth.username().equals(game.blackUsername()));

                        if (!alreadyJoined) {
                            // Only join if not already joined
                            facade.joinGame(auth.authToken(), gameID, playerColor);
                            System.out.println("Joined game " + gameID + " as " + playerColor);
                        }

                        boolean whitePerspective = playerColor.equals("WHITE");
                        BoardPrinter.printBoard(game.game(), whitePerspective);
                    }


                    case "move" -> {
                        System.out.print("Game ID: ");
                        int gameID = Integer.parseInt(scanner.nextLine().trim());

                        System.out.print("Start square (e.g., e2): ");
                        String from = scanner.nextLine().trim().toLowerCase();

                        System.out.print("End square (e.g., e4): ");
                        String to = scanner.nextLine().trim().toLowerCase();

                        try {
                            ChessMove move = new ChessMove(
                                    ChessPosition.fromAlgebraic(from),
                                    ChessPosition.fromAlgebraic(to),
                                    null // You can modify this for promotion
                            );

                            facade.movePiece(auth.authToken(), gameID, move);
                            System.out.println("Move executed: " + from + " to " + to);

                            // Optional: print updated board
                            List<GameData> games = facade.listGames(auth.authToken());
                            GameData game = games.stream().filter(g -> g.gameID() == gameID).findFirst().orElseThrow();
                            BoardPrinter.printBoard(game.game(), true);

                        } catch (Exception e) {
                            System.out.println("Invalid move: " + e.getMessage());
                        }
                    }


                    case "resign" -> {
                        System.out.println("Feature not implemented yet. Stay tuned!");
                    }

                    case "logout" -> {
                        facade.logout(auth.authToken());
                        System.out.println("Logged out.");
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

    private void printHelp() {
        System.out.println("Available Commands:");
        System.out.println("- help: Show this help message.");
        System.out.println("- create: Create a new game.");
        System.out.println("- list: List all available games.");
        System.out.println("- join: Join a game by ID and color.");
        System.out.println("- observe: Observe a game in progress.");
        System.out.println("- play: Join a game and view the board.");
        System.out.println("- move: Make a move in a game. (Not yet implemented)");
        System.out.println("- resign: Resign from a game. (Not yet implemented)");
        System.out.println("- logout: Log out of the current session.");
        System.out.println("- quit: Exit the program.");
    }
}

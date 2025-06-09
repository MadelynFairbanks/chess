package ui;

import client.ServerFacade;
import model.AuthData;
import model.GameData;
import result.ListGamesResult;

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
        System.out.println("- logout: Log out of the current session.");
        System.out.println("- quit: Exit the program.");
    }
}

package ui;

import client.ServerFacade;
import model.AuthData;

import java.util.Scanner;

public class PostloginRepl {
    private final Scanner scanner = new Scanner(System.in);
    private final AuthData auth;
    private final ServerFacade facade;

    public PostloginRepl(AuthData auth, ServerFacade facade) {
        this.auth = auth;
        this.facade = facade;
    }

    public void run() {
        System.out.println("Entered post-login UI for user: " + auth.username());
        while (true) {
            System.out.print("\n[Postlogin] >>> ");
            String input = scanner.nextLine().trim().toLowerCase();

            switch (input) {
                case "help" -> printHelp();
                case "create" -> createGame();
                case "list" -> listGames();
                case "join" -> joinGame();
                case "logout" -> {
                    logout();
                    return;
                }
                default -> System.out.println("Unknown command. Type 'help' for options.");
            }
        }
    }

    private void printHelp() {
        System.out.println("Available Commands:");
        System.out.println("- create: Create a new game");
        System.out.println("- list: List available games");
        System.out.println("- join: Join a game");
        System.out.println("- logout: Log out and return to prelogin");
    }

    private void createGame() {
        try {
            System.out.print("Game name: ");
            String gameName = scanner.nextLine().trim();
            facade.createGame(auth.authToken(), gameName);
            System.out.println("Game created: " + gameName);
        } catch (Exception e) {
            System.out.println("Failed to create game: " + e.getMessage());
        }
    }

    private void listGames() {
        try {
            var games = facade.listGames(auth.authToken());
            if (games.games().isEmpty()) {
                System.out.println("No games available.");
            } else {
                games.games().forEach(g -> System.out.printf("ID: %d | Name: %s | White: %s | Black: %s%n",
                        g.gameID(), g.gameName(), g.whiteUsername(), g.blackUsername()));
            }
        } catch (Exception e) {
            System.out.println("Failed to list games: " + e.getMessage());
        }
    }

    private void joinGame() {
        try {
            System.out.print("Game ID: ");
            int gameID = Integer.parseInt(scanner.nextLine().trim());
            System.out.print("Color (white/black): ");
            String color = scanner.nextLine().trim().toLowerCase();

            facade.joinGame(auth.authToken(), gameID, color);
            System.out.println("Joined game " + gameID + " as " + color);
        } catch (Exception e) {
            System.out.println("Failed to join game: " + e.getMessage());
        }
    }

    private void logout() {
        try {
            facade.logout(auth.authToken());
            System.out.println("Logged out.");
        } catch (Exception e) {
            System.out.println("Logout failed: " + e.getMessage());
        }
    }
}

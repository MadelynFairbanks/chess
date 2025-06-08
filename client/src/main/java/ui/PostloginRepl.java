package ui;

import client.ServerFacade;
import model.AuthData;
import result.GameData;
import result.ListGamesResult;

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
                    case "help":
                        printHelp();
                        break;
                    case "create":
                        System.out.print("Game name: ");
                        String gameName = scanner.nextLine();
                        var result = facade.createGame(auth.authToken(), gameName);
                        System.out.println("Game created: " + gameName);
                        break;
                    case "list":
                        ListGamesResult listResult = facade.listGames(auth.authToken());
                        if (listResult.games().isEmpty()) {
                            System.out.println("No games available.");
                        } else {
                            System.out.println("Available Games:");
                            for (GameData game : listResult.games()) {
                                System.out.printf("- ID: %d | Name: %s | White: %s | Black: %s%n",
                                        game.gameID(),
                                        game.gameName(),
                                        game.whiteUsername() != null ? game.whiteUsername() : "(empty)",
                                        game.blackUsername() != null ? game.blackUsername() : "(empty)");
                            }
                        }
                        break;
                    case "logout":
                        facade.logout(auth.authToken());
                        System.out.println("Logged out.");
                        return;
                    case "quit":
                        System.out.println("Goodbye!");
                        System.exit(0);
                        break;
                    default:
                        System.out.println("Unknown command. Type 'help' for options.");
                        break;
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
        System.out.println("- logout: Log out of the current session.");
        System.out.println("- quit: Exit the program.");
    }
}

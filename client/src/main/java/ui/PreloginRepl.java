package ui;

import client.ServerFacade;
import model.AuthData;

import java.util.Scanner;

public class PreloginRepl {
    private final Scanner scanner = new Scanner(System.in);
    private final ServerFacade facade = new ServerFacade(8080);

    public void run() {
        System.out.println("Type 'help' to see available commands.");
        while (true) {
            System.out.print("\n[Prelogin] >>> ");
            String input = scanner.nextLine().trim().toLowerCase();

            switch (input) {
                case "help" -> printHelp();
                case "register" -> register();
                case "login" -> login();
                case "quit" -> {
                    System.out.println("Goodbye!");
                    return;
                }
                default -> System.out.println("Unknown command. Type 'help' for available options.");
            }
        }
    }

    private void printHelp() {
        System.out.println("Available Commands:");
        System.out.println("- help: Show this help message.");
        System.out.println("- register: Create a new user.");
        System.out.println("- login: Log into your account.");
        System.out.println("- quit: Exit the program.");
    }

    private void register() {
        try {
            System.out.print("Username: ");
            String username = scanner.nextLine().trim();
            System.out.print("Password: ");
            String password = scanner.nextLine().trim();
            System.out.print("Email: ");
            String email = scanner.nextLine().trim();

            AuthData auth = facade.register(username, password, email);
            System.out.println("Registered and logged in as " + username);
            new PostloginRepl(auth, facade).run();

        } catch (Exception e) {
            System.out.println("Registration failed: " + e.getMessage());
        }
    }

    private void login() {
        try {
            System.out.print("Username: ");
            String username = scanner.nextLine().trim();
            System.out.print("Password: ");
            String password = scanner.nextLine().trim();

            AuthData auth = facade.login(username, password);
            System.out.println("Logged in as " + username);
            new PostloginRepl(auth, facade).run();

        } catch (Exception e) {
            System.out.println("Login failed: " + e.getMessage());
        }
    }
}

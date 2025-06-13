package ui.repl;

import ui.client.PostLogInClient;
import ui.client.PreLogInClient;

import java.util.Scanner;

import static ui.EscapeSequences.*;

public class PostLogInRepl {
    public final String authToken;

    private final PostLogInClient client;
    private final String serverUrl;

    public PostLogInRepl(String serverUrl,  String authToken) {
        client = new PostLogInClient(serverUrl /*, this ----- notification handler*/);
        this.serverUrl = serverUrl;
        this.authToken = authToken;
    }

    public void run() {
        System.out.println(SET_TEXT_COLOR_BLUE + "\nWelcome to Calvin's Chess Client. \n\nDo you want to play a game?");
        System.out.print(client.help());

        Scanner scanner = new Scanner(System.in);
        var result = "";
        while (!result.equals("quit")) {
            printPrompt();
            String line = scanner.nextLine();

            try {
                result = client.eval(line, authToken);
                System.out.print(SET_TEXT_COLOR_BLUE + result);
                if (result.equals(" Game Joined Successfully! ")) {
                    System.out.println(SET_TEXT_COLOR_BLUE + "Gameplay has started");
                    GamePlayRepl gamePlayRepl = new GamePlayRepl(this.serverUrl, client.getGameID());
                    gamePlayRepl.run();
                }
                if (result.equals(" GOODBYE!!! ")) {
                    break;
                }
            } catch (Throwable e) {
                var msg = e.toString();
                System.out.print(msg);
            }
        }
        System.out.println();
    }



    private void printPrompt() {
        System.out.print("\n" + RESET_TEXT_COLOR + ">>> " + SET_TEXT_COLOR_GREEN);
    }

}

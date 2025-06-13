package ui.repl;

import ui.client.PostLogInClient;
import ui.client.PreLogInClient;

import java.util.Scanner;

import static ui.EscapeSequences.*;

public class PostLogInRepl {

    public final String authToken;

    private final PostLogInClient client;
    private final String serverUrl;

    // 🏗️ Constructor: Let’s set the stage
    public PostLogInRepl(String serverUrl, String authToken) {
        client = new PostLogInClient(serverUrl /* ← could pass NotificationHandler here if needed */);
        this.serverUrl = serverUrl;
        this.authToken = authToken;
    }

    // 🧠 This is the main loop for post-login commands
    public void run() {
        System.out.println(SET_TEXT_COLOR_BLUE + "\nWelcome to Calvin's Chess Client 🎉");
        System.out.println("Do you want to play a game? 🧐");
        System.out.print(client.help());

        Scanner scanner = new Scanner(System.in);
        var result = "";

        while (!result.equals("quit")) {
            printPrompt();  // 🎤 drop that input cursor
            String line = scanner.nextLine();

            try {
                result = client.eval(line, authToken);
                System.out.print(SET_TEXT_COLOR_BLUE + result);

                // 🚪 Transition into gameplay when ready
                if (result.equals(" Game Joined Successfully! ")) {
                    System.out.println(SET_TEXT_COLOR_BLUE + "⚔️  Gameplay has started!");
                    GamePlayRepl gamePlayRepl = new GamePlayRepl(this.serverUrl, client.getGameID());
                    gamePlayRepl.run();  // 📦 dive into actual game
                }

                // ✌️ Bounce if the user logs out
                if (result.equals(" GOODBYE!!! ")) {
                    break;
                }

            } catch (Throwable e) {
                var msg = e.toString();  // 💥 error caught
                System.out.print(msg);
            }
        }

        System.out.println();  // 🧼 clean exit
    }

    // 🖊️ Prompt that makes you feel like a hacker
    private void printPrompt() {
        System.out.print("\n" + RESET_TEXT_COLOR + ">>> " + SET_TEXT_COLOR_GREEN);
    }
}

package ui.repl;

import ui.client.OurPreLogInClient;

import java.util.Scanner;

import static ui.EscapeSequences.*;

public class PreLogInRepl {

    private final OurPreLogInClient client;
    private final String serverUrl;

    // 🏁 Constructor: Gotta set that server connection up
    public PreLogInRepl(String serverUrl) {
        client = new OurPreLogInClient(serverUrl /* could pass NotificationHandler here if needed */);
        this.serverUrl = serverUrl;
    }

    // 🚪 The pre-login REPL loop — aka the lobby
    public void run() {
        System.out.println("✨ Welcome to Calvin's Chess Client ✨");
        System.out.println("Please sign in or register to start your chess journey 🧠");
        System.out.print(client.help());

        Scanner scanner = new Scanner(System.in);
        var result = "";

        while (!result.equals("quit")) {
            printPrompt();  // 🎤 input time
            String line = scanner.nextLine();

            try {
                result = client.eval(line);  // 🧠 evaluate command
                System.out.print(SET_TEXT_COLOR_WHITE + result);

                // 🚀 If login or registration is successful, head to post-login loop
                if (result.equals("Successful registration!!") || result.equals("Successful login!!")) {
                    PostLogInRepl postLoginRepl = new PostLogInRepl(this.serverUrl, OurPreLogInClient.getAuthToken());
                    postLoginRepl.run();  // 🎮 start the post-login experience
                }

            } catch (Throwable e) {
                var msg = e.toString();  // 💥 oopsie
                System.out.print(msg);
            }
        }

        System.out.println();  // 🧼 clean exit
    }

    // 🖊️ prompt to get user input like a cool command-line wizard
    private void printPrompt() {
        System.out.print("\n" + RESET_TEXT_COLOR + ">>> " + SET_TEXT_COLOR_GREEN);
    }
}

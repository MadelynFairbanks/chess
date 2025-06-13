package ui.repl;

import exception.ResponseException;
import model.GameID;
import ui.client.GamePlayClient;
import ui.websocket.NotificationHandler;

import java.util.Scanner;

import static ui.EscapeSequences.*;
import static ui.client.PreLogInClient.authToken;

public class GamePlayRepl implements NotificationHandler {

    private final GamePlayClient client;
    private final String serverUrl;
    private final GameID gameID;

    // 🧠 Constructor — gets us ready to vibe in a game
    public GamePlayRepl(String serverUrl, GameID gameID) {
        this.client = new GamePlayClient(serverUrl, this);
        this.serverUrl = serverUrl;
        this.gameID = gameID;
    }

    // 🚀 Main gameplay loop — this is where stuff goes DOWN
    public void run() throws ResponseException {
        client.joinGame(authToken, gameID);  // 🔐 we in

        System.out.println(SET_TEXT_COLOR_MAGENTA + "🎮 Welcome to Game Play!");
        System.out.print(client.help());

        Scanner scanner = new Scanner(System.in);
        var result = "";

        while (!result.equals("quit")) {
            printPrompt();  // 👉 user input plz
            String line = scanner.nextLine();

            try {
                result = client.eval(line, authToken);  // 🧠 interpret that input
                System.out.print(SET_TEXT_COLOR_MAGENTA + result);

                // 🛑 exit out of special case if needed
                if (result.equals("finished gameplay 09k")) {
                    break;
                }

            } catch (Throwable e) {
                var msg = e.toString();  // 💥 exception? tell us the tea
                System.out.print(msg);
            }
        }

        System.out.println(); // 👋 after exiting loop
    }

    // 🔔 Notif handler from server → display message like a boss
    public void notify(String notification) {
        System.out.println(SET_TEXT_COLOR_RED + "\t" + notification);
        printPrompt();  // 👀 show prompt again after notif
    }

    // 🖊️ lil prompt for user input
    private void printPrompt() {
        System.out.print("\n" + RESET_TEXT_COLOR + ">>> " + SET_TEXT_COLOR_GREEN);
    }
}

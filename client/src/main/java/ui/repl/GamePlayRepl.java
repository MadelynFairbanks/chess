package ui.repl;

import exception.ResponseException;
import model.GameID;
import ui.client.GamePlayClient;
import ui.websocket.NotificationHandler;
import websocket.messages.Notification;

import java.util.Scanner;

import static ui.EscapeSequences.*;
import static ui.client.PreLogInClient.authToken;

public class GamePlayRepl implements NotificationHandler {

    private final GamePlayClient client;
    private final String serverUrl;
    private final GameID gameID;

    public GamePlayRepl(String serverUrl, GameID gameID){

        client = new GamePlayClient(serverUrl, this);
        this.serverUrl = serverUrl;
        this.gameID = gameID;
    }

    public void run() throws ResponseException {
        client.joinGame(authToken, gameID);

        System.out.println(SET_TEXT_COLOR_MAGENTA + "Welcome to Game Play");
        System.out.print(client.help());

        Scanner scanner = new Scanner(System.in);
        var result = "";
        while (!result.equals("quit")) {
            printPrompt();
            String line = scanner.nextLine();

            try {
                result = client.eval(line, authToken);
                System.out.print(SET_TEXT_COLOR_MAGENTA + result);
                if (result.equals("finished gameplay 09k")) {
                    break;
                }
            } catch (Throwable e) {
                var msg = e.toString();
                System.out.print(msg);
            }
        }
        System.out.println();
    }


    public void notify(String notification) {
        System.out.println(SET_TEXT_COLOR_RED + "\t"+notification);
        printPrompt();
    }


    private void printPrompt() {
        System.out.print("\n" + RESET_TEXT_COLOR + ">>> " + SET_TEXT_COLOR_GREEN);
    }

}

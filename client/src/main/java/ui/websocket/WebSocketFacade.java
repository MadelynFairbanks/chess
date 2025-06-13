package ui.websocket;

import chess.*;
import com.google.gson.Gson;
import exception.ResponseException;
import model.GameData;
import model.GameID;
import ui.LoadBoard;
import ui.client.OurPostLogInClient;
import websocket.commands.MakeMoveCommand;
import websocket.commands.UserGameCommand;
import websocket.messages.ServerMessage;

import javax.websocket.*;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.List;

public class WebSocketFacade extends Endpoint {

    Session session;
    NotificationHandler notificationHandler;
    LoadBoard board;
    GameID gameID;

    // 🧠 Constructor: establishing our WebSocket lifeline
    public WebSocketFacade(String url, NotificationHandler notificationHandler, GameID gameID) throws ResponseException {
        try {
            this.gameID = gameID;
            url = url.replace("http", "ws");
            URI socketURI = new URI(url + "/ws");
            this.notificationHandler = notificationHandler;

            WebSocketContainer container = ContainerProvider.getWebSocketContainer();
            this.session = container.connectToServer(this, socketURI);

            // 💬 Handle incoming messages from the server
            this.session.addMessageHandler((MessageHandler.Whole<String>) message -> handleServerMessage(message));
        } catch (DeploymentException | IOException | URISyntaxException ex) {
            throw new ResponseException(500, ex.getMessage());
        }
    }

    private void handleServerMessage(String message) {
        Gson gson = new Gson();
        ServerMessage serverMessage = gson.fromJson(message, ServerMessage.class);

        switch (serverMessage.getServerMessageType()) {
            case NOTIFICATION -> {
                String notification = (String) serverMessage.getServerMessage();
                notificationHandler.notify(notification);  // 📢 alert the player
            }
            case LOAD_GAME -> loadGameData(serverMessage.getGame());
            case ERROR -> System.out.println(serverMessage.getErrorMessage());  // 💀 print the server-side L
        }
    }

    private void loadGameData(GameData gameData) {
        board = new LoadBoard();
        if (OurPostLogInClient.color == null) {
            board.loadBoard(gameData, "observer");
        } else {
            board.loadBoard(gameData, OurPostLogInClient.color);
        }
    }

    @Override
    public void onOpen(Session session, EndpointConfig endpointConfig) {
        System.out.println("WebSocket connection opened 🚪✨");
    }

    public void joinGame(String authToken, GameID gameID) throws ResponseException {
        sendUserGameCommand(UserGameCommand.CommandType.CONNECT, authToken, gameID);
    }

    public void resign(String authToken) throws ResponseException {
        sendUserGameCommand(UserGameCommand.CommandType.RESIGN, authToken, gameID);
    }

    public void leave(String authToken) throws ResponseException {
        sendUserGameCommand(UserGameCommand.CommandType.LEAVE, authToken, gameID);
    }

    private void sendUserGameCommand(UserGameCommand.CommandType type, String authToken, GameID gameID) throws ResponseException {
        try {
            UserGameCommand command = new UserGameCommand(type, authToken, gameID.gameID());
            this.session.getBasicRemote().sendText(new Gson().toJson(command));
        } catch (IOException ex) {
            throw new ResponseException(500, ex.getMessage());
        }
    }

    public String makeMove(String authToken, String... params) throws ResponseException {
        try {
            ChessMove move = buildMoveFromParams(params);
            if (move == null) return "Invalid move parameters 😬";

            MakeMoveCommand moveCommand = new MakeMoveCommand(authToken, gameID.gameID(), move);
            this.session.getBasicRemote().sendText(new Gson().toJson(moveCommand));
        } catch (IOException ex) {
            throw new ResponseException(500, ex.getMessage());
        }

        return "";
    }

    private ChessMove buildMoveFromParams(String[] params) {
        if (params.length != 2 && params.length != 3) return null;

        int[] start = parsePosition(params[0]);
        int[] end = parsePosition(params[1]);

        if (!isValidPosition(start) || !isValidPosition(end)) return null;

        ChessPiece.PieceType promo = null;
        if (params.length == 3) {
            promo = parsePromotion(params[2]);
            if (promo == null) return null;
        }

        return new ChessMove(
                new ChessPosition(start[0], start[1]),
                new ChessPosition(end[0], end[1]),
                promo
        );
    }

    public void redrawBoard(String authToken) {
        board.redrawBoard();
    }

    public void highlight(String authToken, String piecePosition) {
        int[] parsed = parsePosition(piecePosition);

        if (!isValidPosition(parsed)) {
            System.out.println("🚫 Invalid Location Input");
            return;
        }

        ChessPosition position = new ChessPosition(parsed[0], parsed[1]);
        board.drawHighlightedBoard(position);
    }

    public int[] parsePosition(String position) {
        String[] cr = position.split("");
        List<String> validLetters = Arrays.asList("a", "b", "c", "d", "e", "f", "g", "h");
        List<String> validNumbers = Arrays.asList("1", "2", "3", "4", "5", "6", "7", "8");

        if (!validLetters.contains(cr[0]) || !validNumbers.contains(cr[1])) {
            System.out.println("🚫 Bad Position Data Given");
            return null;
        }

        int rowNumber = Integer.parseInt(cr[1]);
        int column = switch (cr[0]) {
            case "a" -> 1;
            case "b" -> 2;
            case "c" -> 3;
            case "d" -> 4;
            case "e" -> 5;
            case "f" -> 6;
            case "g" -> 7;
            case "h" -> 8;
            default -> 0;
        };

        return new int[]{rowNumber, column};
    }

    public ChessPiece.PieceType parsePromotion(String promotion) {
        return switch (promotion) {
            case "q", "queen" -> ChessPiece.PieceType.QUEEN;
            case "b", "bishop" -> ChessPiece.PieceType.BISHOP;
            case "r", "rook" -> ChessPiece.PieceType.ROOK;
            case "k", "knight" -> ChessPiece.PieceType.KNIGHT;
            default -> null;
        };
    }

    private boolean isValidPosition(int[] pos) {
        return pos != null && pos[0] >= 1 && pos[0] <= 8 && pos[1] >= 1 && pos[1] <= 8;
    }
}

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
            this.session.addMessageHandler((MessageHandler.Whole<String>) message -> {
                Gson gson = new Gson();
                ServerMessage serverMessage = gson.fromJson(message, ServerMessage.class);

                switch (serverMessage.getServerMessageType()) {
                    case NOTIFICATION -> {
                        String notification = (String) serverMessage.getServerMessage();
                        notificationHandler.notify(notification);  // 📢 alert the player
                    }
                    case LOAD_GAME -> {
                        GameData gameData = serverMessage.getGame();
                        board = new LoadBoard();

                        // 👀 Are we observing or playing?
                        if (OurPostLogInClient.color == null) {
                            board.loadBoard(gameData, "observer");
                        } else {
                            board.loadBoard(gameData, OurPostLogInClient.color);
                        }
                    }
                    case ERROR -> {
                        System.out.println(serverMessage.getErrorMessage());  // 💀 print the server-side L
                    }
                }
            });

        } catch (DeploymentException | IOException | URISyntaxException ex) {
            throw new ResponseException(500, ex.getMessage());
        }
    }

    @Override
    public void onOpen(Session session, EndpointConfig endpointConfig) {
        System.out.println("WebSocket connection opened 🚪✨");
    }

    // 🤝 Join a game over the wire
    public void joinGame(String authToken, GameID gameID) throws ResponseException {
        try {
            UserGameCommand connectCommand = new UserGameCommand(
                    UserGameCommand.CommandType.CONNECT, authToken, gameID.gameID());
            String json = new Gson().toJson(connectCommand);
            this.session.getBasicRemote().sendText(json);
        } catch (IOException ex) {
            throw new ResponseException(500, ex.getMessage());
        }
    }

    // 🔍 Translate positions like "e2" into numbers
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

    // 👑 Parse promotion input like "q" or "queen"
    public ChessPiece.PieceType parsePromotion(String promotion) {
        return switch (promotion) {
            case "q", "queen"   -> ChessPiece.PieceType.QUEEN;
            case "b", "bishop"  -> ChessPiece.PieceType.BISHOP;
            case "r", "rook"    -> ChessPiece.PieceType.ROOK;
            case "k", "knight"  -> ChessPiece.PieceType.KNIGHT;
            default             -> null;
        };
    }

    // ✅ Bounds checking — we ain't doing out-of-bounds chess here
    private boolean isValidPosition(int[] pos) {
        return pos != null && pos[0] >= 1 && pos[0] <= 8 && pos[1] >= 1 && pos[1] <= 8;
    }

    // ♟️ Send a move over the WebSocket wire
    public String makeMove(String authToken, String... params) throws ResponseException, IOException {
        try {
            ChessPiece.PieceType piece = null;

            if (params.length == 3) {
                piece = parsePromotion(params[2]);
                if (piece == null) {
                    return "Invalid Promotional Piece";
                }
            } else if (params.length != 2) {
                throw new ResponseException(500, "server error");
            }

            int[] start = parsePosition(params[0]);
            int[] end = parsePosition(params[1]);

            if (!isValidPosition(start) || !isValidPosition(end)) {
                return "Invalid Location Input";
            }

            ChessMove move = new ChessMove(
                    new ChessPosition(start[0], start[1]),
                    new ChessPosition(end[0], end[1]),
                    piece
            );

            MakeMoveCommand moveCommand = new MakeMoveCommand(authToken, gameID.gameID(), move);
            this.session.getBasicRemote().sendText(new Gson().toJson(moveCommand));

        } catch (IOException ex) {
            throw new ResponseException(500, ex.getMessage());
        }

        return "";
    }

    // 🔁 Redraw the board when asked
    public void redrawBoard(String authToken) {
        board.redrawBoard();
    }

    // 😤 Resign from the game like a champ (or cry)
    public void resign(String authToken) throws ResponseException {
        try {
            UserGameCommand resignCommand = new UserGameCommand(
                    UserGameCommand.CommandType.RESIGN, authToken, gameID.gameID());
            this.session.getBasicRemote().sendText(new Gson().toJson(resignCommand));
        } catch (IOException ex) {
            throw new ResponseException(500, ex.getMessage());
        }
    }

    // 👋 Dip from the game without flipping the board
    public void leave(String authToken) throws ResponseException {
        try {
            UserGameCommand leaveCommand = new UserGameCommand(
                    UserGameCommand.CommandType.LEAVE, authToken, gameID.gameID());
            this.session.getBasicRemote().sendText(new Gson().toJson(leaveCommand));
        } catch (IOException ex) {
            throw new ResponseException(500, ex.getMessage());
        }
    }

    // ✨ Highlight all legal moves from a piece's position
    public void highlight(String authToken, String piecePosition) {
        int[] parsed = parsePosition(piecePosition);

        if (!isValidPosition(parsed)) {
            System.out.println("🚫 Invalid Location Input");
        } else {
            ChessPosition position = new ChessPosition(parsed[0], parsed[1]);
            board.drawHighlightedBoard(position);
        }
    }
}

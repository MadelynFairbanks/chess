package ui.websocket;


import chess.ChessMove;
import chess.ChessPiece;
import chess.ChessPosition;
import com.google.gson.Gson;

import exception.ResponseException;
import model.GameData;
import model.GameID;
import ui.LoadBoard;
import ui.client.PostLogInClient;
import websocket.commands.MakeMoveCommand;
import websocket.commands.UserGameCommand;
import websocket.messages.ServerMessage;

import javax.websocket.*;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.List;

public class WebSocketFacade extends Endpoint{
    Session session;
    NotificationHandler notificationHandler;
    LoadBoard board;
    GameID gameID;




    public WebSocketFacade(String url, NotificationHandler notificationHandler, GameID gameID) throws ResponseException {
        try {
            this.gameID = gameID;
            url = url.replace("http", "ws");
            URI socketURI = new URI(url + "/ws");
            this.notificationHandler = notificationHandler;

            WebSocketContainer container = ContainerProvider.getWebSocketContainer();
            this.session = container.connectToServer(this, socketURI);

            this.session.addMessageHandler(new MessageHandler.Whole<String>() {
                @Override
                public void onMessage(String message) {
                    Gson gson = new Gson();
                    ServerMessage serverMessage = gson.fromJson(message, ServerMessage.class);
                    if (serverMessage.getServerMessageType().equals(ServerMessage.ServerMessageType.NOTIFICATION)) {
                        String notification = (String) serverMessage.getServerMessage();
                        notificationHandler.notify(notification);
                    }else if (serverMessage.getServerMessageType().equals(ServerMessage.ServerMessageType.LOAD_GAME)) {
                        GameData gameData = serverMessage.getGame();
                        board = new LoadBoard();
                        if (PostLogInClient.color == null){
                            board.loadBoard(gameData, "observer");
                        }else {
                            board.loadBoard(gameData, PostLogInClient.color);
                        }
                    }else if (serverMessage.getServerMessageType().equals(ServerMessage.ServerMessageType.ERROR)){
                        System.out.println(serverMessage.getErrorMessage());
                    }
                }
            });



        } catch (DeploymentException | IOException | URISyntaxException ex) {
            throw new ResponseException(500, ex.getMessage());
        }
    }

    @Override
    public void onOpen(Session session, EndpointConfig endpointConfig) {
        System.out.println("WebSocket connection opened -- written by calvin.. me.. delete this comment");

    }



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



    public int[] parsePosition(String position) {
        String[] cr = position.split("");
        List<String> validLetters = Arrays.asList("a", "b", "c", "d", "e", "f", "g", "h");
        List<String> validNumbers = Arrays.asList("1", "2", "3", "4", "5", "6", "7", "8");

        if (!validLetters.contains(cr[0]) || !validNumbers.contains(cr[1])){
            System.out.println("Bad Position Data Given");
            return null;
        }
        int rowNumber = Integer.parseInt(cr[1]);
        String column2 = cr[0];
        int column;
        switch (column2) {
            case "a" -> column = 1;
            case "b" -> column = 2;
            case "c" -> column = 3;
            case "d" -> column = 4;
            case "e" -> column = 5;
            case "f" -> column = 6;
            case "g" -> column = 7;
            case "h" -> column = 8;
            default -> column = 0;
        }




        return new int[]{rowNumber, column};
    }

    public ChessPiece.PieceType parsePromotion(String promotion){
        return switch (promotion) {
            case "q", "queen" -> ChessPiece.PieceType.QUEEN;
            case "b", "bishop" ->  ChessPiece.PieceType.BISHOP;
            case "r", "rook" -> ChessPiece.PieceType.ROOK;
            case "k", "knight" -> ChessPiece.PieceType.KNIGHT;
            default -> null;

        };

    }

    private boolean isValidPosition(int[] pos) {
        if (pos == null){
            return false;
        }
        return pos[0] >= 1 && pos[0] < 9 && pos[1] >= 1 && pos[1] < 9;
    }

    public String makeMove(String authToken, String... params) throws ResponseException, IOException {
        try{
            ChessPiece.PieceType piece;
            piece = null;
            if(params.length == 3) {
                    piece = parsePromotion(params[2]);
                    if (piece != null){
                        return "Invalid Promotional Piece";
                    }
            }else if (params.length != 2){
                throw new ResponseException (500, "server error");
            }

            int[] start = parsePosition(params[0]);
            int[] end = parsePosition(params[1]);
            if (!isValidPosition(start) || !isValidPosition(end)) {
                return "Invalid Location Input";
            }
            ChessPosition startPosition = new ChessPosition(start[0],start[1]);
            ChessPosition endPosition = new ChessPosition(end[0], end[1]);
            ChessMove move = new ChessMove(startPosition,endPosition,piece);
            MakeMoveCommand connectCommand = new MakeMoveCommand( authToken, gameID.gameID(), move);
            this.session.getBasicRemote().sendText(new Gson().toJson(connectCommand));
        } catch (IOException ex) {
            throw new ResponseException(500, ex.getMessage());
        }
        return "";
    }


    public void redrawBoard(String authToken){
        board.redrawBoard();
    }

    public void resign(String authToken) throws ResponseException {
        try {
            UserGameCommand connectCommand = new UserGameCommand(
                    UserGameCommand.CommandType.RESIGN, authToken, gameID.gameID());
            String json = new Gson().toJson(connectCommand);
            this.session.getBasicRemote().sendText(json);
        } catch (IOException ex) {
            throw new ResponseException(500, ex.getMessage());
        }
    }

    public void leave(String authToken) throws ResponseException {
        try{
            UserGameCommand connectCommand = new UserGameCommand(
                    UserGameCommand.CommandType.LEAVE, authToken, gameID.gameID());
            String json = new Gson().toJson(connectCommand);
            this.session.getBasicRemote().sendText(json);
        } catch (IOException ex) {
        throw new ResponseException(500, ex.getMessage());
        }
    }

    public void highlight(String authToken, String piecePosition) {
        int[] parsedPiecePosition = parsePosition(piecePosition);
        if (!isValidPosition(parsedPiecePosition)) {
            System.out.println("Invalid Location Input");

        }else {

            ChessPosition position = new ChessPosition(parsedPiecePosition[0], parsedPiecePosition[1]);
            board.drawHighlightedBoard(position);
        }
    }

}

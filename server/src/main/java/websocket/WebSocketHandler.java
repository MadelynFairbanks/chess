package websocket;

import chess.*;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import dataaccess.DataAccessException;
import model.GameData;
import model.GameID;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.*;
import service.TheChessService;
import websocket.commands.MakeMoveCommand;
import websocket.commands.UserGameCommand;
import websocket.messages.ServerMessage;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;

@WebSocket
public class WebSocketHandler {
    private final ConnectionManager connections = new ConnectionManager(); // squad manager tbh

    @OnWebSocketMessage
    public void onMessage(Session session, String message) {
        try {
            JsonObject json = JsonParser.parseString(message).getAsJsonObject();
            String commandType = json.get("commandType").getAsString();
            System.out.println("WebSocketHandler got message type: " + commandType);

            switch (commandType) {
                case "CONNECT" -> connect(session, new Gson().fromJson(message, UserGameCommand.class));
                case "MAKE_MOVE" -> makeMove(session, new Gson().fromJson(message, MakeMoveCommand.class));
                case "LEAVE" -> leaveGame(new Gson().fromJson(message, UserGameCommand.class));
                case "RESIGN" -> resign(session, new Gson().fromJson(message, UserGameCommand.class));
            }
        } catch (DataAccessException ex) {
            connections.sendError(session.getRemote(), "Error: unauthorized"); // classic permission drama
        }
    }

    @OnWebSocketError
    public void onError(Session session, Throwable throwable) {
        System.err.println("⚠️ WebSocket Error for session " + session + ": " + throwable.getMessage());
        throwable.printStackTrace(); // gotta air the dirty laundry
    }

    @OnWebSocketClose
    public void onClose(Session session, int statusCode, String reason) {
        System.out.println("✌️ WebSocket Closed. Code: " + statusCode + ", Reason: " + reason);
    }

    private void connect(Session session, UserGameCommand command) throws DataAccessException {
        connections.resigned.put(command.getGameID(), false);
        String username = TheChessService.getAuthData(command.getAuthToken()).username();
        connections.addConnection(username, command.getGameID(), session);

        GameData gameData;
        try {
            gameData = TheChessService.getGame(command.getAuthToken(), new GameID(command.getGameID()));
        } catch (DataAccessException e) {
            connections.sendError(session.getRemote(), "Error: GameData is unusable 1");
            return;
        }

        ServerMessage loadMessage = new ServerMessage(ServerMessage.ServerMessageType.LOAD_GAME, gameData, null, null);
        try {
            connections.send(loadMessage, username, command.getGameID());
        } catch (IOException e) {
            connections.sendError(session.getRemote(), "Error: GameData is unusable 2");
            return;
        }

        String view = gameData.blackUsername() != null && gameData.blackUsername().equals(username) ? "BLACK" :
                gameData.whiteUsername() != null && gameData.whiteUsername().equals(username) ? "WHITE" :
                        "an Observer 👀";

        String joinMessage = String.format("%s has joined the game as %s", username, view);
        ServerMessage notification = new ServerMessage(ServerMessage.ServerMessageType.NOTIFICATION, null, joinMessage, null);
        connections.broadcast(username, notification, command.getGameID());
    }

    private String getColumnLetter(int col) {
        return switch (col) {
            case 1 -> "a";
            case 2 -> "b";
            case 3 -> "c";
            case 4 -> "d";
            case 5 -> "e";
            case 6 -> "f";
            case 7 -> "g";
            case 8 -> "h";
            default -> "?"; // girl what column even is this
        };
    }

    private void delayRace() {
        try {
            Thread.sleep(100); // we stan a polite thread
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt(); // nap cancelled
        }
    }

    private void makeMove(Session session, MakeMoveCommand command) throws DataAccessException {
        System.out.println("Making move 💃");
        synchronized (connections) {
            String username = TheChessService.getAuthData(command.getAuthToken()).username();
            GameData gameData = TheChessService.getGame(command.getAuthToken(), new GameID(command.getGameID()));

            Collection<ChessMove> validMoves = connections.resigned.get(command.getGameID()) ? new ArrayList<>() :
                    gameData.game().validMoves(command.getMove().getStartPosition());

            ChessMove move = command.getMove();
            String startCol = getColumnLetter(move.getStartPosition().getColumn());
            String endCol = getColumnLetter(move.getEndPosition().getColumn());

            if (startCol.equals("?") || endCol.equals("?")) {
                connections.sendError(session.getRemote(), "bad input, try again 🤨");
                return;
            }

            if (validMoves.contains(move)) {
                updateGameWMove(username, command, move, session, gameData, new String[]{startCol, endCol});
            } else {
                connections.sendError(session.getRemote(), "Error: invalid move given 🚫");
            }
        }
    }

    private void updateGameWMove(String username, MakeMoveCommand command, ChessMove move,
                                 Session session, GameData gameData, String[] columns) {
        try {
            GameData existingGame = TheChessService.getGame(command.getAuthToken(), new GameID(command.getGameID()));
            ChessGame.TeamColor color = gameData.game().getBoard().getPiece(move.getStartPosition()).getTeamColor();

            boolean wrongPlayer = (color == ChessGame.TeamColor.WHITE && !gameData.whiteUsername().equals(username)) ||
                    (color == ChessGame.TeamColor.BLACK && !gameData.blackUsername().equals(username));

            if (wrongPlayer) {
                connections.sendError(session.getRemote(), "Error: Not your turn 🚷");
                return;
            }

            gameData.game().makeMove(move);
            TheChessService.updateGame(command.getAuthToken(), gameData);

        } catch (InvalidMoveException | DataAccessException e) {
            connections.sendError(session.getRemote(), "Error: invalid move 👎");
            return;
        }

        // notify player + others that a move just slayed
        try {
            ServerMessage update = new ServerMessage(ServerMessage.ServerMessageType.LOAD_GAME, gameData, null, null);
            connections.send(update, username, command.getGameID());
            delayRace();
            connections.broadcast(username, update, command.getGameID());
        } catch (IOException e) {
            connections.sendError(session.getRemote(), "Error: couldn’t update game");
            return;
        }

        String moveMsg = String.format("%s moved %s to %s",
                username,
                columns[0] + move.getStartPosition().getRow(),
                columns[1] + move.getEndPosition().getRow());
        connections.broadcast(username, new ServerMessage(ServerMessage.ServerMessageType.NOTIFICATION, null, moveMsg, null), command.getGameID());

        // time to announce winners or check drama
        checkStatus(gameData, command, session, username);
    }

    private void checkStatus(GameData game, UserGameCommand command, Session session, String username) {
        String white = game.whiteUsername();
        String black = game.blackUsername();

        if (game.game().isInCheckmate(ChessGame.TeamColor.WHITE)) {
            notifyEveryone(username, session, command, String.format("‼️ Checkmate ‼️\n%s wins!", black));
        } else if (game.game().isInCheckmate(ChessGame.TeamColor.BLACK)) {
            notifyEveryone(username, session, command, String.format("‼️ Checkmate ‼️\n%s wins!", white));
        } else if (game.game().isInCheck(ChessGame.TeamColor.WHITE)) {
            notifyEveryone(username, session, command, String.format("%s is in check ⚠️", white));
        } else if (game.game().isInCheck(ChessGame.TeamColor.BLACK)) {
            notifyEveryone(username, session, command, String.format("%s is in check ⚠️", black));
        }
    }

    private void notifyEveryone(String username, Session session, UserGameCommand command, String msg) {
        ServerMessage notification = new ServerMessage(ServerMessage.ServerMessageType.NOTIFICATION, null, msg, null);
        try {
            connections.send(notification, username, command.getGameID());
            connections.broadcast(username, notification, command.getGameID());
        } catch (IOException e) {
            connections.sendError(session.getRemote(), "Error: can't notify the squad");
        }
    }

    private void resign(Session session, UserGameCommand command) throws DataAccessException {
        if (!connections.resigned.get(command.getGameID())) {
            String username = TheChessService.getAuthData(command.getAuthToken()).username();
            GameData game = TheChessService.getGame(command.getAuthToken(), new GameID(command.getGameID()));

            String victor = game.whiteUsername().equals(username) ? "BLACK" :
                    game.blackUsername().equals(username) ? "WHITE" : null;

            if (victor != null) {
                connections.resigned.put(command.getGameID(), true);
                notifyEveryone(username, session, command, String.format("%s has resigned 😔\n%s wins 🎉", username, victor));
            } else {
                connections.sendError(session.getRemote(), "Error: You’re just an observer, boo 👀");
            }
        } else {
            connections.sendError(session.getRemote(), "Error: Already resigned 👋");
        }
    }

    private void leaveGame(UserGameCommand command) throws DataAccessException {
        synchronized (connections) {
            String username = TheChessService.getAuthData(command.getAuthToken()).username();
            GameData existingGame = TheChessService.getGame(command.getAuthToken(), new GameID(command.getGameID()));

            var leaveMessage = new ServerMessage(ServerMessage.ServerMessageType.NOTIFICATION, null, username + " left the game 🏃", null);
            connections.broadcast(username, leaveMessage, command.getGameID());
            connections.removePlayer(command.getGameID(), username);

            if (username.equals(existingGame.whiteUsername())) {
                TheChessService.updateGameWhiteUsername(command.getAuthToken(), username, existingGame.gameID());
            } else if (username.equals(existingGame.blackUsername())) {
                TheChessService.updateGameBlackUsername(command.getAuthToken(), username, existingGame.gameID());
            }
        }
    }
}

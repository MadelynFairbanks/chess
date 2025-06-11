package server;

import chess.ChessGame;
import com.google.gson.Gson;
import dataaccess.DataAccess;
import dataaccess.MySqlDataAccess;
import model.GameData;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.*;
import websocket.commands.*;
import websocket.messages.*;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;

@WebSocket
public class WebSocketHandler {

    private static final ConcurrentHashMap<Integer, ConcurrentHashMap<Session, String>> gameSessions = new ConcurrentHashMap<>();
    private static final Gson gson = new Gson();
    private final DataAccess dataAccess = new MySqlDataAccess();

    @OnWebSocketConnect
    public void onConnect(Session session) {
        System.out.println("WebSocket connected: " + session);
    }

    @OnWebSocketClose
    public void onClose(Session session, int statusCode, String reason) {
        System.out.println("WebSocket closed: " + session);
        gameSessions.values().forEach(map -> map.remove(session));
    }

    @OnWebSocketMessage
    public void onMessage(Session session, String message) {
        try {
            UserGameCommand baseCommand = gson.fromJson(message, UserGameCommand.class);
            switch (baseCommand.getCommandType()) {
                case CONNECT -> handleConnect(session, gson.fromJson(message, ConnectCommand.class));
                case MAKE_MOVE -> handleMove(session, gson.fromJson(message, MakeMoveCommand.class));
                case LEAVE -> handleLeave(session, gson.fromJson(message, LeaveCommand.class));
                case RESIGN -> handleResign(session, gson.fromJson(message, ResignCommand.class));
                default -> sendError(session, "Unknown command type.");
            }
        } catch (Exception e) {
            sendError(session, "Error parsing command: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void handleConnect(Session session, ConnectCommand command) {
        gameSessions.putIfAbsent(command.getGameID(), new ConcurrentHashMap<>());
        gameSessions.get(command.getGameID()).put(session, command.getAuthToken());

        try {
            GameData gameData = dataAccess.getGame(command.getGameID());
            ChessGame game = gameData.game();
            ServerMessage loadGame = new LoadGameMessage(game);
            send(session, loadGame);

            String role = (command.getPlayerColor() != null) ? command.getPlayerColor().toString().toLowerCase() : "observer";
            String notification = String.format("%s connected as %s", getUsername(command.getAuthToken()), role);
            broadcastToOthers(command.getGameID(), session, new NotificationMessage(notification));
        } catch (Exception e) {
            sendError(session, "Error loading game: " + e.getMessage());
        }
    }

    private void handleMove(Session session, MakeMoveCommand command) {
        try {
            // You will need to implement this method yourself
            // For now assume it updates the DB successfully
            int gameID = command.getGameID();

            GameData updated = dataAccess.getGame(gameID);  // re-fetch after update
            ServerMessage gameUpdate = new LoadGameMessage(updated.game());
            broadcast(gameID, gameUpdate);

            String player = getUsername(command.getAuthToken());
            String moveDesc = command.getMove().toString();
            broadcast(gameID, new NotificationMessage(player + " moved: " + moveDesc));

            // TODO: handle check/checkmate notifications
        } catch (Exception e) {
            sendError(session, "Invalid move: " + e.getMessage());
        }
    }

    private void handleLeave(Session session, LeaveCommand command) {
        int gameID = command.getGameID();
        gameSessions.getOrDefault(gameID, new ConcurrentHashMap<>()).remove(session);

        String player = getUsername(command.getAuthToken());
        broadcast(gameID, new NotificationMessage(player + " left the game."));
    }

    private void handleResign(Session session, ResignCommand command) {
        try {
            // You will need to implement this logic in your DAO
            String player = getUsername(command.getAuthToken());
            broadcast(command.getGameID(), new NotificationMessage(player + " resigned. Game over."));
        } catch (Exception e) {
            sendError(session, "Resign failed: " + e.getMessage());
        }
    }

    private void send(Session session, ServerMessage message) {
        try {
            session.getRemote().sendString(gson.toJson(message));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void sendError(Session session, String error) {
        send(session, new ErrorMessage(error));
    }

    private void broadcast(int gameID, ServerMessage message) {
        gameSessions.getOrDefault(gameID, new ConcurrentHashMap<>()).keySet().forEach(session -> send(session, message));
    }

    private void broadcastToOthers(int gameID, Session except, ServerMessage message) {
        gameSessions.getOrDefault(gameID, new ConcurrentHashMap<>()).keySet().stream()
                .filter(session -> !session.equals(except))
                .forEach(session -> send(session, message));
    }

    private String getUsername(String authToken) {
        try {
            return dataAccess.getUser(authToken).username();  // Update this to match your actual DAO method
        } catch (Exception e) {
            return "Unknown";
        }
    }
}

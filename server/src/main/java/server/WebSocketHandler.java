package server;

import chess.ChessGame;
import com.google.gson.Gson;
import dataaccess.DataAccess;
import dataaccess.DataAccessException;
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

    private static final ConcurrentHashMap<Integer, ConcurrentHashMap<Session, String>> GAMESESSIONS = new ConcurrentHashMap<>();
    private static final Gson GSON = new Gson();
    private final DataAccess dataAccess = new MySqlDataAccess();

    @OnWebSocketConnect
    public void onConnect(Session session) {
        System.out.println("WebSocket connected: " + session);
    }

    @OnWebSocketClose
    public void onClose(Session session, int statusCode, String reason) {
        System.out.println("WebSocket closed: " + session);
        GAMESESSIONS.values().forEach(map -> map.remove(session));
    }

    @OnWebSocketMessage
    public void onMessage(Session session, String message) {
        try {
            UserGameCommand baseCommand = GSON.fromJson(message, UserGameCommand.class);
            switch (baseCommand.getCommandType()) {
                case CONNECT -> handleConnect(session, GSON.fromJson(message, ConnectCommand.class));
                case MAKE_MOVE -> handleMove(session, GSON.fromJson(message, MakeMoveCommand.class));
                case LEAVE -> handleLeave(session, GSON.fromJson(message, LeaveCommand.class));
                case RESIGN -> handleResign(session, GSON.fromJson(message, ResignCommand.class));
                default -> sendError(session, "Unknown command type.");
            }
        } catch (Exception e) {
            sendError(session, "Error parsing command: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void handleConnect(Session session, ConnectCommand command) {
        try {
            var auth = dataAccess.getAuth(command.getAuthToken());
            if (auth == null) {
                sendError(session, "Invalid auth token.");
                session.close();  // ✅ cleanly close session if auth is bad
                return;
            }

            GAMESESSIONS.putIfAbsent(command.getGameID(), new ConcurrentHashMap<>());
            GAMESESSIONS.get(command.getGameID()).put(session, command.getAuthToken());

            GameData gameData = dataAccess.getGame(command.getGameID());
            ChessGame game = gameData.game();
            ServerMessage loadGame = new LoadGameMessage(game);
            send(session, loadGame);

            String role = (command.getPlayerColor() != null)
                    ? command.getPlayerColor().toString().toLowerCase()
                    : "observer";
            String notification = String.format("%s connected as %s", auth.username(), role);
            broadcastToOthers(command.getGameID(), session, new NotificationMessage(notification));

        } catch (DataAccessException e) {
            sendError(session, "Error validating auth or loading game: " + e.getMessage());
            try {
                session.close(); // best effort
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }




    private void handleMove(Session session, MakeMoveCommand command) {
        int gameID = command.getGameID();
        try {
            // Attempt to apply the move
            dataAccess.makeMove(command);

            // Now update and broadcast new game state
            GameData updated = dataAccess.getGame(gameID);
            broadcast(gameID, new LoadGameMessage(updated.game()));

            String player = getUsername(command.getAuthToken());
            String moveDesc = command.getMove().toString();
            broadcast(gameID, new NotificationMessage(player + " moved: " + moveDesc));

        } catch (Exception e) {
            sendError(session, "Invalid move: " + e.getMessage());
        }
    }



    private void handleLeave(Session session, LeaveCommand command) {
        int gameID = command.getGameID();
        GAMESESSIONS.getOrDefault(gameID, new ConcurrentHashMap<>()).remove(session);

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
            session.getRemote().sendString(GSON.toJson(message));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void sendError(Session session, String error) {
        send(session, new ErrorMessage(error));
    }

    private void broadcast(int gameID, ServerMessage message) {
        GAMESESSIONS.getOrDefault(gameID, new ConcurrentHashMap<>()).keySet().forEach(session -> send(session, message));
    }

    private void broadcastToOthers(int gameID, Session except, ServerMessage message) {
        GAMESESSIONS.getOrDefault(gameID, new ConcurrentHashMap<>()).keySet().stream()
                .filter(session -> !session.equals(except))
                .forEach(session -> send(session, message));
    }

    private String getUsername(String authToken) {
        try {
            return dataAccess.getAuth(authToken).username(); // ✅ simpler and consistent with the rest of your code
        } catch (Exception e) {
            System.out.println("❌ getAuth failed: " + e.getMessage());
            return "Unknown";
        }
    }

}

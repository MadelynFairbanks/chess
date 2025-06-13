package websocket;

import com.google.gson.Gson;
import model.GameData;
import org.eclipse.jetty.websocket.api.RemoteEndpoint;
import org.eclipse.jetty.websocket.api.Session;
import websocket.messages.ErrorMessage;
import websocket.messages.Notification;
import websocket.messages.ServerMessage;

import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

public class ConnectionManager {

    // 🕸️ Tracking who's connected to which game
    private final ConcurrentHashMap<Integer, ConcurrentHashMap<String, Connection>> connections = new ConcurrentHashMap<>();

    // 🏳️ Tracks if someone rage-quit (aka resigned)
    public ConcurrentHashMap<Integer, Boolean> resigned = new ConcurrentHashMap<>();

    // 🎮 Called when a player joins the game
    public void addConnection(String username, int gameID, Session session) {
        var connection = new Connection(username, session);
        connections.computeIfAbsent(gameID, id -> new ConcurrentHashMap<>()).put(username, connection);
    }

    // 💣 Nukes all connections for a specific game
    public void remove(int gameID) {
        connections.remove(gameID);
    }

    // ❌ Kicks a specific player from the party
    public void removePlayer(int gameID, String player) {
        synchronized (connections) {
            var gameConnections = connections.get(gameID);
            if (gameConnections != null) {
                gameConnections.remove(player);
            }
        }
    }

    // 📢 Blasts a message to everyone *except* one person (drama alert)
    public void broadcast(String excludeUsername, ServerMessage notification, int gameID) {
        System.out.println("📣 Broadcast is running for: " + excludeUsername);

        var removeList = new ArrayList<String>();
        var connectionList = connections.get(gameID);

        if (connectionList == null) {
            System.out.println("⚠️ No one is here to receive this message. Byeeee~");
            return;
        }

        for (var entry : connectionList.entrySet()) {
            String username = entry.getKey();
            Connection connection = entry.getValue();
            System.out.println("👀 Found connection for: " + username);

            if (!username.equals(excludeUsername)) {
                try {
                    String message = new Gson().toJson(notification);
                    System.out.println("📨 Sending message: " + message);
                    connection.send(message);
                } catch (IOException e) {
                    System.out.println("💀 Couldn't send to " + username);
                    removeList.add(username); // dead connection detected
                }
            }
        }

        // 🧼 Clean up any ghost connections
        for (String userToRemove : removeList) {
            connectionList.remove(userToRemove);
        }

        // 🧯 If nobody's left, ditch the whole game
        if (connectionList.isEmpty()) {
            remove(gameID);
        }
    }

    // 📬 Send a direct message to one specific user
    public void send(ServerMessage serverMessage, String username, int gameID) throws IOException {
        synchronized (connections) {
            String message = new Gson().toJson(serverMessage);
            System.out.println("📤 Sending to " + username + ": " + message);

            var connectionList = connections.get(gameID);
            if (connectionList != null && connectionList.containsKey(username)) {
                connectionList.get(username).send(message);
            } else {
                System.out.println("🚫 Connection not found for user: " + username);
            }
        }
    }

    // 🛑 Send an error message when things go 💥
    public void sendError(RemoteEndpoint session, String errorMessage) {
        synchronized (connections) {
            var gson = new Gson();
            var message = new ServerMessage(ServerMessage.ServerMessageType.ERROR, null, null, gson.toJson(errorMessage));
            String serverMessage = gson.toJson(message);

            try {
                session.sendString(serverMessage);
                System.out.println("⚠️ Error sent to client: " + errorMessage);
            } catch (IOException e) {
                System.out.println("❗ Error sending error (how meta)");
            }
        }
    }
}

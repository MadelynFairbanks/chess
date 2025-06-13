package websocket;

import com.google.gson.Gson;
import model.GameData;
import org.eclipse.jetty.websocket.api.RemoteEndpoint;
import websocket.messages.ErrorMessage;
import websocket.messages.Notification;

import org.eclipse.jetty.websocket.api.Session;
import websocket.messages.ServerMessage;

import java.io.IOException;
import java.util.ArrayList;

import java.util.concurrent.ConcurrentHashMap;

public class ConnectionManager {

        private final ConcurrentHashMap<Integer, ConcurrentHashMap<String, Connection>> connections = new ConcurrentHashMap<>();
        public ConcurrentHashMap<Integer, Boolean> resigned = new ConcurrentHashMap<>();

        public void addConnection (String username,int gameID, Session session){
                var connection = new Connection(username, session);
                connections.computeIfAbsent(gameID, id -> new ConcurrentHashMap<>()).put(username, connection);
    }

        public void remove ( int gameID){
        connections.remove(gameID);
    }

        public void removePlayer ( int gameID, String player){
        synchronized (connections) {
            ConcurrentHashMap<String, Connection> gameConnections = connections.get(gameID);
            if (gameConnections != null) {
                gameConnections.remove(player);
            }
        }
    }

        public void broadcast (String excludeUsername, ServerMessage notification,int gameID){
            System.out.println("Broadcast is runnign for " + excludeUsername);
            var removeList = new ArrayList<String>();
            var connectionList = connections.get(gameID);
            if (connectionList == null) {
                System.out.println("I'm dying!!!!!");
                return;
            }
            for (var entry : connectionList.entrySet()) {
                    String username = entry.getKey();
                    System.out.println(username + " in connection list");
                    Connection connection = entry.getValue();
                    if (!username.equals(excludeUsername)) {
                        try {
                            Gson gson = new Gson();
                            String message = gson.toJson(notification);
                            System.out.println("message to be sent: " + message);
                            connection.send(message);
                            System.out.println("message was sent to all but: " + excludeUsername);

                        } catch (IOException e) {
                            removeList.add(username);
                        }
                }
            }

            for (String userToRemove : removeList) {
                connectionList.remove(userToRemove);
            }
            if (connectionList.isEmpty()) {
                remove(gameID);
            }
    }


        public void send (ServerMessage serverMessage, String username,int gameID) throws IOException {
            synchronized (connections) {

                Gson gson = new Gson();
                String game = gson.toJson(serverMessage, ServerMessage.class);

                var connectionList = connections.get(gameID);
                var connection = connectionList.get(username);
                System.out.println("message being sent: " + game);
                connection.send(game);
            }
        }


            public void sendError (RemoteEndpoint session, String errorMessage){
                synchronized (connections) {
                    Gson gson = new Gson();
                    ServerMessage message = new ServerMessage(ServerMessage.ServerMessageType.ERROR, null, null, gson.toJson(errorMessage));
                    String serverMessage = gson.toJson(message);

                    try {
                        session.sendString(serverMessage);
                    } catch (IOException e) {
                        System.out.println("Error: system produced error printing error message");
                    }
                }
            }

}

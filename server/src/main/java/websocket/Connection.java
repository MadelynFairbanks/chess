package websocket;

import org.eclipse.jetty.websocket.api.Session;
import java.io.IOException;

// 📡 Represents a user's WebSocket connection
public class Connection {

    // 🧑 Username of the person on the other end of the wire
    public String username;

    // 🔌 This is their actual connection session
    public Session session;

    // 🎉 Constructor sets it all up
    public Connection(String username, Session session) {
        this.username = username;
        this.session = session;
    }

    // 📬 Send a message if the line’s still open
    public void send(String msg) throws IOException {
        if (session.isOpen()) {
            session.getRemote().sendString(msg); // 🚀 Yeeting that message across the internet
        } else {
            // 🚫 Uh-oh, the line is dead. Time to ghost.
            System.out.printf("❌ Session for %s is closed or null. Message not sent.%n", username);
        }
    }
}

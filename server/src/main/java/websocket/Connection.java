package websocket;

import org.eclipse.jetty.websocket.api.Session;
import java.io.IOException;

public class Connection {
    public String username;
    public Session session;

    public Connection(String username, Session session) {
        this.username = username;
        this.session = session;
    }

    public void send(String msg) throws IOException {
        if (session.isOpen()) {
            session.getRemote().sendString(msg);
        } else {
            System.out.printf("Session for %s is closed or null. Message not sent.%n", username);
        }
    }
}

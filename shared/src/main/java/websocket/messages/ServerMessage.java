package websocket.messages;

import model.GameData;

import java.util.Objects;

/**
 * Represents a Message the server can send through a WebSocket
 *
 * Note: You can add to this class, but you should not alter the existing
 * methods.
 */
public class ServerMessage {
    GameData game;
    ServerMessageType serverMessageType;
    Object message;
    Object errorMessage;

    public enum ServerMessageType {
        LOAD_GAME,
        ERROR,
        NOTIFICATION
    }

    public ServerMessage(ServerMessageType type, GameData game, Object message, Object errorMessage) {
        this.game = game;
        this.serverMessageType = type;
        this.message = message;
        this.errorMessage = errorMessage;
    }


    public ServerMessageType getServerMessageType() {
        return this.serverMessageType;
    }
    public Object getServerMessage() {
        return this.message;
    }
    public Object getErrorMessage(){
        return this.errorMessage;
    }
    public GameData getGame(){
        return this.game;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ServerMessage)) {
            return false;
        }
        ServerMessage that = (ServerMessage) o;
        return getServerMessageType() == that.getServerMessageType();
    }

    @Override
    public int hashCode() {
        return Objects.hash(getServerMessageType());
    }
}

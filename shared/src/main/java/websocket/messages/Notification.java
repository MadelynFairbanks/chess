package websocket.messages;

import com.google.gson.Gson;

public record Notification(Type type, String message) {
    public enum Type {
        GEN,
        JOIN_GAME,
        OBSERVE_GAME,
        MOVE_MADE,
        LEFT_GAME,
        RESIGNATION,
        CHECK,
        CHECKMATE
    }

    public String toString() {
        return new Gson().toJson(this);
    }

    public String getMessage() {
        return message;
    }

}
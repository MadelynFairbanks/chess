package dataaccess;

/**
 * Indicates there was an error connecting to the database
 */
public class DataAccessException extends Exception {
    public DataAccessException(String message) {
        super(prefix(message));
    }

    public DataAccessException(String message, Throwable ex) {
        super(prefix(message), ex);
    }

    private static String prefix(String message) {
        if (message.toLowerCase().contains("error")) {
            return message;
        }
        return "Error: " + message;
    }
}


import server.Server;
import dataaccess.DatabaseManager;
import dataaccess.DataAccessException;

public class Main {
    public static void main(String[] args) {
        try {
            DatabaseManager.configureDatabase(); // ✅ THIS is the missing link
            System.out.println("Database configured and ready.");
        } catch (DataAccessException e) {
            System.out.println("Failed to configure database:");
            e.printStackTrace();
        }

        new Server().run(8080);
    }
}


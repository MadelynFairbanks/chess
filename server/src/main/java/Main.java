import server.Server;
import dataaccess.DatabaseManager;
import dataaccess.DataAccessException;

public class Main {
    public static void main(String[] args) {
        try {
            DatabaseManager.createDatabase();
            System.out.println("✅ Database created or already exists.");
        } catch (DataAccessException e) {
            System.out.println("❌ Failed to create database:");
            e.printStackTrace();
        }
        new Server().run(8080);
    }
}

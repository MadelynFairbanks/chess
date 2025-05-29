import dataaccess.DatabaseManager;
import java.sql.Connection;

public class DatabaseTest {
    public static void main(String[] args) {
        try (Connection conn = DatabaseManager.getConnection()) {
            System.out.println("✅ Java-MySQL connection successful!");
        } catch (Exception e) {
            System.out.println("❌ Java-MySQL connection failed:");
            e.printStackTrace();
        }
    }
}

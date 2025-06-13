package dataaccess;

import java.sql.*;
import java.util.Properties;

public class DatabaseManager {

    private static String databaseName;
    private static String dbUsername;
    private static String dbPassword;
    private static String connectionUrl;

    // 🧾 Static initializer — loads db.properties from the resources folder
    static {
        loadPropertiesFromResources();
    }

    /**
     * 🏗️ Creates the database if it doesn’t already exist.
     * Basically makes sure MySQL is ready to catch these tables.
     */
    public static void createDatabase() throws DataAccessException {
        var statement = "CREATE DATABASE IF NOT EXISTS " + databaseName;

        try (var conn = DriverManager.getConnection(connectionUrl, dbUsername, dbPassword);
             var preparedStatement = conn.prepareStatement(statement)) {

            preparedStatement.executeUpdate();

        } catch (SQLException ex) {
            throw new DataAccessException("⚠️ Failed to create database", ex.getErrorCode());
        }
    }

    /**
     * 🔌 Creates a connection to the database and sets the catalog to your db name.
     *
     * Use like this:
     * <pre>
     * try (var conn = DatabaseManager.getConnection()) {
     *     // do SQL things
     * }
     * </pre>
     *
     * Connections should be short-lived — use try-with-resources to avoid 💀 leaks!
     */
    public static Connection getConnection() throws DataAccessException {
        try {
            // ☕ Don’t wrap this one with try-with-resources; you return it to the user
            var conn = DriverManager.getConnection(connectionUrl, dbUsername, dbPassword);
            conn.setCatalog(databaseName);
            return conn;
        } catch (SQLException ex) {
            throw new DataAccessException("🔌 Failed to get DB connection", ex.getErrorCode());
        }
    }

    // 🔎 Load db.properties from your resources folder
    private static void loadPropertiesFromResources() {
        try (var propStream = Thread.currentThread().getContextClassLoader().getResourceAsStream("db.properties")) {
            if (propStream == null) {
                throw new Exception("🫠 Couldn’t load db.properties");
            }

            Properties props = new Properties();
            props.load(propStream);
            loadProperties(props);

        } catch (Exception ex) {
            throw new RuntimeException("😵 Failed to process db.properties", ex);
        }
    }

    // 🔐 Set DB connection variables from the .properties file
    private static void loadProperties(Properties props) {
        databaseName = props.getProperty("db.name");
        dbUsername   = props.getProperty("db.user");
        dbPassword   = props.getProperty("db.password");

        var host = props.getProperty("db.host");
        var port = Integer.parseInt(props.getProperty("db.port"));

        connectionUrl = String.format("jdbc:mysql://%s:%d", host, port);
    }
}

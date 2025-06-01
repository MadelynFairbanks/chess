package dataaccess;

import java.sql.*;
import java.util.Properties;

public class DatabaseManager {
    private static String databaseName;
    private static String dbUsername;
    private static String dbPassword;
    private static String connectionUrl;

    /*
     * Load the database information for the db.properties file.
     */
    static {
        loadPropertiesFromResources();
    }

    /**
     * Creates the database if it does not already exist.
     */
    static public void createDatabase() throws DataAccessException {
        var statement = "CREATE DATABASE IF NOT EXISTS " + databaseName;
        try (var conn = DriverManager.getConnection(connectionUrl, dbUsername, dbPassword);
             var preparedStatement = conn.prepareStatement(statement)) {
            preparedStatement.executeUpdate();
        } catch (SQLException ex) {
            throw new DataAccessException("failed to create database", ex);
        }
    }
    public static void createTables() throws DataAccessException {
        try (var conn = getConnection();
             var stmt = conn.createStatement()) {

            // users table
            stmt.executeUpdate("""
            CREATE TABLE IF NOT EXISTS users (
                username VARCHAR(50) PRIMARY KEY,
                password VARCHAR(100) NOT NULL,
                email VARCHAR(100) NOT NULL
            );
        """);

            // auth_tokens table
            stmt.executeUpdate("""
            CREATE TABLE IF NOT EXISTS auth_tokens (
                token VARCHAR(100) PRIMARY KEY,
                username VARCHAR(50) NOT NULL,
                FOREIGN KEY (username) REFERENCES users(username) ON DELETE CASCADE
            );
        """);

            // games table
            stmt.executeUpdate("""
            CREATE TABLE IF NOT EXISTS games (
                gameID INT PRIMARY KEY AUTO_INCREMENT,
                gameName VARCHAR(100) NOT NULL,
                whiteUsername VARCHAR(50),
                blackUsername VARCHAR(50),
                game TEXT NOT NULL,
                FOREIGN KEY (whiteUsername) REFERENCES users(username) ON DELETE SET NULL,
                FOREIGN KEY (blackUsername) REFERENCES users(username) ON DELETE SET NULL
            );
        """);

        } catch (SQLException e) {
            throw new DataAccessException("Failed to create tables", e);
        }
    }
    /**
     * Create a connection to the database and sets the catalog based upon the
     * properties specified in db.properties. Connections to the database should
     * be short-lived, and you must close the connection when you are done with it.
     * The easiest way to do that is with a try-with-resource block.
     * <br/>
     * <code>
     * try (var conn = DatabaseManager.getConnection()) {
     * // execute SQL statements.
     * }
     * </code>
     */
    //I added this public down here because you told me to, Chat
    public static Connection getConnection() throws DataAccessException {
        try {
            //do not wrap the following line with a try-with-resources
            var conn = DriverManager.getConnection(connectionUrl, dbUsername, dbPassword);
            conn.setCatalog(databaseName);
            return conn;
        } catch (SQLException ex) {
            throw new DataAccessException("failed to get connection", ex);
        }
    }

    private static void loadPropertiesFromResources() {
        Properties props = new Properties();

        try (var propStream = Thread.currentThread().getContextClassLoader().getResourceAsStream("db.properties")) {
            if (propStream != null) {
                props.load(propStream);
            }
        } catch (Exception e) {
            System.out.println("No db.properties file found — attempting to use environment variables.");
        }

        // Prefer environment variables, fallback to props, then to hardcoded defaults
        databaseName = System.getenv("DB_NAME");
        if (databaseName == null) {
            databaseName = props.getProperty("db.name", "chess");
        }

        dbUsername = System.getenv("DB_USER");
        if (dbUsername == null) {
            dbUsername = props.getProperty("db.user", "root");
        }

        dbPassword = System.getenv("DB_PASSWORD");
        if (dbPassword == null) {
            dbPassword = props.getProperty("db.password", "MyChessRoot2025!");
        }

        var host = System.getenv("DB_HOST");
        if (host == null) {
            host = props.getProperty("db.host", "localhost");
        }

        var portStr = System.getenv("DB_PORT");
        if (portStr == null) {
            portStr = props.getProperty("db.port", "3306");
        }

        try {
            int port = Integer.parseInt(portStr);
            connectionUrl = String.format("jdbc:mysql://%s:%d", host, port);
        } catch (NumberFormatException e) {
            throw new RuntimeException("Invalid port number for database: " + portStr);
        }
    }






    private static void loadProperties(Properties props) {
        databaseName = props.getProperty("db.name");
        dbUsername = props.getProperty("db.user");
        dbPassword = props.getProperty("db.password");

        var host = props.getProperty("db.host");
        var port = Integer.parseInt(props.getProperty("db.port"));
        connectionUrl = String.format("jdbc:mysql://%s:%d", host, port);
    }
}

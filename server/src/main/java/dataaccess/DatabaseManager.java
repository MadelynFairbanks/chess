package dataaccess;

import java.sql.*;
import java.util.Properties;

public class DatabaseManager {
    private static String databaseName;
    private static String dbUsername;
    private static String dbPassword;
    private static String connectionUrl;
    private static DataAccess dataAccess;

    static {
        loadPropertiesFromResources();
    }

    public static void configureDatabase() throws DataAccessException {
        String url = System.getenv("DB_URL");
        String user = System.getenv("DB_USER");
        String password = System.getenv("DB_PASSWORD");

        if (url == null || user == null || password == null) {
            throw new DataAccessException("Missing required DB environment variables.");
        }

        // Parse DB name from full URL
        databaseName = url.substring(url.lastIndexOf("/") + 1);
        String noDbUrl = url.substring(0, url.lastIndexOf("/") + 1);
        dbUsername = user;
        dbPassword = password;
        connectionUrl = noDbUrl;

        // Make sure the database exists
        try (Connection conn = DriverManager.getConnection(noDbUrl, user, password);
             Statement stmt = conn.createStatement()) {
            stmt.executeUpdate("CREATE DATABASE IF NOT EXISTS " + databaseName);
        } catch (SQLException e) {
            throw new DataAccessException("Failed to create database: " + databaseName, e);
        }

        // Switch to actual DB and build tables
        connectionUrl = url;
        dataAccess = new MySqlDataAccess();
        createTables();
    }

    public static DataAccess getDataAccess() {
        return dataAccess;
    }

    public static Connection getConnection() throws DataAccessException {
        try {
            Connection conn = DriverManager.getConnection(connectionUrl, dbUsername, dbPassword);
            conn.setCatalog(databaseName);
            return conn;
        } catch (SQLException ex) {
            throw new DataAccessException("failed to get connection", ex);
        }
    }

    private static void createTables() throws DataAccessException {
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {

            stmt.executeUpdate("""
                CREATE TABLE IF NOT EXISTS users (
                    username VARCHAR(50) PRIMARY KEY,
                    password VARCHAR(100) NOT NULL,
                    email VARCHAR(100) NOT NULL
                );
            """);

            stmt.executeUpdate("""
                CREATE TABLE IF NOT EXISTS auth_tokens (
                    token VARCHAR(100) PRIMARY KEY,
                    username VARCHAR(50) NOT NULL,
                    FOREIGN KEY (username) REFERENCES users(username) ON DELETE CASCADE
                );
            """);

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

    private static void loadPropertiesFromResources() {
        Properties props = new Properties();

        try (var propStream = Thread.currentThread().getContextClassLoader().getResourceAsStream("db.properties")) {
            if (propStream != null) {
                props.load(propStream);
            }
        } catch (Exception e) {
            System.out.println("No db.properties file found — attempting to use environment variables.");
        }

        databaseName = System.getenv("DB_NAME");
        if (databaseName == null) databaseName = props.getProperty("db.name", "chess");

        dbUsername = System.getenv("DB_USER");
        if (dbUsername == null) dbUsername = props.getProperty("db.user", "root");

        dbPassword = System.getenv("DB_PASSWORD");
        if (dbPassword == null) dbPassword = props.getProperty("db.password", "MyChessRoot2025!");

        var host = System.getenv("DB_HOST");
        if (host == null) host = props.getProperty("db.host", "localhost");

        var portStr = System.getenv("DB_PORT");
        if (portStr == null) portStr = props.getProperty("db.port", "3306");

        try {
            int port = Integer.parseInt(portStr);
            connectionUrl = String.format("jdbc:mysql://%s:%d", host, port);
        } catch (NumberFormatException e) {
            throw new RuntimeException("Invalid port number: " + portStr);
        }
    }
}

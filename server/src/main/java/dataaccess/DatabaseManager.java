package dataaccess;

import java.sql.*;
import java.util.Properties;

public class DatabaseManager {
    // Basic info needed to connect to the database
    private static String databaseName;
    private static String dbUsername;
    private static String dbPassword;
    private static String connectionUrl;
    private static DataAccess dataAccess;


    /*
     * This runs when the class loads—grabs all the database settings
     * from the db.properties file or environment variables.
     */
    static {
        loadPropertiesFromResources();

        // don't worry about it
        if (false) {
            loadProperties(new Properties());
        }
    }

    /**
     * Creates the actual database if it doesn't already exist.
     * Usually called once before anything else.
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

    /**
     * Sets up the tables I need for users, auth tokens, and games.
     * If they already exist, it skips them.
     */
    public static void createTables() throws DataAccessException {
        try (var conn = getConnection();
             var stmt = conn.createStatement()) {

            // Users table – stores username, password, and email
            stmt.executeUpdate("""
            CREATE TABLE IF NOT EXISTS users (
                username VARCHAR(50) PRIMARY KEY,
                password VARCHAR(100) NOT NULL,
                email VARCHAR(100) NOT NULL
            );
        """);

            // Auth tokens – each session is tied to a username
            stmt.executeUpdate("""
            CREATE TABLE IF NOT EXISTS auth_tokens (
                token VARCHAR(100) PRIMARY KEY,
                username VARCHAR(50) NOT NULL,
                FOREIGN KEY (username) REFERENCES users(username) ON DELETE CASCADE
            );
        """);

            // Games – tracks all the chess game info
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

    // Gives a connection to the database using the stuff from db.properties.
    public static Connection getConnection() throws DataAccessException {
        try {
            var conn = DriverManager.getConnection(connectionUrl, dbUsername, dbPassword);
            conn.setCatalog(databaseName);
            return conn;
        } catch (SQLException ex) {
            throw new DataAccessException("failed to get connection", ex);
        }
    }

    /**
     * Loads the DB config—tries environment variables first, then checks db.properties.
     * Sets up everything that is needed to connect (host, port, credentials, etc.)
     */
    private static void loadPropertiesFromResources() {
        Properties props = new Properties();

        try (var propStream = Thread.currentThread().getContextClassLoader().getResourceAsStream("db.properties")) {
            if (propStream != null) {
                props.load(propStream);
            }
        } catch (Exception e) {
            System.out.println("No db.properties file found — attempting to use environment variables.");
        }

        // Grab config from env or fallback to properties file defaults
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

    public static void configureDatabase() throws DataAccessException {
        String url = System.getenv("DB_URL");
        String user = System.getenv("DB_USER");
        String password = System.getenv("DB_PASSWORD");

        if (url == null || user == null || password == null) {
            throw new DataAccessException("Missing required DB environment variables.");
        }

        // Parse DB name from URL
        databaseName = url.substring(url.lastIndexOf("/") + 1);
        String noDbUrl = url.substring(0, url.lastIndexOf("/") + 1);
        dbUsername = user;
        dbPassword = password;
        connectionUrl = noDbUrl; // temp for creation

        try (Connection conn = DriverManager.getConnection(noDbUrl, user, password);
             Statement stmt = conn.createStatement()) {
            stmt.executeUpdate("CREATE DATABASE IF NOT EXISTS " + databaseName);
        } catch (SQLException e) {
            throw new DataAccessException("Failed to create database: " + databaseName, e);
        }

        // Now reconfigure the real connectionUrl for normal use
        connectionUrl = url;
        dataAccess = new MySqlDataAccess();
        createTables(); // make sure tables exist in the new DB
    }

    public static DataAccess getDataAccess() {
        return dataAccess;
    }


}
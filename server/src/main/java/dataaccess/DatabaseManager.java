package dataaccess;

import java.sql.*;
import java.util.Properties;
import java.nio.file.Files;
import java.nio.file.Path;
import java.io.IOException;


public class DatabaseManager {


    /*
     * Load the database information for the db.properties file.
     */
    //static {
        //loadPropertiesFromResources();
    //}

    /**
     * Creates the database if it does not already exist.
     */
    public static void createDatabase() throws DataAccessException {
        try {
            String user = System.getenv("DB_USER");
            String password = System.getenv("DB_PASSWORD");
            String database = System.getenv("DB_NAME");
            String host = System.getenv("DB_HOST");
            String port = System.getenv("DB_PORT");

            if (user == null || password == null || database == null) {
                var props = new Properties();
                try (var in = Files.newInputStream(Path.of("server/src/main/resources/db.properties"))) {
                    props.load(in);
                }
                user = props.getProperty("db.user", "root");
                password = props.getProperty("db.password", "MyChessRoot2025!");
                database = props.getProperty("db.name", "chess");
                host = props.getProperty("db.host", "localhost");
                port = props.getProperty("db.port", "3306");
            }

            if (host == null) host = "localhost";
            if (port == null) port = "3306";

            String connectionUrl = String.format("jdbc:mysql://%s:%s", host, port);
            try (var conn = DriverManager.getConnection(connectionUrl, user, password);
                 var preparedStatement = conn.prepareStatement("CREATE DATABASE IF NOT EXISTS " + database)) {
                preparedStatement.executeUpdate();
            }

        } catch (IOException | SQLException e) {
            throw new DataAccessException("Failed to create database", e);
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
            String user = System.getenv("DB_USER");
            String password = System.getenv("DB_PASSWORD");
            String database = System.getenv("DB_NAME");
            String host = System.getenv("DB_HOST");
            String port = System.getenv("DB_PORT");

            // Fallback to db.properties
            if (user == null || password == null || database == null) {
                var props = new Properties();
                try (var in = Files.newInputStream(Path.of("server/src/main/resources/db.properties"))) {
                    props.load(in);
                }
                user = props.getProperty("db.user", "root");
                password = props.getProperty("db.password", "MyChessRoot2025!");
                database = props.getProperty("db.name", "chess");
                host = props.getProperty("db.host", "localhost");
                port = props.getProperty("db.port", "3306");
            }

            if (host == null) host = "localhost";
            if (port == null) port = "3306";

            String connectionUrl = String.format("jdbc:mysql://%s:%s/%s", host, port, database);
            return DriverManager.getConnection(connectionUrl, user, password);

        } catch (IOException | SQLException e) {
            throw new DataAccessException("Unable to connect to database", e);
        }
    }

}

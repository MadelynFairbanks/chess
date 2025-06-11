package dataaccess;

import model.UserData;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;


import org.mindrot.jbcrypt.BCrypt;

public class MySqlUserDAO {

    // Adds a new user to the database (and hashes the password like a responsible dev)
    public void insertUser(UserData user) throws DataAccessException {
        String sql = "INSERT INTO users (username, password, email) VALUES (?, ?, ?)";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            // Salting + hashing the password because storing raw passwords is a no-go
            String hashedPassword = BCrypt.hashpw(user.password(), BCrypt.gensalt());

            stmt.setString(1, user.username());
            stmt.setString(2, hashedPassword);
            stmt.setString(3, user.email());

            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("Unable to insert user", e);
        }
    }

    // Clears all users from the DB (good for testing or when we want a clean slate)
    public void clear() throws DataAccessException {
        try (Connection conn = DatabaseManager.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.executeUpdate("DELETE FROM users");
        } catch (SQLException e) {
            throw new DataAccessException("Unable to clear users", e);
        }
    }

    // Looks up a user by username and returns their data (null if they don’t exist)
    public UserData findUser(String username) throws DataAccessException {
        String sql = "SELECT * FROM users WHERE username = ?";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                String hashedPassword = rs.getString("password");
                String email = rs.getString("email");
                return new UserData(username, hashedPassword, email);
            } else {
                return null;
            }
        } catch (SQLException e) {
            throw new DataAccessException("Unable to find user", e);
        }
    }

    // Verifies the given password against the hashed one in the DB
    public boolean verifyPassword(String username, String inputPassword) throws DataAccessException {
        UserData user = findUser(username);
        if (user == null) {
            return false; // No such user
        }

        // BCrypt does the heavy lifting to check the hash
        return BCrypt.checkpw(inputPassword, user.password());
    }
}


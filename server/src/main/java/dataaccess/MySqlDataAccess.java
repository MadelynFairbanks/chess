package dataaccess;

import com.google.gson.Gson;
import model.AuthData;
import model.UserData;
import model.GameData;

import java.sql.*;
import java.util.List;
import websocket.commands.MakeMoveCommand;
import chess.ChessMove;
import chess.ChessGame;



public class MySqlDataAccess implements DataAccess {
    private final MySqlUserDAO userDAO = new MySqlUserDAO();
    private final MySqlAuthTokenDAO authDAO = new MySqlAuthTokenDAO();
    private final MySqlGameDAO gameDAO = new MySqlGameDAO();


    @Override
    public List<GameData> listGames() throws DataAccessException {
        return gameDAO.listGames();
    }

    @Override
    public void clear() throws DataAccessException {
        gameDAO.clear();
        userDAO.clear();
        authDAO.clear();
    }

    @Override
    public int createGame(GameData game) throws DataAccessException {
        return gameDAO.insertGame(game);
    }

    @Override
    public GameData getGame(int gameID) throws DataAccessException {
        return gameDAO.findGame(gameID);
    }

    @Override
    public void updateGame(GameData game) throws DataAccessException {
        gameDAO.updateGame(game);
    }

    @Override
    public void createUser(UserData user) throws DataAccessException {
        userDAO.insertUser(user);
    }

    @Override
    public UserData getUser(String username) throws DataAccessException {
        return userDAO.findUser(username);
    }

    @Override
    public void createAuth(AuthData auth) throws DataAccessException {
        authDAO.insertAuth(auth);
    }

    @Override
    public AuthData getAuth(String token) throws DataAccessException {
        return authDAO.findAuth(token);
    }

    @Override
    public void deleteAuth(String token) throws DataAccessException {
        authDAO.deleteAuth(token);
    }

    @Override
    public UserData getUserByAuthToken(String authToken) throws DataAccessException {
        String sql = """
        SELECT u.username, u.password, u.email
        FROM users u
        JOIN auth a ON u.username = a.username
        WHERE a.authToken = ?;
    """;

        try (var conn = DatabaseManager.getConnection();
             var stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, authToken);

            try (var rs = stmt.executeQuery()) {
                if (rs.next()) {
                    String username = rs.getString("username");
                    String password = rs.getString("password");
                    String email = rs.getString("email");
                    return new UserData(username, password, email);
                } else {
                    throw new DataAccessException("Invalid auth token.");
                }
            }

        } catch (SQLException e) {
            throw new DataAccessException("Error fetching user from auth token", e);
        }
    }

    @Override
    public void makeMove(MakeMoveCommand command) throws DataAccessException {
        int gameID = command.getGameID();
        ChessMove move = command.getMove();
        String authToken = command.getAuthToken();

        GameData gameData = getGame(gameID);
        ChessGame game = gameData.game();

        // Check if user is part of this game and has the right color
        UserData user = getUserByAuthToken(authToken);
        String username = user.username();

        var white = gameData.whiteUsername();
        var black = gameData.blackUsername();
        var currentTurn = game.getTeamTurn();

        if (currentTurn == ChessGame.TeamColor.WHITE && !username.equals(white)) {
            throw new DataAccessException("Not your turn (WHITE).");
        }
        if (currentTurn == ChessGame.TeamColor.BLACK && !username.equals(black)) {
            throw new DataAccessException("Not your turn (BLACK).");
        }

        try {
            game.makeMove(move);
        } catch (Exception e) {
            throw new DataAccessException("Illegal move: " + e.getMessage());
        }

        // Save updated game state
        String sql = "UPDATE games SET game = ?, gameOver = ? WHERE gameID = ?";
        try (var conn = DatabaseManager.getConnection();
             var stmt = conn.prepareStatement(sql)) {

            String updatedJson = new Gson().toJson(game);
            boolean gameOver = game.isGameOver();

            stmt.setString(1, updatedJson);
            stmt.setBoolean(2, gameOver);
            stmt.setInt(3, gameID);
            stmt.executeUpdate();

        } catch (Exception e) {
            throw new DataAccessException("Failed to update game after move.", e);
        }
    }


}

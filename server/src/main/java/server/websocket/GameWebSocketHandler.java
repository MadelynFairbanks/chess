package server.websocket;

import chess.ChessGame;
import com.google.gson.Gson;
import dataaccess.MySqlAuthDAO;
import dataaccess.MySqlGameDAO;
import model.AuthData;
import model.GameData;
import websocket.commands.*;
import websocket.messages.*;
import org.eclipse.jetty.websocket.api.*;
import org.eclipse.jetty.websocket.api.annotations.*;

import java.util.*;

@WebSocket
public class GameWebSocketHandler {

    private final Map<Session, String> sessionToUsername = new HashMap<>();
    private final Map<String, Session> usernameToSession = new HashMap<>();
    private final Map<Integer, Set<String>> gameToUsers = new HashMap<>();

    private final Gson gson = new Gson();

    private final MySqlAuthDAO authDAO = new MySqlAuthDAO();
    private final MySqlGameDAO gameDAO = new MySqlGameDAO();

    @OnWebSocketConnect
    public void onConnect(Session session) {
        // No-op for now
    }

    @OnWebSocketClose
    public void onClose(Session session, int statusCode, String reason) {
        String username = sessionToUsername.remove(session);
        if (username != null) {
            usernameToSession.remove(username);
        }
    }

    @OnWebSocketMessage
    public void onMessage(Session session, String messageJson) {
        try {
            UserGameCommand base = gson.fromJson(messageJson, UserGameCommand.class);
            switch (base.getCommandType()) {
                case CONNECT -> handleConnect(session, gson.fromJson(messageJson, ConnectCommand.class));
                case MAKE_MOVE -> handleMakeMove(session, gson.fromJson(messageJson, MakeMoveCommand.class));
                case LEAVE -> handleLeave(session, gson.fromJson(messageJson, LeaveCommand.class));
                case RESIGN -> handleResign(session, gson.fromJson(messageJson, ResignCommand.class));
            }
        } catch (Exception e) {
            send(session, new ErrorMessage("Error: " + e.getMessage()));
        }
    }

    // ========== HANDLER METHODS ==========

    private void handleConnect(Session session, ConnectCommand cmd) {
        try {
            AuthData auth = authDAO.findAuth(cmd.getAuthToken());
            GameData game = gameDAO.findGame(cmd.getGameID());

            if (auth == null || game == null) {
                send(session, new ErrorMessage("Error: Invalid auth or game ID"));
                return;
            }

            String username = auth.username();
            sessionToUsername.put(session, username);
            usernameToSession.put(username, session);
            gameToUsers.computeIfAbsent(cmd.getGameID(), k -> new HashSet<>()).add(username);

            send(session, new LoadGameMessage(game.game()));
            broadcastExcept(cmd.getGameID(), username, new NotificationMessage(username + " connected to game."));

        } catch (Exception e) {
            send(session, new ErrorMessage("Error during connect: " + e.getMessage()));
        }
    }

    private void handleMakeMove(Session session, MakeMoveCommand cmd) {
        try {
            AuthData auth = authDAO.findAuth(cmd.getAuthToken());
            GameData gameData = gameDAO.findGame(cmd.getGameID());

            if (auth == null || gameData == null) {
                send(session, new ErrorMessage("Error: Invalid move - bad auth or game ID"));
                return;
            }

            if (gameData.gameOver()) {
                send(session, new ErrorMessage("Error: Game is already over"));
                return;
            }

            ChessGame game = gameData.game();
            var move = cmd.getMove();

            // Validate turn
            ChessGame.TeamColor playerColor = gameData.whiteUsername().equals(auth.username())
                    ? ChessGame.TeamColor.WHITE
                    : gameData.blackUsername().equals(auth.username())
                    ? ChessGame.TeamColor.BLACK
                    : null;

            if (playerColor == null || !game.getTeamTurn().equals(playerColor)) {
                send(session, new ErrorMessage("Error: Not your turn"));
                return;
            }

            if (!game.validMoves(move.getStartPosition()).contains(move)) {
                send(session, new ErrorMessage("Error: Invalid move"));
                return;
            }

            game.makeMove(move);
            gameDAO.updateGame(new GameData(
                    gameData.gameID(),
                    gameData.whiteUsername(),
                    gameData.blackUsername(),
                    gameData.gameName(),
                    game,
                    false // Game is not over yet
            ));


            broadcastAll(cmd.getGameID(), new LoadGameMessage(game));
            broadcastExcept(cmd.getGameID(), auth.username(),
                    new NotificationMessage(auth.username() + " moved from " + move.getStartPosition() + " to " + move.getEndPosition()));

            if (game.isInCheckmate(game.getTeamTurn())) {
                broadcastAll(cmd.getGameID(), new NotificationMessage(game.getTeamTurn() + " is in checkmate!"));
            } else if (game.isInCheck(game.getTeamTurn())) {
                broadcastAll(cmd.getGameID(), new NotificationMessage(game.getTeamTurn() + " is in check."));
            }

        } catch (Exception e) {
            send(session, new ErrorMessage("Error during move: " + e.getMessage()));
        }
    }

    private void handleLeave(Session session, LeaveCommand cmd) {
        try {
            String username = sessionToUsername.remove(session);
            if (username != null) {
                usernameToSession.remove(username);
                gameToUsers.getOrDefault(cmd.getGameID(), new HashSet<>()).remove(username);

                GameData gameData = gameDAO.findGame(cmd.getGameID());
                if (gameData != null) {
                    if (username.equals(gameData.whiteUsername())) {
                        gameDAO.setWhiteUsername(cmd.getGameID(), null);
                    } else if (username.equals(gameData.blackUsername())) {
                        gameDAO.setBlackUsername(cmd.getGameID(), null);
                    }
                }

                broadcastExcept(cmd.getGameID(), username,
                        new NotificationMessage(username + " left the game."));
            }
        } catch (Exception e) {
            send(session, new ErrorMessage("Error during leave: " + e.getMessage()));
        }
    }


    private void handleResign(Session session, ResignCommand cmd) {
        try {
            AuthData auth = authDAO.findAuth(cmd.getAuthToken());
            GameData gameData = gameDAO.findGame(cmd.getGameID());


            if (auth == null || gameData == null) {
                send(session, new ErrorMessage("Error: Invalid auth or game ID"));
                return;
            }

            if (gameData.gameOver()) {
                send(session, new ErrorMessage("Error: Game is already over"));
                return;
            }

            ChessGame game = gameData.game();
            String username = auth.username();

            // Reject if observer tries to resign
            if (!username.equals(gameData.whiteUsername()) && !username.equals(gameData.blackUsername())) {
                send(session, new ErrorMessage("Error: Observers can't resign the game"));
                return;
            }

            gameDAO.updateGame(new GameData(
                    gameData.gameID(),
                    gameData.whiteUsername(),
                    gameData.blackUsername(),
                    gameData.gameName(),
                    game,
                    true // Game is over now
            ));


            broadcastAll(cmd.getGameID(), new NotificationMessage(auth.username() + " has resigned. Game over."));
        } catch (Exception e) {
            send(session, new ErrorMessage("Error during resign: " + e.getMessage()));
        }
    }

    // ========== HELPER METHODS ==========

    private void send(Session session, ServerMessage message) {
        try {
            session.getRemote().sendString(gson.toJson(message));
        } catch (Exception ignored) {}
    }

    private void broadcastAll(int gameID, ServerMessage message) {
        gameToUsers.getOrDefault(gameID, Set.of()).forEach(username -> {
            Session session = usernameToSession.get(username);
            if (session != null && session.isOpen()) send(session, message);
        });
    }

    private void broadcastExcept(int gameID, String excludedUsername, ServerMessage message) {
        gameToUsers.getOrDefault(gameID, Set.of()).forEach(username -> {
            if (!username.equals(excludedUsername)) {
                Session session = usernameToSession.get(username);
                if (session != null && session.isOpen()) send(session, message);
            }
        });
    }
}

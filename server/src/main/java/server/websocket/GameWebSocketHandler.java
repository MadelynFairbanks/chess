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

    // Keeps track of which session is linked to which user
    private final Map<Session, String> sessionToUsername = new HashMap<>();

    // Lets us find a user's session if we know their username
    private final Map<String, Session> usernameToSession = new HashMap<>();

    // Tracks which users are currently in each game
    private final Map<Integer, Set<String>> gameToUsers = new HashMap<>();

    private final Gson gson = new Gson();

    private final MySqlAuthDAO authDAO = new MySqlAuthDAO();
    private final MySqlGameDAO gameDAO = new MySqlGameDAO();

    // When a client connects to the socket ...
    @OnWebSocketConnect
    public void onConnect(Session session) {
        // No-op for now
    }

    // When a client disconnects, clean up their session
    @OnWebSocketClose
    public void onClose(Session session, int statusCode, String reason) {
        String username = sessionToUsername.remove(session);
        if (username != null) {
            usernameToSession.remove(username);
        }
    }

    // Incoming message handler — figures out what kind of command it is and routes it
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

    // User joins a game through WebSocket
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

    // Handles a move being made in a game
    private void handleMakeMove(Session session, MakeMoveCommand cmd) {
        try {
            AuthData auth = authDAO.findAuth(cmd.getAuthToken());
            GameData gameData = gameDAO.findGame(cmd.getGameID());

            if (auth == null || gameData == null) {
                send(session, new ErrorMessage("Error: Invalid move - bad auth or game ID"));
                return;
            }


            ChessGame game = gameData.game();
            var move = cmd.getMove();

            // Determining if it's the user's turn based on color
            ChessGame.TeamColor playerColor = gameData.whiteUsername().equals(auth.username())
                    ? ChessGame.TeamColor.WHITE
                    : gameData.blackUsername().equals(auth.username())
                    ? ChessGame.TeamColor.BLACK
                    : null;

            if (playerColor == null || !game.getTeamTurn().equals(playerColor)) {
                send(session, new ErrorMessage("Error: Not your turn"));
                return;
            }

            // Check if the move is valid before making it
            if (!game.validMoves(move.getStartPosition()).contains(move)) {
                send(session, new ErrorMessage("Error: Invalid move"));
                return;
            }

            game.makeMove(move);

            // Update the game in the DB with the new state
            gameDAO.updateGame(new GameData(
                    gameData.gameID(),
                    gameData.whiteUsername(),
                    gameData.blackUsername(),
                    gameData.gameName(),
                    game
            ));

            // Let everyone know the new state + announce the move
            broadcastAll(cmd.getGameID(), new LoadGameMessage(game));
            broadcastExcept(cmd.getGameID(), auth.username(),
                    new NotificationMessage(auth.username() + " moved from " + move.getStartPosition() + " to " + move.getEndPosition()));

            // Check for check or checkmate after the move
            if (game.isInCheckmate(game.getTeamTurn())) {
                broadcastAll(cmd.getGameID(), new NotificationMessage(game.getTeamTurn() + " is in checkmate!"));
            } else if (game.isInCheck(game.getTeamTurn())) {
                broadcastAll(cmd.getGameID(), new NotificationMessage(game.getTeamTurn() + " is in check."));
            }

        } catch (Exception e) {
            send(session, new ErrorMessage("Error during move: " + e.getMessage()));
        }
    }


    // Handles when a user leaves a game (drops their color if needed)
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


    // Handles resigning from a game
    private void handleResign(Session session, ResignCommand cmd) {
        try {
            AuthData auth = authDAO.findAuth(cmd.getAuthToken());
            GameData gameData = gameDAO.findGame(cmd.getGameID());


            if (auth == null || gameData == null) {
                send(session, new ErrorMessage("Error: Invalid auth or game ID"));
                return;
            }


            ChessGame game = gameData.game();
            String username = auth.username();

            // Reject if observer tries to resign
            if (!username.equals(gameData.whiteUsername()) && !username.equals(gameData.blackUsername())) {
                send(session, new ErrorMessage("Error: Observers can't resign the game"));
                return;
            }

            // Mark game as over and save it
            gameDAO.updateGame(new GameData(
                    gameData.gameID(),
                    gameData.whiteUsername(),
                    gameData.blackUsername(),
                    gameData.gameName(),
                    game
            ));


            broadcastAll(cmd.getGameID(), new NotificationMessage(auth.username() + " has resigned. Game over."));
        } catch (Exception e) {
            send(session, new ErrorMessage("Error during resign: " + e.getMessage()));
        }
    }

    // ========== HELPER METHODS ==========

    // Sends a message to a single user
    private void send(Session session, ServerMessage message) {
        try {
            session.getRemote().sendString(gson.toJson(message));
        } catch (Exception ignored) {}
    }

    // Sends a message to every user in a game
    private void broadcastAll(int gameID, ServerMessage message) {
        gameToUsers.getOrDefault(gameID, Set.of()).forEach(username -> {
            Session session = usernameToSession.get(username);
            if (session != null && session.isOpen()) {
                send(session, message);
            }
        });
    }

    // Sends a message to everyone *except* the specified user
    private void broadcastExcept(int gameID, String excludedUsername, ServerMessage message) {
        gameToUsers.getOrDefault(gameID, Set.of()).forEach(username -> {
            if (!username.equals(excludedUsername)) {
                Session session = usernameToSession.get(username);
                if (session != null && session.isOpen()) {
                    send(session, message);
                }
            }
        });
    }
}

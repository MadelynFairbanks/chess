package service;

import dataaccess.DataAccess;
import dataaccess.DataAccessException;
import model.AuthData;
import model.GameData;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class GameService {
    private final DataAccess dataAccess;
    private final AtomicInteger idCounter = new AtomicInteger(1); // auto-increment ID

    public GameService(DataAccess dataAccess) {
        this.dataAccess = dataAccess;
    }

    public int createGame(String authToken, String gameName) throws DataAccessException {
        if (authToken == null || gameName == null || gameName.isEmpty()) {
            throw new DataAccessException("Error: bad request");
        }

        AuthData auth = dataAccess.getAuth(authToken);
        if (auth == null) {
            throw new DataAccessException("Error: unauthorized");
        }

        int gameID = idCounter.getAndIncrement();
        GameData game = new GameData(gameID, null, null, gameName, new chess.ChessGame());

        dataAccess.createGame(game);
        return gameID;
    }
}

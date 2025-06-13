package model;

import chess.ChessGame;

public record GameData(int gameID, String whiteUsername, String blackUsername, String gameName, ChessGame game) {
    public GameData setWhiteUsername(String username){
        return new GameData(gameID, username, blackUsername, gameName, game);
    }

    public GameData setBlackUsername(String username){
        return new GameData(gameID, whiteUsername, username, gameName, game);
    }

    public GameData setGameName(String gameName) {
        return new GameData(gameID, whiteUsername, blackUsername, gameName, game);

    }

    public GameData setGame(ChessGame game) {
        return new GameData(gameID, whiteUsername, blackUsername, gameName, game);
    }
}


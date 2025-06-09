package request;

import chess.ChessMove;

public record MoveRequest(String authToken, int gameID, ChessMove move) {}


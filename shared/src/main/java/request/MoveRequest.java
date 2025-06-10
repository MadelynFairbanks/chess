package request;

import chess.ChessMove;

public record MoveRequest(int gameID, ChessMove move) { }

package chess;

import java.util.ArrayList;
import java.util.Collection;

public class KnightMovesCalculator implements PieceMoveCalculatorInterface {
    public Collection<ChessMove> calculatedPieceMoves(ChessBoard board, ChessPosition myPosition) {
        Collection<ChessMove> moves = new ArrayList<>();
        int[][] directions = {{1, 2}, {-1, 2}, {1, -2}, {-1, -2},   {2, 1}, {-2, 1}, {2, -1}, {-2, -1}};
        moveOneSquare(directions, myPosition.getRow(), myPosition.getColumn(), board, myPosition, moves);
        return moves;
    }
}

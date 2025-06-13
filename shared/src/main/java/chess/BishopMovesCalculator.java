package chess;

import java.util.ArrayList;
import java.util.Collection;

public class BishopMovesCalculator implements PieceMoveCalculatorInterface{
    public Collection<ChessMove> calculatedPieceMoves(ChessBoard board, ChessPosition myPosition) {
        Collection<ChessMove> moves = new ArrayList<>();
        int[][] directions = {{1,1},{-1,1},{1,-1},{-1,-1}};
        loopMoves(moves, directions, myPosition.getRow(), myPosition.getColumn(), myPosition, board);
        return moves;
    }
}

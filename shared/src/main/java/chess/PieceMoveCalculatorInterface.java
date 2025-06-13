package chess;

import java.util.Collection;

public interface PieceMoveCalculatorInterface {
    Collection<ChessMove> calculatedPieceMoves(ChessBoard board, ChessPosition myPosition);

    default void loopMoves(Collection<ChessMove> moves, int[][] directions, int row, int col,
                                                               ChessPosition myPosition, ChessBoard board){
        for (int[] direction : directions) {
            int futureRow = row + direction[0];
            int futureCol = col + direction[1];
            isMovingLoop(futureRow, futureCol, board, direction, myPosition, moves);
        }
    }


    default void isMovingLoop(int futureRow, int futureCol, ChessBoard board, int[] direction, ChessPosition myPosition,Collection<ChessMove> moves){
        ChessPiece currentPiece = board.getPiece(myPosition);
        boolean blocked = false;
        while(!blocked){
            if (!(futureRow > 8 || futureCol > 8 || futureRow <=0 || futureCol <=0 )){
                ChessPosition futurePosition = new ChessPosition(futureRow,futureCol);
                ChessPiece pieceInFuturePosition = board.getPiece(futurePosition);

                if (pieceInFuturePosition != null) {
                    if (pieceInFuturePosition.getTeamColor() != currentPiece.getTeamColor()){
                        moves.add(new ChessMove(myPosition, futurePosition, null));
                    }
                    blocked = true;
                }else{
                    moves.add(new ChessMove(myPosition, futurePosition, null));
                    futureRow += direction[0];
                    futureCol += direction[1];
                }
            } else {
                blocked = true;
            }
        }
    }

    default void moveOneSquare(int[][] directions, int row, int col,
                               ChessBoard board, ChessPosition myPosition, Collection <ChessMove> moves){
        for (int[] direction : directions) {
            int futureRow = row + direction[0];
            int futureCol = col + direction[1];
            ChessPiece currentPiece = board.getPiece(myPosition);
            if (!(futureRow <= 0 || futureRow > 8 || futureCol <= 0 || futureCol > 8)) {
                ChessPosition futurePosition = new ChessPosition(futureRow, futureCol);
                ChessPiece futurePiece = board.getPiece(futurePosition);
                if (futurePiece != null) {
                    if (futurePiece.pieceColor != currentPiece.pieceColor) {
                        moves.add(new ChessMove(myPosition, futurePosition, null));
                    }
                } else {
                    moves.add(new ChessMove(myPosition, futurePosition, null));
                }
            }
        }
    }
}

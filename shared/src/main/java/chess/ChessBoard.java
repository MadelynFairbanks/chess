package chess;

import java.util.Arrays;

/**
 * A chessboard that can hold and rearrange chess pieces.
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessBoard implements Cloneable{
     ChessPiece[][] squares = new ChessPiece[8][8];

    public ChessBoard() {
// Blank method
    }

    /**
     * Adds a chess piece to the chessboard
     *
     * @param position where to add the piece to
     * @param piece    the piece to add
     */
    public void addPiece(ChessPosition position, ChessPiece piece) {
        squares[position.getRow()-1][position.getColumn()-1] = piece;
    }

    /**
     * Gets a chess piece on the chessboard
     *
     * @param position The position to get the piece from
     * @return Either the piece at the position, or null if no piece is at that
     * position
     */
    public ChessPiece getPiece(ChessPosition position) {
        return squares[position.getRow()-1][position.getColumn()-1];
    }

    /**
     * Sets the board to the default starting board
     * (How the game of chess normally starts)
     */
    public void resetBoard() {

        addPiece(new ChessPosition(1,1), new ChessPiece(ChessGame.TeamColor.WHITE ,ChessPiece.PieceType.ROOK));
        addPiece(new ChessPosition(1,8), new ChessPiece(ChessGame.TeamColor.WHITE ,ChessPiece.PieceType.ROOK));

        addPiece(new ChessPosition(1,2), new ChessPiece(ChessGame.TeamColor.WHITE ,ChessPiece.PieceType.KNIGHT));
        addPiece(new ChessPosition(1,7), new ChessPiece(ChessGame.TeamColor.WHITE ,ChessPiece.PieceType.KNIGHT));

        addPiece(new ChessPosition(1,3), new ChessPiece(ChessGame.TeamColor.WHITE ,ChessPiece.PieceType.BISHOP));
        addPiece(new ChessPosition(1,6), new ChessPiece(ChessGame.TeamColor.WHITE ,ChessPiece.PieceType.BISHOP));

        addPiece(new ChessPosition(1,4), new ChessPiece(ChessGame.TeamColor.WHITE ,ChessPiece.PieceType.QUEEN));
        addPiece(new ChessPosition(1,5), new ChessPiece(ChessGame.TeamColor.WHITE ,ChessPiece.PieceType.KING));

        for (int j = 1; j < 9; j++) {
            addPiece(new ChessPosition(2, j), new ChessPiece(ChessGame.TeamColor.WHITE, ChessPiece.PieceType.PAWN));
        }
        addPiece(new ChessPosition(8,1), new ChessPiece(ChessGame.TeamColor.BLACK ,ChessPiece.PieceType.ROOK));
        addPiece(new ChessPosition(8,8), new ChessPiece(ChessGame.TeamColor.BLACK ,ChessPiece.PieceType.ROOK));

        addPiece(new ChessPosition(8,2), new ChessPiece(ChessGame.TeamColor.BLACK ,ChessPiece.PieceType.KNIGHT));
        addPiece(new ChessPosition(8,7), new ChessPiece(ChessGame.TeamColor.BLACK ,ChessPiece.PieceType.KNIGHT));

        addPiece(new ChessPosition(8,3), new ChessPiece(ChessGame.TeamColor.BLACK ,ChessPiece.PieceType.BISHOP));
        addPiece(new ChessPosition(8,6), new ChessPiece(ChessGame.TeamColor.BLACK ,ChessPiece.PieceType.BISHOP));

        addPiece(new ChessPosition(8,4), new ChessPiece(ChessGame.TeamColor.BLACK ,ChessPiece.PieceType.QUEEN));
        addPiece(new ChessPosition(8,5), new ChessPiece(ChessGame.TeamColor.BLACK ,ChessPiece.PieceType.KING));

        for (int k = 1; k < 9; k++){
            addPiece(new ChessPosition(7,k), new ChessPiece(ChessGame.TeamColor.BLACK ,ChessPiece.PieceType.PAWN));
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ChessBoard that = (ChessBoard) o;
        return Arrays.deepEquals(squares, that.squares);
    }

    @Override
    public int hashCode() {
        return Arrays.deepHashCode(squares);
    }

    @Override
    public ChessBoard clone(){
        try{
            ChessBoard clone = (ChessBoard) super.clone();
            ChessPiece[][] cloneSquares = new ChessPiece[8][8];
            for (int i = 1; i <= 8; i++) {
                for (int j = 1; j <= 8; j++) {
                    if (squares[i-1][j-1] != null) {
                        cloneSquares[i-1][j-1] = squares[i-1][j-1].clone();
                    } else {
                        cloneSquares[i-1][j-1] = null;
                    }
                }
            }
            clone.squares = cloneSquares;
            return clone;
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException(e);
        }

    }
}

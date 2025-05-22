package chess;

import java.util.Collection;
import java.util.ArrayList;
import java.util.Objects;

/**
 * Represents a single chess piece
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessPiece {
    private final ChessGame.TeamColor teamColor;
    private final PieceType pieceType;
    private boolean isOnBoard(ChessPosition position) {
        int row = position.getRow();
        int col = position.getColumn();
        return row >= 1 && row <= 8 && col >= 1 && col <= 8;
    }
    private void addPromotionMovesIfPromotionOccurs(ChessPosition myCurrentPosition,
                                                    Collection<ChessMove> validMoves,
                                                    ChessPosition oneSpotForward) {
        int targetRow = oneSpotForward.getRow();
        boolean isPromotionTime = (teamColor == ChessGame.TeamColor.WHITE && targetRow == 8)
                || (teamColor == ChessGame.TeamColor.BLACK && targetRow == 1);
        if (isPromotionTime) {
            validMoves.add(new ChessMove(myCurrentPosition, oneSpotForward, PieceType.QUEEN));
            validMoves.add(new ChessMove(myCurrentPosition, oneSpotForward, PieceType.KNIGHT));
            validMoves.add(new ChessMove(myCurrentPosition, oneSpotForward, PieceType.ROOK));
            validMoves.add(new ChessMove(myCurrentPosition, oneSpotForward, PieceType.BISHOP));
        } else {
            validMoves.add(new ChessMove(myCurrentPosition, oneSpotForward, null));
        }

    }
    private void tryAddMove(ChessBoard board, ChessPosition myCurrentPosition,
                            Collection<ChessMove> validMoves, int newRow, int newCol) {
        ChessPosition target = new ChessPosition(newRow, newCol);
        if (isOnBoard(target)) {
            ChessPiece occupantOfSpot = board.getPiece(target);
            if (occupantOfSpot == null || occupantOfSpot.getTeamColor() != this.teamColor) {
                validMoves.add(new ChessMove(myCurrentPosition, target, null));
            }
        }
    }
    private void addSlidingMoves(ChessBoard board, ChessPosition myPosition,
                                 Collection<ChessMove> validMoves, int[][] directions) {
        int row = myPosition.getRow();
        int col = myPosition.getColumn();
        for (int[] dir : directions) {
            int r = row + dir[0];
            int c = col + dir[1];
            while (r >= 1 && r <= 8 && c >= 1 && c <= 8) {
                ChessPosition target = new ChessPosition(r, c);
                ChessPiece occupantOfSpot = board.getPiece(target);
                if (occupantOfSpot == null) {
                    validMoves.add(new ChessMove(myPosition, target, null));
                } else {
                    if (occupantOfSpot.getTeamColor() != this.teamColor) {
                        validMoves.add(new ChessMove(myPosition, target, null));
                    }
                    break;
                }
                r += dir[0];
                c += dir[1];
            }
        }
    }

    public ChessPiece(ChessGame.TeamColor pieceColor, ChessPiece.PieceType type) {
        this.teamColor = pieceColor;
        this.pieceType = type;
    }

    /**
     * The various different chess piece options
     */
    public enum PieceType {
        KING,
        QUEEN,
        BISHOP,
        KNIGHT,
        ROOK,
        PAWN
    }

    /**
     * @return Which team this chess piece belongs to
     */
    public ChessGame.TeamColor getTeamColor() {
        return teamColor;
    }

    /**
     * @return which type of chess piece this piece is
     */
    public PieceType getPieceType() {
        return pieceType;
    }

    /**
     * Calculates all the positions a chess piece can move to
     * Does not take into account moves that are illegal due to leaving the king in
     * danger
     *
     * @return Collection of valid moves
     */
    public Collection<ChessMove> pieceMoves(ChessBoard board, ChessPosition myPosition) {
        Collection<ChessMove> validMoves = new ArrayList<>();
        // the valid moves of a PAWN:
        if (pieceType == PieceType.PAWN) {
            int col = myPosition.getColumn();
            int row = myPosition.getRow();
            // if it's white then it should move positive one to go forward and if
            // it's not white it should move negative one to go forward
            int pawnDirection = (teamColor == ChessGame.TeamColor.WHITE) ? 1:-1;
            // if the spot in front is on the board and there isnt another piece there, move forward
            ChessPosition oneSpotForward = new ChessPosition(row + pawnDirection, col);
            if (isOnBoard(oneSpotForward) && board.getPiece(oneSpotForward) == null) {
                // move forward but also we need to take into account whether its a promotion piece or not
                addPromotionMovesIfPromotionOccurs(myPosition, validMoves, oneSpotForward);
                // pawns can double move if it's their first move of the game / if its the starting move
                int startRow = (teamColor == ChessGame.TeamColor.WHITE) ? 2 : 7;
                ChessPosition twoSpotsForward = new ChessPosition(row + 2 * pawnDirection, col);
                // if there is no piece one or two spots ahead of the pawn on its first move then it can go two spots forward
                if (row == startRow && board.getPiece(oneSpotForward) == null && board.getPiece(twoSpotsForward) == null) {
                    validMoves.add(new ChessMove(myPosition, twoSpotsForward, null));
                }

            }
            // Pawns can also move diagonally if capturing another piece
            int[] columnOffsets = {-1, 1};
            for (int offset : columnOffsets) {
                ChessPosition diagonal = new ChessPosition(row + pawnDirection, col + offset);
                if (isOnBoard(diagonal)){
                    ChessPiece targetPiece = board.getPiece(diagonal);
                    if (targetPiece != null && targetPiece.getTeamColor() != this.teamColor) {
                        addPromotionMovesIfPromotionOccurs(myPosition, validMoves, diagonal);
                    }
                }
            }

            } else if (pieceType == PieceType.KNIGHT) {
            // Knights move in an L shape in any direction and can jump over other pieces
            int[][] offsets = {{-2,-1}, {-2,1}, {-1,-2}, {-1, 2},
                    {1,-2}, {1, 2}, {2, -1}, {2, 1}};
            for (int[] offset: offsets) {
                int newRow = myPosition.getRow() + offset[0];
                int newCol = myPosition.getColumn() + offset[1];
                tryAddMove(board, myPosition, validMoves, newRow, newCol);
            }
        } else if (pieceType == PieceType.ROOK) {
            int[][] rookDirections = {{-1, 0}, {1, 0}, {0, -1}, {0, 1}};
            addSlidingMoves(board, myPosition, validMoves, rookDirections);
        } else if (pieceType == PieceType.BISHOP) {
            int[][] bishopDirections = {{-1, -1}, {-1, 1}, {1, -1}, {1, 1}};
            addSlidingMoves(board, myPosition, validMoves, bishopDirections);
        } else if (pieceType == PieceType.QUEEN) {
            int[][] queenDirections = {{-1, 0}, {1, 0}, {0, -1}, {0, 1},
                    {-1, -1}, {-1, 1}, {1, -1}, {1, 1}};
            addSlidingMoves(board, myPosition, validMoves, queenDirections);
        } else if (pieceType == PieceType.KING) {
            int[][] kingDirections = {{-1, 0}, {1, 0}, {0, -1},
                    {0, 1}, {-1, -1}, {-1, 1}, {1, -1}, {1, 1}};
            int row = myPosition.getRow();
            int column = myPosition.getColumn();
            for (int[] direction: kingDirections) {
                int newRow = row + direction[0];
                int newCol = column + direction[1];
                tryAddMove(board, myPosition, validMoves, newRow, newCol);
            }
        }
        return validMoves;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ChessPiece)) {
            return false;
        }
        ChessPiece that = (ChessPiece) o;
        return teamColor == that.teamColor && pieceType == that.pieceType;
    }

    @Override
    public int hashCode() {
        return Objects.hash(teamColor, pieceType);
    }

    @Override
    public String toString() {
        return teamColor + " " + pieceType;
    }
}


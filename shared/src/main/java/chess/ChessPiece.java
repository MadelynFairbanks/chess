package chess;

import java.util.ArrayList;
import java.util.Collection;
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
    private boolean isOnBoard(ChessPosition pos) {
        int row = pos.getRow();
        int col = pos.getColumn();
        return row >= 1 && row <= 8 && col >= 1 && col <= 8;
    }
    private void slidingAcrossTheBoardMoves(ChessBoard board, ChessPosition myPosition, Collection<ChessMove> validMoves, int[][] directions) {
        int row = myPosition.getRow();
        int col = myPosition.getColumn();
        for (int[] dir : directions) {
            int r = row + dir[0];
            int c = col + dir[1];
            while (r >= 1 && r <= 8 && c >= 1 && c <= 8) {
                ChessPosition target = new ChessPosition(r, c);
                ChessPiece occupant = board.getPiece(target);
                if (occupant == null) {
                    validMoves.add(new ChessMove(myPosition, target, null));
                } else {
                    if (occupant.getTeamColor() != this.teamColor) {
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
        if (pieceType == PieceType.PAWN) {
            int row = myPosition.getRow();
            int col = myPosition.getColumn();
            int direction = (teamColor == ChessGame.TeamColor.WHITE) ? 1 : -1;

            // if the spot in front is empty, able to move forward by 1
            ChessPosition oneSpotForward = new ChessPosition(row + direction, col);
            if (isOnBoard(oneSpotForward) && board.getPiece(oneSpotForward) == null) {
                int targetRow = oneSpotForward.getRow();
                boolean isPromotion = (teamColor == ChessGame.TeamColor.WHITE && targetRow == 8) ||
                        (teamColor == ChessGame.TeamColor.BLACK && targetRow == 1);
                if (isPromotion) {
                    validMoves.add(new ChessMove(myPosition, oneSpotForward, PieceType.QUEEN));
                    validMoves.add(new ChessMove(myPosition, oneSpotForward, PieceType.ROOK));
                    validMoves.add(new ChessMove(myPosition, oneSpotForward, PieceType.BISHOP));
                    validMoves.add(new ChessMove(myPosition, oneSpotForward, PieceType.KNIGHT));
                } else {
                    validMoves.add(new ChessMove(myPosition, oneSpotForward, null));
                }
            }
            // if the pawn is at the starting row then it can move 2 forward as long as the spot is open
            int startRow = (teamColor == ChessGame.TeamColor.WHITE) ? 2 : 7; // this tells the starting row depending on what color
            ChessPosition twoSpotsForward = new ChessPosition(row + 2 * direction, col);
            if (row == startRow && board.getPiece(oneSpotForward) == null && board.getPiece(twoSpotsForward) == null) {
                validMoves.add(new ChessMove(myPosition, twoSpotsForward, null));
            }
            // Pawns can also move diagonally when capturing other pieces
            int[] columnOffsets = {-1, 1};
            for (int offset : columnOffsets) {
                ChessPosition diagonal = new ChessPosition(row + direction, col + offset);
                if (isOnBoard(diagonal)) {
                    ChessPiece targetPiece = board.getPiece(diagonal);
                    if (targetPiece != null && targetPiece.getTeamColor() != this.teamColor) {
                        int targetRow = diagonal.getRow();
                        boolean isPromotion = (teamColor == ChessGame.TeamColor.WHITE && targetRow == 8) ||
                                (teamColor == ChessGame.TeamColor.BLACK && targetRow == 1);
                        if (isPromotion) {
                            validMoves.add(new ChessMove(myPosition, diagonal, PieceType.QUEEN));
                            validMoves.add(new ChessMove(myPosition, diagonal, PieceType.ROOK));
                            validMoves.add(new ChessMove(myPosition, diagonal, PieceType.BISHOP));
                            validMoves.add(new ChessMove(myPosition, diagonal, PieceType.KNIGHT));
                        } else {
                            validMoves.add(new ChessMove(myPosition, diagonal, null));
                        }
                    }
                }
            }
        } else if (pieceType == PieceType.KNIGHT) {
            int[][] offsets = {
                    {-2, -1}, {-2, 1}, {-1, -2}, {-1, 2},
                    {1, -2}, {1, 2}, {2, -1}, {2, 1}
            };

            for (int[] offset : offsets) {
                int newRow = myPosition.getRow() + offset[0];
                int newCol = myPosition.getColumn() + offset[1];
                ChessPosition target = new ChessPosition(newRow, newCol);

                if (isOnBoard(target)) {
                    ChessPiece occupant = board.getPiece(target);
                    if (occupant == null || occupant.getTeamColor() != this.teamColor) {
                        validMoves.add(new ChessMove(myPosition, target, null));
                    }
                }
            }
        } else if (pieceType == PieceType.ROOK) {
            int[][] rookDirs = {{-1, 0}, {1, 0}, {0, -1}, {0, 1}};
            slidingAcrossTheBoardMoves(board, myPosition, validMoves, rookDirs);

        } else if (pieceType == PieceType.BISHOP) {
            int[][] bishopDirs = {{-1, -1}, {-1, 1}, {1, -1}, {1, 1}};
            slidingAcrossTheBoardMoves(board, myPosition, validMoves, bishopDirs);

        } else if (pieceType == PieceType.QUEEN) {
            int[][] queenDirs = {
                    {-1, 0}, {1, 0}, {0, -1}, {0, 1},
                    {-1, -1}, {-1, 1}, {1, -1}, {1, 1}
            };
            slidingAcrossTheBoardMoves(board, myPosition, validMoves, queenDirs);
        }

        return validMoves;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ChessPiece)) return false;
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

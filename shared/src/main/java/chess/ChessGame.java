package chess;

import java.util.Collection;
import java.util.ArrayList;
import java.util.Objects;

/**
 * For a class that can manage a chess game, making moves on a board
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessGame {
    private ChessBoard board;
    private TeamColor teamTurn;
    private ChessMove lastMove = null;
    private boolean whiteKingMoved = false;
    private boolean blackKingMoved = false;
    private boolean whiteLeftRookMoved = false;
    private boolean whiteRightRookMoved = false;
    private boolean blackLeftRookMoved = false;
    private boolean blackRightRookMoved = false;

    private ChessPosition findKingPosition(TeamColor color, ChessBoard board) {
        for (int row = 1; row <= 8; row++) {
            for (int col = 1; col <= 8; col++) {
                ChessPosition pos = new ChessPosition(row, col);
                ChessPiece p = board.getPiece(pos);
                if (p != null && p.getTeamColor() == color && p.getPieceType() == ChessPiece.PieceType.KING) {
                    return pos;
                }
            }
        }
        return null;
    }

    private boolean isInCheck(TeamColor color, ChessBoard board, ChessPosition kingPosition) {
        for (int row = 1; row <= 8; row++) {
            for (int col = 1; col <= 8; col++) {
                ChessPosition pos = new ChessPosition(row, col);
                ChessPiece attacker = board.getPiece(pos);
                if (attacker != null && attacker.getTeamColor() != color) {
                    Collection<ChessMove> moves = attacker.pieceMoves(board, pos);
                    for (ChessMove move : moves) {
                        if (move.getEndPosition().equals(kingPosition)) {
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    private void copyBoard(ChessBoard target, ChessBoard source) {
        for (int row = 1; row <= 8; row++) {
            for (int col = 1; col <= 8; col++) {
                ChessPosition pos = new ChessPosition(row, col);
                target.addPiece(pos, source.getPiece(pos));
            }
        }
    }

    private boolean Checking(TeamColor teamColor) {
        for (int row = 1; row <= 8; row++) {
            for (int col = 1; col <= 8; col++) {
                ChessPosition pos = new ChessPosition(row, col);
                ChessPiece piece = board.getPiece(pos);

                if (piece != null && piece.getTeamColor() == teamColor) {
                    Collection<ChessMove> moves = validMoves(pos);
                    if (moves != null && !moves.isEmpty()) {
                        return false; // Found a legal move
                    }
                }
            }
        }

        return true; // No legal moves and not in check
    }

    public ChessGame() {
        this.board = new ChessBoard();
        this.board.resetBoard(); // Sets up the default starting position
        this.teamTurn = TeamColor.WHITE; // White always starts
    }

    /**
     * @return Which team's turn it is
     */
    public TeamColor getTeamTurn() {
        return teamTurn;
    }

    /**
     * Set's which teams turn it is
     *
     * @param team the team whose turn it is
     */
    public void setTeamTurn(TeamColor team) {
        this.teamTurn = team;
    }

    /**
     * Enum identifying the 2 possible teams in a chess game
     */
    public enum TeamColor {
        WHITE,
        BLACK
    }

    /**
     * Gets a valid moves for a piece at the given location
     *
     * @param startPosition the piece to get valid moves for
     * @return Set of valid moves for requested piece, or null if no piece at
     * startPosition
     */
    public Collection<ChessMove> validMoves(ChessPosition startPosition) {
        ChessPiece piece = board.getPiece(startPosition);
        if (piece == null) {
            return null;
        }

        Collection<ChessMove> potentialMoves = piece.pieceMoves(board, startPosition);
        Collection<ChessMove> validMoves = new ArrayList<>();
        // En Passant
        if (piece.getPieceType() == ChessPiece.PieceType.PAWN && lastMove != null) {
            ChessPiece lastMoved = board.getPiece(lastMove.getEndPosition());
            if (lastMoved != null && lastMoved.getPieceType() == ChessPiece.PieceType.PAWN &&
                    Math.abs(lastMove.getStartPosition().getRow() - lastMove.getEndPosition().getRow()) == 2) {

                int direction = (piece.getTeamColor() == TeamColor.WHITE) ? 1 : -1;
                int row = startPosition.getRow();
                int col = startPosition.getColumn();

                if (Math.abs(lastMove.getEndPosition().getColumn() - col) == 1 &&
                        lastMove.getEndPosition().getRow() == row) {
                    ChessPosition capture = new ChessPosition(row + direction, lastMove.getEndPosition().getColumn());
                    validMoves.add(new ChessMove(startPosition, capture, null));
                }
            }
        }


        for (ChessMove move : potentialMoves) {
            // Make a copy of the board
            ChessBoard simulatedBoard = new ChessBoard();
            copyBoard(simulatedBoard, board);

            // Simulate the move
            simulatedBoard.addPiece(move.getEndPosition(), piece);
            simulatedBoard.addPiece(startPosition, null);

            // Check if move leaves king in check
            ChessPosition kingPosition = findKingPosition(piece.getTeamColor(), simulatedBoard);
            if (!isInCheck(piece.getTeamColor(), simulatedBoard, kingPosition)) {
                validMoves.add(move);
            }
        }
        // Add castling if it's the king
        if (piece.getPieceType() == ChessPiece.PieceType.KING && !isInCheck(piece.getTeamColor())) {
            boolean isWhite = piece.getTeamColor() == TeamColor.WHITE;
            int row = isWhite ? 1 : 8;

            // Kingside castling
            if ((isWhite && !whiteKingMoved && !whiteRightRookMoved) ||
                    (!isWhite && !blackKingMoved && !blackRightRookMoved)) {
                if (board.getPiece(new ChessPosition(row, 6)) == null &&
                        board.getPiece(new ChessPosition(row, 7)) == null) {

                    ChessBoard simulated = new ChessBoard();
                    copyBoard(simulated, board);
                    simulated.addPiece(new ChessPosition(row, 6), piece);
                    simulated.addPiece(startPosition, null);
                    if (!isInCheck(piece.getTeamColor(), simulated, new ChessPosition(row, 6))) {
                        validMoves.add(new ChessMove(startPosition, new ChessPosition(row, 7), null));
                    }
                }
            }

            // Queenside castling
            if ((isWhite && !whiteKingMoved && !whiteLeftRookMoved) ||
                    (!isWhite && !blackKingMoved && !blackLeftRookMoved)) {
                if (board.getPiece(new ChessPosition(row, 2)) == null &&
                        board.getPiece(new ChessPosition(row, 3)) == null &&
                        board.getPiece(new ChessPosition(row, 4)) == null) {

                    ChessBoard simulated = new ChessBoard();
                    copyBoard(simulated, board);
                    simulated.addPiece(new ChessPosition(row, 4), piece);
                    simulated.addPiece(startPosition, null);
                    if (!isInCheck(piece.getTeamColor(), simulated, new ChessPosition(row, 4))) {
                        validMoves.add(new ChessMove(startPosition, new ChessPosition(row, 3), null));
                    }
                }
            }
        }
        return validMoves;
    }

    /**
     * Makes a move in a chess game
     *
     * @param move chess move to perform
     * @throws InvalidMoveException if move is invalid
     */
    public void makeMove(ChessMove move) throws InvalidMoveException {
        ChessPiece piece = board.getPiece(move.getStartPosition());

        if (piece == null) {
            throw new InvalidMoveException("No piece at the starting position.");
        }

        if (piece.getTeamColor() != teamTurn) {
            throw new InvalidMoveException("It's not this piece's team's turn.");
        }

        Collection<ChessMove> legalMoves = validMoves(move.getStartPosition());
        if (legalMoves == null || !legalMoves.contains(move)) {
            throw new InvalidMoveException("Move is not valid.");
        }

        // If this is a promotion move, replace with promoted piece
        if (move.getPromotionPiece() != null) {
            ChessPiece promoted = new ChessPiece(teamTurn, move.getPromotionPiece());
            board.addPiece(move.getEndPosition(), promoted);
        } else {
            board.addPiece(move.getEndPosition(), piece);
        }

        // Clear the start position
        board.addPiece(move.getStartPosition(), null);
        // Track piece movement for castling
        if (piece.getPieceType() == ChessPiece.PieceType.KING) {
            if (teamTurn == TeamColor.WHITE) whiteKingMoved = true;
            else blackKingMoved = true;

            // Detect castling and move the rook
            int row = move.getStartPosition().getRow();
            int colStart = move.getStartPosition().getColumn();
            int colEnd = move.getEndPosition().getColumn();

            if (Math.abs(colEnd - colStart) == 2) {
                if (colEnd == 7) { // Kingside
                    ChessPosition rookFrom = new ChessPosition(row, 8);
                    ChessPosition rookTo = new ChessPosition(row, 6);
                    board.addPiece(rookTo, board.getPiece(rookFrom));
                    board.addPiece(rookFrom, null);
                } else if (colEnd == 3) { // Queenside
                    ChessPosition rookFrom = new ChessPosition(row, 1);
                    ChessPosition rookTo = new ChessPosition(row, 4);
                    board.addPiece(rookTo, board.getPiece(rookFrom));
                    board.addPiece(rookFrom, null);
                }
            }
        }

        if (piece.getPieceType() == ChessPiece.PieceType.ROOK) {
            int row = move.getStartPosition().getRow();
            int col = move.getStartPosition().getColumn();
            if (teamTurn == TeamColor.WHITE) {
                if (row == 1 && col == 1) whiteLeftRookMoved = true;
                else if (row == 1 && col == 8) whiteRightRookMoved = true;
            } else {
                if (row == 8 && col == 1) blackLeftRookMoved = true;
                else if (row == 8 && col == 8) blackRightRookMoved = true;
            }
        }

// En Passant capture
        if (piece.getPieceType() == ChessPiece.PieceType.PAWN &&
                lastMove != null &&
                Math.abs(move.getEndPosition().getColumn() - move.getStartPosition().getColumn()) == 1 &&
                board.getPiece(move.getEndPosition()) == null) {

            int capturedRow = (teamTurn == TeamColor.WHITE) ?
                    move.getEndPosition().getRow() - 1 :
                    move.getEndPosition().getRow() + 1;
            ChessPosition capturedPawnPos = new ChessPosition(capturedRow, move.getEndPosition().getColumn());
            board.addPiece(capturedPawnPos, null);
        }

// Track last move
        lastMove = move;
        // Change the turn
        teamTurn = (teamTurn == TeamColor.WHITE) ? TeamColor.BLACK : TeamColor.WHITE;
    }

    /**
     * Determines if the given team is in check
     *
     * @param teamColor which team to check for check
     * @return True if the specified team is in check
     */
    public boolean isInCheck(TeamColor teamColor) {
        ChessPosition kingPos = findKingPosition(teamColor, board);
        return isInCheck(teamColor, board, kingPos);
    }

    /**
     * Determines if the given team is in checkmate
     *
     * @param teamColor which team to check for checkmate
     * @return True if the specified team is in checkmate
     */
    public boolean isInCheckmate(TeamColor teamColor) {
        if (!isInCheck(teamColor)) {
            return false;
        }

        // Try every move by every piece of the team
        return Checking(teamColor);
    }

    /**
     * Determines if the given team is in stalemate, which here is defined as having
     * no valid moves
     *
     * @param teamColor which team to check for stalemate
     * @return True if the specified team is in stalemate, otherwise false
     */
    public boolean isInStalemate(TeamColor teamColor) {
        if (isInCheck(teamColor)) {
            return false;
        }

        return Checking(teamColor);
    }


    /**
     * Sets this game's chessboard with a given board
     *
     * @param board the new board to use
     */
    public void setBoard(ChessBoard board) {
        this.board = board;
    }

    /**
     * Gets the current chessboard
     *
     * @return the chessboard
     */
    public ChessBoard getBoard() {
        return board;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ChessGame other)) return false;
        return board.equals(other.board) && teamTurn == other.teamTurn;
    }

    @Override
    public int hashCode() {
        return Objects.hash(board, teamTurn);
    }

}



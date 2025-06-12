package chess;

import java.util.Collection;
import java.util.ArrayList;
import java.util.Objects;

public class ChessGame {
    private ChessBoard board;
    private TeamColor teamTurn;

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
                if (isAttackingKing(pos, color, board, kingPosition)) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean isAttackingKing(ChessPosition pos, TeamColor color, ChessBoard board, ChessPosition kingPosition) {
        ChessPiece attacker = board.getPiece(pos);
        if (attacker == null || attacker.getTeamColor() == color) {
            return false;
        }
        Collection<ChessMove> moves = attacker.pieceMoves(board, pos);
        for (ChessMove move : moves) {
            if (move.getEndPosition().equals(kingPosition)) {
                return true;
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

    private boolean checking(TeamColor teamColor) {
        for (int row = 1; row <= 8; row++) {
            for (int col = 1; col <= 8; col++) {
                ChessPosition pos = new ChessPosition(row, col);
                if (hasLegalMoves(teamColor, pos)) {
                    return false; // Found a legal move
                }
            }
        }
        return true; // No legal moves and not in check
    }

    // <-- This is the **only** new helper method added to reduce nesting depth -->
    private boolean hasLegalMoves(TeamColor teamColor, ChessPosition pos) {
        ChessPiece piece = board.getPiece(pos);
        if (piece != null && piece.getTeamColor() == teamColor) {
            Collection<ChessMove> moves = validMoves(pos);
            return moves != null && !moves.isEmpty();
        }
        return false;
    }

    public ChessGame() {
        this.board = new ChessBoard();
        this.board.resetBoard(); // Sets up the default starting position
        this.teamTurn = TeamColor.WHITE; // White always starts
    }

    public TeamColor getTeamTurn() {
        return teamTurn;
    }

    public void setTeamTurn(TeamColor team) {
        this.teamTurn = team;
    }

    public enum TeamColor {
        WHITE,
        BLACK
    }

    public Collection<ChessMove> validMoves(ChessPosition startPosition) {
        ChessPiece piece = board.getPiece(startPosition);
        if (piece == null) {
            return null;
        }

        Collection<ChessMove> potentialMoves = piece.pieceMoves(board, startPosition);
        Collection<ChessMove> validMoves = new ArrayList<>();

        for (ChessMove move : potentialMoves) {
            ChessBoard simulatedBoard = new ChessBoard();
            copyBoard(simulatedBoard, board);

            simulatedBoard.addPiece(move.getEndPosition(), piece);
            simulatedBoard.addPiece(startPosition, null);

            ChessPosition kingPosition = findKingPosition(piece.getTeamColor(), simulatedBoard);
            if (!isInCheck(piece.getTeamColor(), simulatedBoard, kingPosition)) {
                validMoves.add(move);
            }
        }

        return validMoves;
    }

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

        if (move.getPromotionPiece() != null) {
            ChessPiece promoted = new ChessPiece(teamTurn, move.getPromotionPiece());
            board.addPiece(move.getEndPosition(), promoted);
        } else {
            board.addPiece(move.getEndPosition(), piece);
        }

        board.addPiece(move.getStartPosition(), null);
        teamTurn = (teamTurn == TeamColor.WHITE) ? TeamColor.BLACK : TeamColor.WHITE;
    }

    public boolean isInCheck(TeamColor teamColor) {
        ChessPosition kingPos = findKingPosition(teamColor, board);
        return isInCheck(teamColor, board, kingPos);
    }

    public boolean isInCheckmate(TeamColor teamColor) {
        if (!isInCheck(teamColor)) {
            return false;
        }
        return checking(teamColor);
    }

    public boolean isInStalemate(TeamColor teamColor) {
        if (isInCheck(teamColor)) {
            return false;
        }
        return checking(teamColor);
    }

    public boolean isGameOver() {
        return isInCheckmate(TeamColor.WHITE) || isInCheckmate(TeamColor.BLACK)
                || isInStalemate(TeamColor.WHITE) || isInStalemate(TeamColor.BLACK);
    }


    public void setBoard(ChessBoard board) {
        this.board = board;
    }

    public ChessBoard getBoard() {
        return board;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ChessGame other)) {
            return false;
        }
        return board.equals(other.board) && teamTurn == other.teamTurn;
    }

    @Override
    public int hashCode() {
        return Objects.hash(board, teamTurn);
    }
}

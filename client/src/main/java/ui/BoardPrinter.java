package ui;

import chess.*;

public class BoardPrinter {

    public static void printBoard(ChessGame game, boolean whitePerspective) {
        ChessBoard board = game.getBoard();
        System.out.println();

        int startRow = whitePerspective ? 8 : 1;
        int endRow = whitePerspective ? 0 : 9;
        int step = whitePerspective ? -1 : 1;

        // Print column labels
        System.out.print("   ");
        for (char col = 'a'; col <= 'h'; col++) {
            System.out.print(" " + col + " ");
        }
        System.out.println();

        for (int row = startRow; row != endRow; row += step) {
            System.out.print(" " + row + " ");
            for (int col = 1; col <= 8; col++) {
                int displayCol = whitePerspective ? col : 9 - col;
                ChessPiece piece = board.getPiece(new ChessPosition(row, displayCol));

                boolean lightSquare = ((row + displayCol) % 2 == 0);
                String bgColor = lightSquare ? EscapeSequences.SET_BG_COLOR_WHITE : EscapeSequences.SET_BG_COLOR_BLACK;

                String symbol = getUnicodePiece(piece);
                System.out.print(bgColor + EscapeSequences.SET_TEXT_COLOR_WHITE + " " + symbol + " " + EscapeSequences.RESET_BG_COLOR);
            }
            System.out.println(" " + row);
        }

        // Print column labels again
        System.out.print("   ");
        for (char col = 'a'; col <= 'h'; col++) {
            System.out.print(" " + col + " ");
        }
        System.out.println("\n");
    }

    private static String getUnicodePiece(ChessPiece piece) {
        if (piece == null) return EscapeSequences.EMPTY;

        return switch (piece.getTeamColor()) {
            case WHITE -> switch (piece.getPieceType()) {
                case KING -> "♔";   // ♔
                case QUEEN -> "♕";  // ♕
                case ROOK -> "♖";   // ♖
                case BISHOP -> "♗"; // ♗
                case KNIGHT -> "♘"; // ♘
                case PAWN -> "♙";   // ♙
            };
            case BLACK -> switch (piece.getPieceType()) {
                case KING -> "♚";   // ♚
                case QUEEN -> "♛";  // ♛
                case ROOK -> "♜";   // ♜
                case BISHOP -> "♝"; // ♝
                case KNIGHT -> "♞"; // ♞
                case PAWN -> "♟";   // ♟
            };
        };
    }
}

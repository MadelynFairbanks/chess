package ui;

import chess.*;

public class BoardPrinter {

    // Prints the current state of the chessboard in the console
    public static void printBoard(ChessGame game, boolean whitePerspective) {
        ChessBoard board = game.getBoard();
        System.out.println();

        // If whitePerspective is true, we start from row 8 and count down (white's view)
        int startRow = whitePerspective ? 8 : 1;
        int endRow = whitePerspective ? 0 : 9;
        int step = whitePerspective ? -1 : 1;

        // Print the top row of column letters (a–h)
        System.out.print("   ");
        for (char col = 'a'; col <= 'h'; col++) {
            System.out.print(" " + col + " ");
        }
        System.out.println();

        // Print each row from top to bottom
        for (int row = startRow; row != endRow; row += step) {
            System.out.print(" " + row + " ");
            for (int col = 1; col <= 8; col++) {
                // Flip the board horizontally if it's black's perspective
                int displayCol = whitePerspective ? col : 9 - col;
                ChessPiece piece = board.getPiece(new ChessPosition(row, displayCol));

                // Alternate light/dark squares (like a real board)
                boolean lightSquare = ((row + displayCol) % 2 == 0);
                String bgColor = lightSquare ? EscapeSequences.SET_BG_COLOR_WHITE : EscapeSequences.SET_BG_COLOR_BLACK;

                // Get the symbol for this piece, or an empty square if null
                String symbol = getUnicodePiece(piece);
                System.out.print(bgColor + EscapeSequences.SET_TEXT_COLOR_WHITE + " " + symbol + " " + EscapeSequences.RESET_BG_COLOR);
            }
            System.out.println(" " + row);
        }

        // Print the bottom row of column letters again
        System.out.print("   ");
        for (char col = 'a'; col <= 'h'; col++) {
            System.out.print(" " + col + " ");
        }
        System.out.println("\n");
    }

    // Converts a ChessPiece into its corresponding Unicode character
    private static String getUnicodePiece(ChessPiece piece) {
        if (piece == null) {
            return EscapeSequences.EMPTY;
        }

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

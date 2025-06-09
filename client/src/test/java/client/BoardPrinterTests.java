package client;

import chess.ChessGame;
import ui.BoardPrinter;

public class BoardPrinterTests {
    public static void main(String[] args) {
        ChessGame game = new ChessGame(); // Correct type

        System.out.println("White's perspective:");
        BoardPrinter.printBoard(game, true);  // true = white perspective

        System.out.println("\nBlack's perspective:");
        BoardPrinter.printBoard(game, false); // false = black perspective
    }
}


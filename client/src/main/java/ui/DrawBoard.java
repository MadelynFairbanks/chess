package ui;

import chess.*;

import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.Collection;

import static ui.EscapeSequences.*;

public class DrawBoard {

    private static final int BOARD_SIZE_IN_SQUARES = 8;
    private static final int SQUARE_SIZE_IN_PADDED_CHARS = 2;
    private static final int middleLine = SQUARE_SIZE_IN_PADDED_CHARS / 2;

    private static ChessBoard board;
    private static ChessGame game;

    // ✨ Draws the board AND highlights valid moves for a selected piece
    public void drawHighlighted(ChessBoard board, String perspective, ChessPosition position, ChessGame game) {
        DrawBoard.game = game;
        DrawBoard.board = board;

        System.out.println("\n");
        var out = new PrintStream(System.out, true, StandardCharsets.UTF_8);

        drawHeaders(out, perspective);
        drawChessBoard(out, perspective, position);
        drawHeaders(out, perspective);
    }

    // 🎨 Draws the board without any highlighting
    public void draw(ChessBoard board, String perspective) {
        DrawBoard.board = board;

        System.out.println("\n");
        var out = new PrintStream(System.out, true, StandardCharsets.UTF_8);

        drawHeaders(out, perspective);
        drawChessBoard(out, perspective, null);
        drawHeaders(out, perspective);
    }

    // 🧾 Draws the A–H / H–A column labels at top & bottom
    private static void drawHeaders(PrintStream out, String perspective) {
        setBlack(out);
        out.print("     ");

        String[] headers = (perspective.equals("white") || perspective.equals("observer"))
                ? new String[]{"A", "B", "C", "D", "E", "F", "G", "H"}
                : new String[]{"H", "G", "F", "E", "D", "C", "B", "A"};

        for (String header : headers) {
            drawHeader(out, header);
        }
        out.println();
    }

    private static void drawHeader(PrintStream out, String headerText) {
        out.print("  ");
        printHeaderText(out, headerText);
    }

    private static void printHeaderText(PrintStream out, String text) {
        out.print(SET_BG_COLOR_BLACK);
        out.print(SET_TEXT_COLOR_GREEN);
        out.print(text);
        setBlack(out);
    }

    // 🧠 Draws either the white or black player’s view
    private static void drawChessBoard(PrintStream out, String perspective, ChessPosition highlight) {
        for (int row = 0; row < BOARD_SIZE_IN_SQUARES; row++) {
            if (perspective.equals("white") || perspective.equals("observer")) {
                drawRowOfSquaresPerspectiveWhite(out, row, highlight);
            } else {
                drawRowOfSquaresPerspectiveBlack(out, row, highlight);
            }
        }
    }

    // 🎨 Determine what color to draw the chess piece based on team
    private static String setPieceColor(int row, int col, ChessBoard board) {
        ChessPiece piece = board.getPiece(new ChessPosition(row, col));
        return (piece.getTeamColor() == ChessGame.TeamColor.WHITE)
                ? SET_TEXT_COLOR_BLUE
                : SET_TEXT_COLOR_BLACK;
    }

    // 🧩 Get the piece symbol for a given square
    private static String setPieceType(int row, int col, ChessBoard board) {
        ChessPiece piece = board.getPiece(new ChessPosition(row, col));
        if (piece == null) return null;

        return switch (piece.getPieceType()) {
            case BISHOP -> "B";
            case KNIGHT -> "N";
            case PAWN   -> "p";
            case KING   -> "K";
            case QUEEN  -> "Q";
            case ROOK   -> "R";
        };
    }

    // ♟️ Draw a row from the BLACK perspective
    private static void drawRowOfSquaresPerspectiveBlack(PrintStream out, int row, ChessPosition highlight) {
        Collection<ChessMove> validMoves = null;
        out.print(SET_TEXT_COLOR_GREEN);
        out.printf("   %2d ", row + 1);

        for (int col = 0; col < BOARD_SIZE_IN_SQUARES; col++) {
            int actualRow = row + 1;
            int actualCol = 8 - col;

            boolean isDarkSquare = (row + col) % 2 == 1;
            if (highlight != null) {
                validMoves = game.validMoves(highlight);
                ChessMove move = new ChessMove(highlight, new ChessPosition(actualRow, actualCol), null);
                setColorByHighlight(out, isDarkSquare, validMoves.contains(move));
            } else {
                setColorDefault(out, isDarkSquare);
            }

            drawPiece(out, actualRow, actualCol);
        }

        setBlack(out);
        out.print(SET_TEXT_COLOR_GREEN);
        out.printf(" %2d", row + 1);
        out.println();
    }

    // ♟️ Draw a row from the WHITE perspective
    private static void drawRowOfSquaresPerspectiveWhite(PrintStream out, int row, ChessPosition highlight) {
        Collection<ChessMove> validMoves = null;
        int actualRow = 8 - row;

        out.print(SET_TEXT_COLOR_GREEN);
        out.printf("   %2d ", actualRow);

        for (int col = 0; col < BOARD_SIZE_IN_SQUARES; col++) {
            int actualCol = col + 1;

            boolean isDarkSquare = (row + col) % 2 == 1;
            if (highlight != null) {
                validMoves = game.validMoves(highlight);
                ChessMove move = new ChessMove(highlight, new ChessPosition(actualRow, actualCol), null);
                setColorByHighlight(out, isDarkSquare, validMoves.contains(move));
            } else {
                setColorDefault(out, isDarkSquare);
            }

            drawPiece(out, actualRow, actualCol);
        }

        setBlack(out);
        out.print(SET_TEXT_COLOR_GREEN);
        out.printf(" %2d", actualRow);
        out.println();
    }

    // 🎯 Determines whether to draw white/grey OR highlight yellow
    private static void setColorByHighlight(PrintStream out, boolean isDarkSquare, boolean isHighlight) {
        if (isHighlight) {
            setYellow(out);
        } else {
            setColorDefault(out, isDarkSquare);
        }
    }

    // 🎨 Sets standard square color
    private static void setColorDefault(PrintStream out, boolean isDarkSquare) {
        if (isDarkSquare) {
            setGrey(out);
        } else {
            setWhite(out);
        }
    }

    // ♟️ Actually print a piece OR a blank space
    private static void drawPiece(PrintStream out, int row, int col) {
        String piece = setPieceType(row, col, board);
        if (piece == null) {
            out.print(SET_TEXT_COLOR_GREEN + "   ");
        } else {
            out.print(setPieceColor(row, col, board) + " " + piece + " ");
        }
    }

    // 🎨 Background + text color helpers
    private static void setWhite(PrintStream out) {
        out.print(SET_BG_COLOR_WHITE + SET_TEXT_COLOR_WHITE);
    }

    private static void setGrey(PrintStream out) {
        out.print(SET_BG_COLOR_LIGHT_GREY + SET_TEXT_COLOR_LIGHT_GREY);
    }

    private static void setBlack(PrintStream out) {
        out.print(SET_BG_COLOR_BLACK + SET_TEXT_COLOR_BLACK);
    }

    private static void setYellow(PrintStream out) {
        out.print(SET_BG_COLOR_YELLOW + SET_TEXT_COLOR_YELLOW);
    }
}

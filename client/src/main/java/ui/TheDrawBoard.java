package ui;

import chess.*;

import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.Collection;

import static ui.EscapeSequences.*;

public class TheDrawBoard {

    private static final int BOARD_SIZE_IN_SQUARES = 8;
    private static final int SQUARE_SIZE_IN_PADDED_CHARS = 2;

    private static ChessBoard board;
    private static ChessGame game;

    // 💡 Draw board w/ highlights for a selected piece's valid moves
    public void drawHighlighted(ChessBoard board, String perspective, ChessPosition position, ChessGame game) {
        TheDrawBoard.game = game;
        TheDrawBoard.board = board;

        System.out.println("\n");
        var out = new PrintStream(System.out, true, StandardCharsets.UTF_8);

        drawHeaders(out, perspective);
        drawChessBoard(out, perspective, position);
        drawHeaders(out, perspective);
    }

    // 🎨 Plain board draw, no glowy highlight vibes
    public void draw(ChessBoard board, String perspective) {
        TheDrawBoard.board = board;

        System.out.println("\n");
        var out = new PrintStream(System.out, true, StandardCharsets.UTF_8);

        drawHeaders(out, perspective);
        drawChessBoard(out, perspective, null);
        drawHeaders(out, perspective);
    }

    // 🧾 A B C D E F G H or reversed – keepin’ the grid legit
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

    // 🧠 Switch up the perspective depending on white/black/observer
    private static void drawChessBoard(PrintStream out, String perspective, ChessPosition highlight) {
        boolean isWhitePerspective = perspective.equals("white") || perspective.equals("observer");
        for (int row = 0; row < BOARD_SIZE_IN_SQUARES; row++) {
            drawRowOfSquares(out, row, highlight, isWhitePerspective);
        }
    }

    // 🧠 This is the big brain unified row drawer – no more duplication 🚀
    private static void drawRowOfSquares(PrintStream out, int row, ChessPosition highlight, boolean isWhitePerspective) {
        Collection<ChessMove> validMoves = null;
        int displayRow = isWhitePerspective ? 8 - row : row + 1;
        out.print(SET_TEXT_COLOR_GREEN);
        out.printf("   %2d ", displayRow);

        for (int col = 0; col < BOARD_SIZE_IN_SQUARES; col++) {
            int displayCol = isWhitePerspective ? col + 1 : 8 - col;

            boolean isDarkSquare = (row + col) % 2 == 1;

            if (highlight != null) {
                validMoves = game.validMoves(highlight);
                ChessMove move = new ChessMove(highlight, new ChessPosition(displayRow, displayCol), null);
                setColorByHighlight(out, isDarkSquare, validMoves.contains(move));
            } else {
                setColorDefault(out, isDarkSquare);
            }

            drawPiece(out, displayRow, displayCol);
        }

        setBlack(out);
        out.print(SET_TEXT_COLOR_GREEN);
        out.printf(" %2d", displayRow);
        out.println();
    }

    // 💅 Give the piece its stylish team color
    private static String setPieceColor(int row, int col, ChessBoard board) {
        ChessPiece piece = board.getPiece(new ChessPosition(row, col));
        return (piece.getTeamColor() == ChessGame.TeamColor.WHITE)
                ? SET_TEXT_COLOR_BLUE
                : SET_TEXT_COLOR_BLACK;
    }

    // 🔠 Return the letter that represents the piece type
    private static String setPieceType(int row, int col, ChessBoard board) {
        ChessPiece piece = board.getPiece(new ChessPosition(row, col));
        if (piece == null) {
            return null;
        }

        return switch (piece.getPieceType()) {
            case BISHOP -> "B";
            case KNIGHT -> "N";
            case PAWN   -> "p";
            case KING   -> "K";
            case QUEEN  -> "Q";
            case ROOK   -> "R";
        };
    }

    // 🌈 Decide the vibe: default square or highlight it
    private static void setColorByHighlight(PrintStream out, boolean isDarkSquare, boolean isHighlight) {
        if (isHighlight) {
            setYellow(out);
        } else {
            setColorDefault(out, isDarkSquare);
        }
    }

    // 🧱 Paint the square: grey = dark, white = light
    private static void setColorDefault(PrintStream out, boolean isDarkSquare) {
        if (isDarkSquare) {
            setGrey(out);
        } else {
            setWhite(out);
        }
    }

    // 🎭 Draw the actual piece or just leave it blank
    private static void drawPiece(PrintStream out, int row, int col) {
        String piece = setPieceType(row, col, board);
        if (piece == null) {
            out.print(SET_TEXT_COLOR_GREEN + "   ");
        } else {
            out.print(setPieceColor(row, col, board) + " " + piece + " ");
        }
    }

    // 🎨 Background and foreground setter squad
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

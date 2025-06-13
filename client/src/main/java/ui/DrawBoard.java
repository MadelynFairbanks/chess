package ui;

import chess.*;

import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.Collection;

import static ui.EscapeSequences.*;

public class DrawBoard {
    private static final int BOARD_SIZE_IN_SQUARES = 8;
    private static final int SQUARE_SIZE_IN_PADDED_CHARS = 2;
    int middleLine = SQUARE_SIZE_IN_PADDED_CHARS / 2;

    private static ChessBoard board;
    private static ChessGame game;

    public void drawHighlighted(ChessBoard board, String perspective, ChessPosition position, ChessGame game) {
        this.game = game;
        System.out.println("\n");
        DrawBoard.board = board;
        var out = new PrintStream(System.out, true, StandardCharsets.UTF_8);
        drawHeaders(out, perspective);
        drawChessBoard(out, perspective, position);
        drawHeaders(out, perspective);
    }
    public void draw(ChessBoard board, String perspective) {
        System.out.println("\n");
        DrawBoard.board = board;
        var out = new PrintStream(System.out, true, StandardCharsets.UTF_8);
        drawHeaders(out, perspective);
        drawChessBoard(out, perspective, null);
        drawHeaders(out, perspective);
    }


    private static void drawHeaders(PrintStream out, String perspective) {
        setBlack(out);
        out.print("     ");
        String[] headers = {};
        if (perspective.equals("white")|| perspective.equals("observer")) {
            headers = new String[]{"A", "B", "C", "D", "E", "F", "G", "H"};
        } else {
            headers = new String[]{"H", "G", "F", "E", "D", "C", "B", "A"};
        }
        for (String header : headers) {
            drawHeader(out, header);
        }
        out.println();
    }

    private static void drawHeader(PrintStream out, String headerText) {
        out.print("  ");
        printHeaderText(out, headerText);

    }

    private static void printHeaderText(PrintStream out, String player) {
        out.print(SET_BG_COLOR_BLACK);
        out.print(SET_TEXT_COLOR_GREEN);

        out.print(player);

        setBlack(out);
    }

    private static void drawChessBoard(PrintStream out, String perspective, ChessPosition piecePosition) {


        for (int boardRow = 0; boardRow < BOARD_SIZE_IN_SQUARES; ++boardRow) {
            if (perspective.equals("white") || perspective.equals("observer")){
                drawRowOfSquaresPerspectiveWhite(out, boardRow, piecePosition);
            }else {
                drawRowOfSquaresPerspectiveBLack(out, boardRow, piecePosition);
            }
        }
    }

    private static String setPieceColor(int row, int col, ChessBoard board){
        ChessPiece piece = board.getPiece(new ChessPosition(row, col));
        if(piece.getTeamColor() == ChessGame.TeamColor.WHITE){
            return(SET_TEXT_COLOR_BLUE);
        } else {
            return(SET_TEXT_COLOR_BLACK);
        }
    }


    private static String setPieceType(int row, int col, ChessBoard board){
        ChessPiece piece = null;
        piece = board.getPiece(new ChessPosition(row, col));
//        System.out.println(piece.getTeamColor());
        if (piece == null){
            return null;
        }
        String picture = null;
        switch (piece.getPieceType()) {
            case BISHOP -> picture ="B";
            case KNIGHT -> picture ="N";
            case PAWN -> picture ="p";
            case KING -> picture ="K";
            case QUEEN -> picture ="Q";
            case ROOK -> picture ="R";
        }
        return picture;
    }

    private static void drawRowOfSquaresPerspectiveBLack(PrintStream out, int row, ChessPosition pieceH) {
        Collection<ChessMove> validMoves;
        out.print(SET_TEXT_COLOR_GREEN);
        out.printf("   %2d ",(row + 1));
        String piece;
        for (int boardCol = 0; boardCol < BOARD_SIZE_IN_SQUARES; ++boardCol) {
            if((row + boardCol) % 2 == 1) {
                if (pieceH == null) {
                    setGrey(out);
                } else {
                    validMoves = game.validMoves(pieceH);
                    ChessMove move = new ChessMove(pieceH, new ChessPosition(row + 1, 8 - boardCol), null);
                    if (validMoves.contains(move)){
                        setYellow(out);
                    } else {
                        setGrey(out);
                    }
                }
            }else{
                if (pieceH == null) {
                    setWhite(out);
                }else {
                    validMoves = game.validMoves(pieceH);
                    ChessMove move = new ChessMove(pieceH, new ChessPosition(row + 1, 8 - boardCol), null);
                    if (validMoves.contains(move)){
                        setYellow(out);
                    } else {
                        setWhite(out);
                    }
                }
            }
            piece = setPieceType(row + 1, 8 - boardCol, board);
            if (piece == null){
                out.print(SET_TEXT_COLOR_GREEN);
                out.print(("   "));
            }else{
                out.print(setPieceColor(row + 1, 8 - boardCol, board));
                out.print((" " +  piece + " "));
            }
        }
        setBlack(out);
        out.print(SET_TEXT_COLOR_GREEN);
        out.printf(" %2d  ",(row + 1));

        out.println();
    }

    private static void drawRowOfSquaresPerspectiveWhite(PrintStream out, int row, ChessPosition pieceH) {
        Collection<ChessMove> validMoves;
        out.print(SET_TEXT_COLOR_GREEN);
        out.printf("   %2d ",(8 - row));
        String piece;
        for (int boardCol = 0; boardCol < BOARD_SIZE_IN_SQUARES; ++boardCol) {
            if((row + boardCol) % 2 == 1) {
                if (pieceH == null) {
                    setGrey(out);
                }else {
                    validMoves = game.validMoves(pieceH);
                    ChessMove move = new ChessMove(pieceH, new ChessPosition(8 - row, boardCol + 1), null);

                    if (validMoves.contains(move)){
                        setYellow(out);
                    }else{
                        setGrey(out);
                    }
                }
            }else{
                if (pieceH == null) {
                    setWhite(out);
                }else {
                    validMoves = game.validMoves(pieceH);

                    ChessMove move = new ChessMove(pieceH, new ChessPosition(8 - row, boardCol + 1), null);
                    if (validMoves.contains(move)){
                        setYellow(out);
                    }else{
                        setWhite(out);
                    }
                }
            }
            piece = setPieceType(8 - row, boardCol + 1, board);
            if (piece == null){
                out.print(SET_TEXT_COLOR_GREEN);
                out.print(("   "));
            }else{
                out.print(setPieceColor(8 - row, boardCol + 1, board));
                out.print((" " +  piece + " "));
            }
        }
        setBlack(out);
        out.print(SET_TEXT_COLOR_GREEN);
        out.printf(" %2d   ",(8 - row));

        out.println();
    }


    private static void setWhite(PrintStream out) {
        out.print(SET_BG_COLOR_WHITE);
        out.print(SET_TEXT_COLOR_WHITE);
    }


    private static void setGrey(PrintStream out) {
        out.print(SET_BG_COLOR_LIGHT_GREY);
        out.print(SET_TEXT_COLOR_LIGHT_GREY);
    }

    private static void setBlack(PrintStream out) {
        out.print(SET_BG_COLOR_BLACK);
        out.print(SET_TEXT_COLOR_BLACK);
    }

    private static void setYellow(PrintStream out) {
        out.print(SET_BG_COLOR_YELLOW);
        out.print(SET_TEXT_COLOR_YELLOW);
    }



}

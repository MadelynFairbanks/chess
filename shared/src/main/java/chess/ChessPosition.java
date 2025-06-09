package chess;

import java.util.Objects;

/**
 * Represents a single square position on a chess board
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessPosition {
    private final int row;
    private final int column;

    public ChessPosition(int row, int column) {
        this.row = row;
        this.column = column;
    }

    public static ChessPosition fromAlgebraic(String square) {
        if (square == null || square.length() != 2) {
            throw new IllegalArgumentException("Invalid algebraic notation: " + square);
        }

        char file = square.charAt(0); // 'a' through 'h'
        char rank = square.charAt(1); // '1' through '8'

        int column = file - 'a' + 1; // a=1, b=2, ..., h=8
        int row = rank - '0';        // '1' = row 1 (bottom), '8' = row 8 (top)

        if (column < 1 || column > 8 || row < 1 || row > 8) {
            throw new IllegalArgumentException("Invalid board position: " + square);
        }

        return new ChessPosition(row, column);
    }

    /**
     * @return which row this position is in
     * 1 codes for the bottom row
     */
    public int getRow() {
        return row;
    }

    /**
     * @return which column this position is in
     * 1 codes for the left row
     */
    public int getColumn() {
        return column;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ChessPosition)) {
            return false;
        }
        ChessPosition that = (ChessPosition) o;
        return row == that.row && column == that.column;
    }

    @Override
    public int hashCode() {
        return Objects.hash(row, column);
    }

    @Override
    public String toString() {
        return "(" + row + "," + column + ")";
    }
}

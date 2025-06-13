package ui;

import chess.ChessPosition;
import model.GameData;

public class LoadBoard {

    private GameData gameData;
    private String color;
    private final DrawBoard drawBoard = new DrawBoard();  // 🖼️ Our trusty artist

    // 🧠 Load the board and draw it from a given player’s perspective
    public void loadBoard(GameData gameData, String color) {
        this.gameData = gameData;
        this.color = color;
        drawBoard.draw(gameData.game().getBoard(), color);  // 🎨 Make it pretty
    }

    // 📦 Just in case you need access to the current game data
    public GameData getGameData() {
        return gameData;
    }

    // 🔄 Redraw the current board — same game, same vibe
    public void redrawBoard() {
        drawBoard.draw(gameData.game().getBoard(), color);
    }

    // ✨ Highlight legal moves for a selected piece
    public void drawHighlightedBoard(ChessPosition position) {
        drawBoard.drawHighlighted(gameData.game().getBoard(), color, position, gameData.game());
    }
}

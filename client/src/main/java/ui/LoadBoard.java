package ui;

import chess.ChessPosition;
import model.GameData;

public class LoadBoard {
    GameData gameData;
    String color;
    DrawBoard drawBoard = new DrawBoard();


    public void loadBoard(GameData gameData, String color){
        this.gameData = gameData;
        this.color = color;
        drawBoard.draw(gameData.game().getBoard(), color);
    }

    public GameData getGameData() {
        return gameData;
    }

    public void redrawBoard(){
        drawBoard.draw(gameData.game().getBoard(), color);
    }

    public void drawHighlightedBoard(ChessPosition position) {
        drawBoard.drawHighlighted(gameData.game().getBoard(), color, position, gameData.game());
    }
}

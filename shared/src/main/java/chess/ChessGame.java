package chess;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Objects;

/**
 * For a class that can manage a chess game, making moves on a board
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessGame implements Cloneable{
    ChessBoard thisBoard = new ChessBoard();
    int turnTracker = 0;


    public ChessGame() {
        thisBoard.resetBoard();
    }

    /**
     * @return Which team's turn it is
     */
    public TeamColor getTeamTurn() {
        if((this.turnTracker % 2) == 0){
            return TeamColor.WHITE;
        } else {
            return TeamColor.BLACK;
        }
    }

    /**
     * Set's which teams turn it is
     *
     * @param team the team whose turn it is
     */
    public void setTeamTurn(TeamColor team) {
        if (team == TeamColor.WHITE){
            turnTracker = 0;
        }else{
            turnTracker = 1;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ChessGame chessGame = (ChessGame) o;
        return turnTracker == chessGame.turnTracker && Objects.equals(thisBoard, chessGame.thisBoard);
    }

    @Override
    public int hashCode() {
        return Objects.hash(thisBoard, turnTracker);
    }

    /**
     * Enum identifying the 2 possible teams in a chess game
     */
    public enum TeamColor {
        WHITE,
        BLACK
    }

    /**
     * Gets a valid moves for a piece at the given location
     *
     * @param startPosition the piece to get valid moves for
     * @return Set of valid moves for requested piece, or null if no piece at
     * startPosition
     */
    public Collection<ChessMove> validMoves(ChessPosition startPosition) {
        ChessPiece currentPiece = thisBoard.getPiece(startPosition);
        if ( currentPiece != null ) {
            Collection<ChessMove> tempMovesHolder = currentPiece.pieceMoves(thisBoard,startPosition);
            Collection<ChessMove> movesHolder = new ArrayList<>();
            for (ChessMove singleMove : tempMovesHolder){
                if (!placesKingInCheck(singleMove)){
                    movesHolder.add(singleMove);
                }
            }
            return movesHolder;
        }else{return new ArrayList<>();}
    }


    @Override
    public ChessGame clone(){
        try{
            ChessGame clone = (ChessGame) super.clone();
            clone.thisBoard = thisBoard.clone();
            return clone;
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException(e);
        }

    }

    public boolean placesKingInCheck(ChessMove move) {
        ChessGame tempGame = this.clone();
        ChessBoard tempBoard = this.thisBoard.clone();
        ChessPiece movePiece = tempBoard.getPiece(move.getStartPosition());
        if (move.getPromotionPiece() == null) {
            tempGame.thisBoard.addPiece(move.getEndPosition(), movePiece);
        } else {
            tempGame.thisBoard.addPiece(move.getEndPosition(), new ChessPiece(movePiece.getTeamColor(),move.getPromotionPiece()));
        }
        tempGame.thisBoard.addPiece(move.getStartPosition(), null);
        return tempGame.isInCheck(movePiece.getTeamColor());
    }


    /**
     * Makes a move in a chess game
     *
     * @param move chess move to perform
     * @throws InvalidMoveException if move is invalid
     */
    public void makeMove(ChessMove move) throws InvalidMoveException {
        ChessPiece movePiece = thisBoard.getPiece(move.getStartPosition());
        try {
            if (movePiece.getTeamColor() != getTeamTurn() || (!validMoves(move.startPosition).contains(move))) {
                throw new InvalidMoveException();
            }
            if (move.getPromotionPiece() == null) {
                thisBoard.addPiece(move.getEndPosition(), movePiece);
            } else {
                thisBoard.addPiece(move.getEndPosition(), new ChessPiece(movePiece.getTeamColor(),move.getPromotionPiece()));
            }
            thisBoard.addPiece(move.getStartPosition(), null);
            turnTracker += 1;
        } catch (NullPointerException e) {
            throw new InvalidMoveException();
        }
    }

    /**
     * Determines if the given team is in check
     *
     * @param teamColor which team to check for check
     * @return True if the specified team is in check
     */
    public boolean isInCheck(TeamColor teamColor) {
        ChessPosition kingPosition = getKingPosition(teamColor);
        for (int i = 1; i <= 8; i++){
            for (int j = 1; j <= 8; j++) {
                if (isThreat(i,j,teamColor,kingPosition)){
                    return true;
                }
            }
        }
        return false;
    }

    ChessPosition getKingPosition(TeamColor teamColor){
        ChessPosition kingPosition = new ChessPosition(1,1);
        boolean foundKingBool = false;
        ChessPiece king;
        for (int i = 1; i <= 8; i++){
            for (int j = 1; j <= 8; j++){
                king = thisBoard.getPiece(new ChessPosition(i, j));
                if (king != null && (king.pieceType == ChessPiece.PieceType.KING && king.getTeamColor() == teamColor)){
                        kingPosition = new ChessPosition(i,j);
                        foundKingBool = true;
                        break;
                }
            }
            if (foundKingBool) {break;}
        }
        return kingPosition;
    }


    boolean isThreat(int i, int j, TeamColor teamColor, ChessPosition kingPosition){
        ChessPiece threat = thisBoard.getPiece(new ChessPosition(i, j));
        if (threat != null && threat.getTeamColor() != teamColor) {
            ChessPosition threatPosition = new ChessPosition(i, j);
            Collection<ChessMove> threatPossibleMoves = threat.pieceMoves(thisBoard, threatPosition);
            for (ChessMove possibleMove : threatPossibleMoves){
                if (kingPosition.getRow() == possibleMove.endPosition.getRow()
                        && kingPosition.getColumn() == possibleMove.endPosition.getColumn()) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Determines if the given team is in checkmate
     *
     * @param teamColor which team to check for checkmate
     * @return True if the specified team is in checkmate
     */
    public boolean isInCheckmate(TeamColor teamColor) {
        if (isInCheck(teamColor)){
            for (int i = 1; i <= 8; i++) {
                for (int j = 1; j <= 8; j++) {
                    if (isValidMove(i,j,teamColor)){
                        return false;
                    }
                }
            }
            return true;
        }
        return false;
    }


    boolean isValidMove(int i, int j, TeamColor teamColor){
        ChessPiece pieceToCheck = thisBoard.getPiece(new ChessPosition(i, j));
        if (pieceToCheck != null && pieceToCheck.getTeamColor() == teamColor) {
            Collection<ChessMove> protectKingMoves = pieceToCheck.pieceMoves(thisBoard, new ChessPosition(i,j));
            for (ChessMove protectKingMove : protectKingMoves) {
                if (!placesKingInCheck(protectKingMove)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Determines if the given team is in stalemate, which here is defined as having
     * no valid moves
     *
     * @param teamColor which team to check for stalemate
     * @return True if the specified team is in stalemate, otherwise false
     */
    public boolean isInStalemate(TeamColor teamColor) {
        if (!isInCheck(teamColor)){
            for (int i = 1; i <= 8; i++) {
                for (int j = 1; j <= 8; j++) {
                    if (isFreshMove(i,j,teamColor)){
                        return false;
                    }
                }
            }
            return true;
        }
        return false;
    }

    boolean isFreshMove(int i, int j, TeamColor teamColor) {
        ChessPosition location = new ChessPosition(i, j);
        ChessPiece pieceToCheck = thisBoard.getPiece(location);
        if (pieceToCheck != null && pieceToCheck.getTeamColor() == teamColor) {
            Collection<ChessMove> protectKingMoves = validMoves(location);
            for (ChessMove protectKingMove : protectKingMoves) {
                if (!placesKingInCheck(protectKingMove)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Sets this game's chessboard with a given board
     *
     * @param board the new board to use
     */
    public void setBoard(ChessBoard board) {
        this.thisBoard = board;
    }

    /**
     * Gets the current chessboard
     *
     * @return the chessboard
     */
    public ChessBoard getBoard() {
        return thisBoard;
    }
}

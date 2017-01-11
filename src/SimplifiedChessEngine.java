import javafx.geometry.Pos;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Scanner;

/**
 * Created by user1 on 16-12-23.
 */
public class SimplifiedChessEngine {
    static final int ROWS = 4;
    static final int COLS = 4;
    //static final String[] COL_NAMES = {"A", "B", "C", "D"};
    //static final String[] COLOR_NAMES = {"W", "B"};


    enum Type {
        EMPTY,
        QUEEN,
        ROOK,
        BISHOP,
        KNIGHT,
        PAWN;

        public static Type fromString(String type) {
                if (type.equals("Q")) return Type.QUEEN;
                if (type.equals("R")) return Type.ROOK;
                if (type.equals("B")) return Type.BISHOP;
                if (type.equals("N")) return Type.KNIGHT;
                if (type.equals("P")) return Type.PAWN;
                return Type.EMPTY; //should never happen
        }
    }

    enum Color {
        WHITE,
        BLACK;
    }

    static int codeFigure(Color color, String type)
    {
        int code = Type.fromString(type).ordinal();
        if(color == Color.WHITE) code *= 2;
        else if(color == Color.BLACK) code = 2 * code + 1;
        else assert(false);
        return code;
    }

    static char figureToChar(int figure)
    {
        //U.print(figure);
        if(figure == Type.EMPTY.ordinal()) return '*';
        int colorOrdinal = figure % 2;
        int typeOrdinal = figure / 2;
        Color color = Color.values()[colorOrdinal];
        Type type = Type.values()[typeOrdinal];

        switch (type) {
           case QUEEN:
               return color == Color.WHITE ? 'Q' : 'q';
           case ROOK:
               return color == Color.WHITE ? 'R' : 'r';
           case BISHOP:
               return color == Color.WHITE ? 'B' : 'b';
           case KNIGHT:
               return color == Color.WHITE ? 'N' : 'n';
           case PAWN:
               return color == Color.WHITE ? 'P' : 'p';
           default:
               return '?'; //invalid should never happen
        }
    }

    static Color getColor(int figure)
    {
        return Color.values()[figure%2];
    }

    static Type getType(int figure) {
        return Type.values()[figure / 2];
    }

    static Color oppositeColor(Color color) {
        if (color == Color.WHITE) return Color.BLACK;
        else return Color.WHITE;
    }


    static void printBoard(int[][] board)
    {
        System.out.println("   A B C D");
        for (int i = 0; i < ROWS; i++) {
            System.out.print((ROWS-i) +"| ");
            for (int j = 0; j < COLS; j++) {
                System.out.print(figureToChar(board[i][j]) + " ");
            }
            System.out.println();
        }
    }


    static class Position {
        public int row;
        public int column;

        public Position(int row, int column) {
            this.row = row;
            this.column = column;
        }
    }

    static List<Position> getMoves(Type figureType, Position currentPosition, int[][] board)
    {
        List<Position> result = new ArrayList<Position>();

        switch(figureType)
        {
            case QUEEN:
                result.addAll(getStraightMoves(currentPosition, board));
                result.addAll(getDiagonalMoves(currentPosition, board));
                break;
            case BISHOP:
                result.addAll(getDiagonalMoves(currentPosition, board));
                break;
            case ROOK:
                result.addAll(getStraightMoves(currentPosition, board));
                break;
            case KNIGHT:
                result.addAll(getKnightMoves(currentPosition));
                break;
            case PAWN:
                result.addAll(getPawnMoves(currentPosition, board));
                break;
            default:
                //do nothing

        }
        return result;
    }

    static List<Position> getPawnMoves(Position current, int[][] board) {
        List<Position> result = new ArrayList<Position>();
        int figure = board[current.row][current.column];
        Color color = getColor(figure);

        //white pawn going up
        if (color == Color.WHITE && current.row - 1 >= 0) { //there is row ahead
            if (getType(board[current.row-1][current.column]) == Type.EMPTY) {
                result.add(new Position(current.row-1, current.column));
            }
            if (current.column - 1 >= 0 &&
                    getColor(board[current.row-1][current.column-1]) == Color.BLACK) {
                result.add(new Position(current.row-1, current.column-1));
            }
            if (current.column + 1 < COLS &&
                    getColor(board[current.row-1][current.column+1]) == Color.BLACK) {
                result.add(new Position(current.row-1, current.column+1));
            }
        }

        //black pawn going down
        if (color == Color.BLACK && current.row + 1 < ROWS) { //there is row ahead
            if (getType(board[current.row+1][current.column]) == Type.EMPTY) {
                result.add(new Position(current.row+1, current.column));
            }
            if (current.column - 1 >= 0 &&
                    getColor(board[current.row+1][current.column-1]) == Color.BLACK) {
                result.add(new Position(current.row+1, current.column-1));
            }
            if (current.column + 1 < COLS &&
                    getColor(board[current.row+1][current.column+1]) == Color.BLACK) {
                result.add(new Position(current.row+1, current.column+1));
            }
        }


        return result;
    }




    static List<Position> getStraightMoves(Position current, int[][] board) {
        List<Position> result = new ArrayList<Position>();
        //up
        for(int i = current.row + 1; i < ROWS; i++) {
            Position newPosition = new Position(i, current.column);
            result.add(newPosition);
            if(getType(board[newPosition.row][newPosition.column]) != Type.EMPTY) break;
        }

        //down
        for(int i = current.row - 1; i >= 0; i--) {
            Position newPosition = new Position(i, current.column);
            result.add(newPosition);
            if(getType(board[newPosition.row][newPosition.column]) != Type.EMPTY) break;
        }

        //right
        for(int i = current.column + 1; i < COLS; i++) {
            Position newPosition = new Position(current.row, i);
            result.add(newPosition);
            if(getType(board[newPosition.row][newPosition.column]) != Type.EMPTY) break;
        }

        //left
        for(int i = current.column - 1; i >= 0; i--) {
            Position newPosition = new Position(current.row, i);
            result.add(newPosition);
            if(getType(board[newPosition.row][newPosition.column]) != Type.EMPTY) break;
        }


        return result;
    }

    static List<Position> getDiagonalMoves(Position current, int[][] board) {
        List<Position> result = new ArrayList<Position>();

        //up-right
        int r;
        int c;
        for (r = current.row+1, c = current.column+1; r < ROWS && c < COLS; r++,c++){
            Position newPosition = new Position(r, c);
            result.add(newPosition);
            if(getType(board[newPosition.row][newPosition.column]) != Type.EMPTY) break;
        }

        //bottom-left
        for (r = current.row-1, c = current.column-1; r >= 0 && c >= 0; r--,c--){
            Position newPosition = new Position(r, c);
            result.add(newPosition);
            if(getType(board[newPosition.row][newPosition.column]) != Type.EMPTY) break;
        }

        //up-left
        for (r = current.row+1, c = current.column-1; r < ROWS && c >= 0; r++,c--){
            Position newPosition = new Position(r, c);
            result.add(newPosition);
            if(getType(board[newPosition.row][newPosition.column]) != Type.EMPTY) break;
        }

        //bottom-right
        for (r = current.row-1, c = current.column+1; r >= 0 && c < COLS; r--,c++){
            Position newPosition = new Position(r, c);
            result.add(newPosition);
            if(getType(board[newPosition.row][newPosition.column]) != Type.EMPTY) break;
        }

        return result;
    }


    static boolean onBoard(Position position) {
        return position.row >= 0 && position.row < ROWS && position.column >= 0 && position.column < COLS;
    }

    static List<Position> getKnightMoves(Position current) {
        List<Position> result = new ArrayList<Position>();

        int rs[] = {1,2,2,1,-1,-2,-2,-1};
        int cs[] = {-2,-1,1,2,2,1,-1,-2};

        for (int i = 0; i < 8; i++) {
            int r = current.row + rs[i];
            int c = current.column + cs[i];
            Position newPosition = new Position(r, c);
            if (onBoard(newPosition)) result.add(newPosition);
        }
        return result;
    }


    static int[][] copy(int[][] input) { //n x n
        int n = input.length;
        int[][] result = new int[n][n];

        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                result[i][j] = input[i][j];
            }
        }
        return result;
    }

    static boolean shouldBePromoted(Position position, Color color, Type type) {
        if (type != Type.PAWN) return false;
        if (color == Color.WHITE && position.row == 0) return true;
        if (color == Color.BLACK && position.row == ROWS-1) return true;
        return false;
    }

     static Color solve(Color colorToMove, int[][] board, int remainingMoves)
    {
        U.print("remaining moves: " + remainingMoves);
        U.print(colorToMove == Color.WHITE ? "W" : "B");

        // U.print("calculating board");
        printBoard(board);
        if(remainingMoves == 0) return Color.BLACK;

        for (int i = 0; i < ROWS; i++ ) {
            for (int j = 0; j < COLS; j++) {
                int figure = board[i][j];
                Type type = getType(figure);
                Color color = getColor(figure);
                if ( type != Type.EMPTY && color == colorToMove) {
                    List<Position> moves = getMoves(type, new Position(i, j), board);
                    for (Position move : moves) {
                        int targetFigure = board[move.row][move.column];
                        Type targetType = getType(targetFigure);
                        Color targetColor = getColor(targetFigure);

                        //cannot step on a figure of the same color
                        if (targetType != Type.EMPTY && targetColor == colorToMove) continue;
                        //killing the opponent's queen
                        if (targetType == Type.QUEEN && targetColor == oppositeColor(colorToMove)) return colorToMove;

                        //move to empty or opponent's field
                        if (targetType == Type.EMPTY || targetColor == oppositeColor(colorToMove)) {
                            board[i][j] = Type.EMPTY.ordinal();
                            board[move.row][move.column] = figure;
                            if (shouldBePromoted(move, color, type)) { //create 3 new boards with all possible promotions
                                board[move.row][move.column] = codeFigure(colorToMove, "R");
                                Color winner = solve(oppositeColor(colorToMove), board, remainingMoves-1);
                                if (winner == colorToMove) {
                                    //reset
                                    board[i][j] = figure;
                                    board[move.row][move.column] = targetFigure;
                                    return colorToMove;
                                }


                                board[move.row][move.column] = codeFigure(colorToMove, "B");
                                winner = solve(oppositeColor(colorToMove), board, remainingMoves-1);
                                if (winner == colorToMove) {
                                    //reset
                                    board[i][j] = figure;
                                    board[move.row][move.column] = targetFigure;
                                    return colorToMove;
                                }


                                board[move.row][move.column] = codeFigure(colorToMove, "N");
                                U.print("solve it for the king");
                                winner = solve(oppositeColor(colorToMove), board, remainingMoves-1);
                                if (winner == colorToMove) {
                                    //reset
                                    board[i][j] = figure;
                                    board[move.row][move.column] = targetFigure;
                                    return colorToMove;
                                }
                                //reset
                                board[i][j] = figure;
                                board[move.row][move.column] = targetFigure;

                            } else {
                                Color winner = solve(oppositeColor(colorToMove), board, remainingMoves-1);
                                //reset
                                board[i][j] = figure;
                                board[move.row][move.column] = targetFigure;
                                if (winner == colorToMove) return colorToMove;
                            }
                        }

                    }

                }
            }
        }
        return oppositeColor(colorToMove);
    }


    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        int g = in.nextInt();
        for(int a0 = 0; a0 < g; a0++){
            int w = in.nextInt();
            int b = in.nextInt();
            int m = in.nextInt();

            int[][] board = new int[ROWS][COLS];

            //U.print("white: " + w);
            //U.print("black: " + b);

            for(int i = 0; i < w + b; i++){
                String type = in.next();
                Color color = i < w ? Color.WHITE : Color.BLACK;

                //U.print("type: " + type);
                int figure = codeFigure(color, type);
                String column = in.next();
                int columnInt = column.charAt(0) - 'A';
                int row = Integer.parseInt(in.next());
                board[ROWS-row][columnInt] = figure;
            }
            //U.print(board);
            U.print("Initial board:");
            printBoard(board);
            Color winner = solve(Color.WHITE, board, m);
            System.out.println(winner == Color.WHITE ? "YES" : "NO");
        }
    }

}

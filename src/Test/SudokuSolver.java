package Test;

import org.jacop.core.Store;
import org.jacop.core.IntVar;
import org.jacop.constraints.*;
import org.jacop.search.*;
import java.util.Arrays;

public class SudokuSolver {
    public static void main(String[] args) {
        long startTime = System.currentTimeMillis();

        int[][] board = {
                {0, 7, 0, 9, 0, 0, 2, 0, 4},
                {4, 0, 2, 0, 0, 5, 9, 0, 0},
                {0, 6, 0, 0, 4, 2, 0, 8, 5},
                {3, 0, 0, 0, 7, 4, 5, 1, 0},
                {6, 0, 7, 5, 0, 3, 8, 0, 0},
                {0, 2, 4, 8, 6, 0, 0, 0, 3},
                {8, 3, 0, 6, 2, 0, 0, 5, 9},
                {0, 0, 6, 1, 0, 0, 0, 0, 8},
                {0, 0, 9, 0, 0, 0, 6, 7, 0}
        };

        System.out.println("SUDOKU BOARD:");
        printBoard(board);
        solve(board);

        long endTime = System.currentTimeMillis();
        System.out.println("\n*** Execution time: " + (endTime - startTime) + " ms");
    }


    private static void solve(int[][] board) {
        Store store = new Store();

        IntVar[][] possibleBoard = new IntVar[board.length][board.length];

        for (int i = 0; i < board.length; i++ ) {
            for (int j = 0; j < board.length; j++) {
                if (board[i][j] == 0) {
                    possibleBoard[i][j] = new IntVar(store, "board[" + i + "," + j + "]", 1, board.length);
                } else {
                    possibleBoard[i][j] = new IntVar(store, "board[" + i + "," + j + "]", board[i][j], board[i][j]);
                }
            }
        }

        // Horizontal/Vertical constraints
        for (int i = 0; i < board.length; i++) {
            for (int j = 0; j < board.length; j++) {
                for (int k = i + 1; k < board.length; k++) {
                    store.impose(new XneqY(possibleBoard[i][j], possibleBoard[k][j]));
                }
                for (int k = j + 1; k < board.length; k++) {
                    store.impose(new XneqY(possibleBoard[i][j], possibleBoard[i][k]));
                }
            }
        }

        // 2x2 box constraints
        for (int i = 0; i < board.length; i+=3) {
            for (int j = 0; j < board.length; j+=3) {
                store.impose(new XneqY(possibleBoard[i][j], possibleBoard[i+1][j+1]));
                store.impose(new XneqY(possibleBoard[i][j], possibleBoard[i+2][j+1]));
                store.impose(new XneqY(possibleBoard[i][j], possibleBoard[i+1][j+2]));
                store.impose(new XneqY(possibleBoard[i][j], possibleBoard[i+2][j+2]));
            }
        }

        //System.out.println("Number of variables: " + store.size() +
         //       "\nNumber of constraints: " + store.numberConstraints());

        Search<IntVar> search = new DepthFirstSearch<>();
        SelectChoicePoint<IntVar> select = new SimpleMatrixSelect<>(possibleBoard, new SmallestDomain<>(), new IndomainMin<>());

//        System.out.println(store);

        if (search.labeling(store, select)) {
          //  System.out.println("\n*** Found solution.");
          //  System.out.println("Solution cost is: " + -1);
        } else {
            System.out.println("\n*** No solution found.");
        }

        System.out.println("");
        System.out.println("SOLVED SUDOKU:");
        for (int i=0; i < board.length; i++) {
            for (int j=0; j < board[0].length; j++) {
                System.out.print(possibleBoard[i][j].value() + " ");
            }
            System.out.print("\n");
        }
    }

    private static void printBoard(int[][] board) {
        for (int i=0; i < board.length; i++) {
            for (int j=0; j < board[0].length; j++) {
                System.out.print(board[i][j] + " ");
            }
            System.out.print("\n");
        }
    }
}
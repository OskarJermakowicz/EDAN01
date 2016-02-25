package Lab4;

import org.jacop.core.Store;

import java.util.ArrayList;

public class AutoRegressionFilter {
    public static void main(String[] args) {
        long startTime = System.currentTimeMillis();

        // First element: clock cycles of operation. Second element: how many operations.
        int[] multipliers = { 2, 4 };
        int[] adders = { 1, 2 };

        ArrayList<int[]> input = new ArrayList<>();
        input.add(multipliers);
        input.add(adders);

        solve(input);

        long endTime = System.currentTimeMillis();
        System.out.println("\n*** Execution time: " + (endTime - startTime) + " ms");
    }

    private static void solve(ArrayList<int[]> input) {
        /**
         * Initialize variables
         */
        Store store = new Store();


    }
}

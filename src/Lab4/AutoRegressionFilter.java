package Lab4;

import org.jacop.constraints.Cumulative;
import org.jacop.core.IntVar;
import org.jacop.core.Store;
import org.jacop.search.*;

public class AutoRegressionFilter {
    public static void main(String[] args) {
        long startTime = System.currentTimeMillis();

        solve(new ar_1_1());
        //solve(new ar_1_2());
        //solve(new ar_1_3());
        //solve(new ar_2_2());
        //solve(new ar_2_3());
        //solve(new ar_2_4());

        long endTime = System.currentTimeMillis();
        System.out.println("\n*** Execution time: " + (endTime - startTime) + " ms");
    }

    private static void solve(ar_input input) {
        /**
         * Import input data
         */
        int del_add = input.del_add;
        int del_mul = input.del_mul;

        int number_add = input.number_add;
        int number_mul = input.number_mul;
        int n = input.n;

        int[] last = input.last;
        int[] add = input.add;
        int[] mul = input.mul;

        int[] dependencies = input.dependencies;

        /**
         * Initialize variables
         */
        Store store = new Store();

        IntVar[] addStarts = new IntVar[number_add];
        IntVar[] addDurations = new IntVar[number_add];
        IntVar[] addResources = new IntVar[number_add];

        IntVar[] mulStarts = new IntVar[number_mul];
        IntVar[] mulDurations = new IntVar[number_mul];
        IntVar[] mulResources = new IntVar[number_mul];

        /**
         * Add constraints
         */

        //........

        IntVar addLimit = new IntVar(store, "addLimit", 0, number_add);
        IntVar mulLimit = new IntVar(store, "mulLimit", 0, number_mul);
        store.impose(new Cumulative(addStarts, addDurations, addResources, addLimit));
        store.impose(new Cumulative(mulStarts, mulDurations, mulResources, mulLimit));


        /**
         * Search & solve
         */
        System.out.println("Number of variables: " + store.size() +
                "\nNumber of constraints: " + store.numberConstraints());

        Search<IntVar> search = new DepthFirstSearch<>();
        SelectChoicePoint<IntVar> select = new SimpleMatrixSelect<>(null, null, new IndomainMin<>()); //change 1st null

        System.out.println(store);

        if (search.labeling(store, select, null)) { //change null
            System.out.println("\n*** Found solution.");
            System.out.println("Solution cost is: " + "-");
        } else {
            System.out.println("\n*** No solution found.");
        }
    }
}
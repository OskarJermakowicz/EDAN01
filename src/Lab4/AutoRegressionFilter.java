package Lab4;

import org.jacop.core.Store;
import org.jacop.core.IntVar;
import org.jacop.constraints.*;
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
        int[][] dependencies = input.dependencies;

        /**
         * Initialize variables
         */
        Store store = new Store();

        // Starts vectors: when the operation at index starts
        // Durations vectors: how many clock-cycles the operation takes
        // Resources vectors: how many resources the operation needs
        IntVar[] addStarts = new IntVar[add.length];
        IntVar[] addDurations = new IntVar[add.length];
        IntVar[] addResources = new IntVar[add.length];

        IntVar[] mulStarts = new IntVar[mul.length];
        IntVar[] mulDurations = new IntVar[mul.length];
        IntVar[] mulResources = new IntVar[mul.length];

        // First "rectangle": contains when an operation starts and its clock-cycle length
        IntVar[] origin1 = new IntVar[n];
        IntVar[] length1 = new IntVar[n];

        // Second "rectangle": contains index of operations (length = 1)
        IntVar[] origin2 = new IntVar[n];
        IntVar[] length2 = new IntVar[n];

        int maxTime = add.length * del_add + mul.length * del_mul;

        for (int i = 0; i < n; i++) {
            if (i < add.length) {
                addStarts[i] = new IntVar(store, "addStarts[" + i + "]", 0, maxTime);
                addDurations[i] = new IntVar(store, "addDurations[" + i + "]", del_add, del_add);
                addResources[i] = new IntVar(store, "addResources[" + i + "]", 1, 1);
            } else {
                mulStarts[i - add.length] = new IntVar(store, "mulStarts[" + (i - add.length) + "]", 0, maxTime);
                mulDurations[i - add.length] = new IntVar(store, "mulDurations[" + (i - add.length) + "]", del_mul, del_mul);
                mulResources[i - add.length] = new IntVar(store, "mulResources[" + (i - add.length) + "]", 1, 1);
            }
        }

        for (int i = 0; i < n; i++) {
            origin1[i] = new IntVar(store, "origin1[" + i + "]", 0, maxTime);
            length2[i] = new IntVar(store, "length2[" + i + "]", 1, 1);

            if (i < add.length) {
                origin2[add[i] - 1] = new IntVar(store, "origin2[" + i + "]", 0, number_add - 1);
                length1[add[i] - 1] = new IntVar(store, "length1[" + i + "]", del_add, del_add);
            } else {
                origin2[mul[i - add.length] - 1] = new IntVar(store, "origin2[" + (i - add.length) + "]", number_add, n - 1);
                length1[mul[i - add.length] - 1] = new IntVar(store, "length1[" + (i - add.length) + "]", del_mul, del_mul);
            }
        }

        /**
         * Add constraints
         */
        for (int i = 0; i < n; i++) {
            if (i < add.length) {
                // Constraint: the start times for add operations must be the same
                store.impose(new XeqY(origin1[add[i] - 1], addStarts[i]));
            } else {
                // Constraint: the start times for mul operations must be the same
                store.impose(new XeqY(origin1[mul[i - add.length] - 1], mulStarts[i - add.length]));
            }
        }

        IntVar cost = new IntVar(store, "cost", 0, maxTime);
        IntVar[] endTimes = new IntVar[n];
        for (int i = 0; i < dependencies.length; i++) {
            for (int j = 0; j < dependencies[i].length; j++) {
                // Constraint: determines the end-time for an operation
                IntVar endTime = new IntVar(store, "endTime[" + i + "," + j + "]", 0, maxTime);
                store.impose(new XplusYeqZ(origin1[dependencies[i][j]], length1[dependencies[i][j]], endTime));

                // Constraint: the operation cannot start before the operation its dependent on has finished
                store.impose(new XgteqY(origin1[i], endTime));
            }
            // Constraint: determines the end-time for each operation
            endTimes[i] = new IntVar(store, "endTimes[" + i + "]", 0, maxTime);
            store.impose(new XplusYeqZ(origin1[i], length1[i], endTimes[i]));

            // Constraint: the max value of each element can not be greater than cost
            store.impose(new Not(new XgtY(endTimes[i], cost)));
        }

        // Constraint: determines how to schedule the operations
        IntVar addLimit = new IntVar(store, "addLimit", 0, number_add);
        IntVar mulLimit = new IntVar(store, "mulLimit", 0, number_mul);
        store.impose(new Cumulative(addStarts, addDurations, addResources, addLimit));
        store.impose(new Cumulative(mulStarts, mulDurations, mulResources, mulLimit));

        // Constraint: make sure that no operations overlap
        store.impose(new Diff2(origin1, origin2, length1, length2));

        /**
         * Search & solve
         */
        System.out.println("Number of variables: " + store.size() +
                "\nNumber of constraints: " + store.numberConstraints());

        Search<IntVar> search = new DepthFirstSearch<>();
        SelectChoicePoint<IntVar> select = new SimpleSelect<>(endTimes, new SmallestDomain<>(), new IndomainMin<>());

        System.out.println(store);

        if (search.labeling(store, select, cost)) {
            System.out.println("\n*** Found solution.");
            System.out.println("Solution cost is: " + cost.value());
        } else {
            System.out.println("\n*** No solution found.");
        }
    }
}
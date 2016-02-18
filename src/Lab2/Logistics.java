package Lab2;

import org.jacop.core.Store;
import org.jacop.core.IntVar;
import org.jacop.constraints.*;
import org.jacop.search.*;
import java.util.Arrays;

public class Logistics {
	public static void main(String[] args) {
		long startTime = System.currentTimeMillis();

//		System.out.println("\n*** Expected result: 20");
//		int graph_size = 6;
//		int start = 1;
//		int n_dests = 1;
//		int[] dest = { 6 };
//		int n_edges = 7;
//		int[] from = { 1, 1, 2, 2, 3, 4, 4 };
//		int[] to = { 2, 3, 3, 4, 5, 5, 6 };
//		int[] cost = { 4, 2, 5, 10, 3, 4, 11 };
//		optimalDistance(graph_size, start, n_dests, dest, n_edges, from, to, cost);

//		System.out.println("\n*** Expected result: 20");
//		int graph_size = 6;
//		int start = 1;
//		int n_dests = 2;
//		int[] dest = { 5, 6 };
//		int n_edges = 7;
//		int[] from = { 1, 1, 2, 2, 3, 4, 4 };
//		int[] to = { 2, 3, 3, 4, 5, 5, 6 };
//		int[] cost = { 4, 2, 5, 10, 3, 4, 11 };
//		optimalDistance(graph_size, start, n_dests, dest, n_edges, from, to, cost);

		System.out.println("\n*** Expected result: 11");
		int graph_size = 6;
		int start = 1;
		int n_dests = 2;
		int[] dest = { 5, 6 };
		int n_edges = 9;
		int[] from = { 1, 1, 1, 2, 2, 3, 3, 3, 4 };
		int[] to = { 2, 3, 4, 3, 5, 4, 5, 6, 6 };
		int[] cost = { 6, 1, 5, 5, 3, 5, 6, 4, 2 };
		optimalDistance(graph_size, start, n_dests, dest, n_edges, from, to, cost);

		long endTime = System.currentTimeMillis();
		System.out.println("\n*** Execution time: " + (endTime - startTime) + " ms");
	}

	private static void optimalDistance(int graph_size, int start, int n_dests, int[] dest, int n_edges, int[] from,
										int[] to, int[] cost) {
		/**
		 * Initialize variables
		 */
		Store store = new Store();

		// graph_size is increased by one so that the nodes have the same value as their index in the distances
		// representation matrix.
		// The first value of each row (and each sub-circuit) in the paths matrix will not be used. This means that the
		// nodes will have value+1 in paths matrix.
		graph_size++;

		// Possible paths
		IntVar[][] paths = new IntVar[n_dests][graph_size];
		// Representation of the graph for easier understanding.
		int[][] distances = new int[graph_size][graph_size];

		final IntVar ZERO = new IntVar(store, "ZERO", 0, 0);

		/**
		 * Populate the matrices
		 */
		for (int i = 0; i < n_dests; i++) {
			for (int j = 0; j < graph_size; j++) {
				paths[i][j] = new IntVar(store, "paths[" + i + "," + j + "]", 1, graph_size);
			}
		}

		// Fill matrix with possible paths (with the cost of travel as weights), 0 to not move at a node or move to
		// start, -1 if path is not possible to take.
		for (int i = 0; i < graph_size; i++) {
			Arrays.fill(distances[i], -1);
		}

		for (int i = 0; i < graph_size; i++) {
			for (int j = 0; j < graph_size; j++) {
				if (i == j) {
					distances[i][j] = 0;
				}
			}
			if (n_dests > i) {
				distances[dest[i]][start] = 0;
			}
		}

		for (int i = 0; i < n_edges; i++) {
			distances[from[i]][to[i]] = cost[i];
			distances[to[i]][from[i]] = cost[i];
		}

		/**
		 * Add constraints
		 */
		for (int i = 0; i < n_dests; i++) {
			// Constraint: make each row a sub-circuit (there must be a path to each destination)
			store.impose(new Subcircuit(paths[i]));

			// Constraint: the sub-circuit has to start in start and end in dest
			IntVar startVar = new IntVar(store, "startVar[" + i + "]", start + 1, start + 1);
			IntVar destVar = new IntVar(store, "destVar[" + i + "]", dest[i] + 1, dest[i] + 1);
			store.impose(new XneqY(paths[i][start], startVar));
			store.impose(new XneqY(paths[i][dest[i]], destVar));
		}

		IntVar[][] travelCosts = new IntVar[n_dests][graph_size];
		for (int i = 0; i < n_dests; i++) {
			for (int j = 0; j < graph_size; j++) {
				// Constraint: define the cost to travel from a certain node in the sub-circuit
				IntVar maxDistance = new IntVar(store, "maxDistance[" + i + "," + j + "]", 0, maxValueOfArray(cost));
				store.impose(new Element(paths[i][j], distances[j], maxDistance));

				// Constraint: if you traveled a path before, it is now free of cost
				travelCosts[i][j] = new IntVar(store, "travelCosts[" + i + "," + j + "]", 0, maxValueOfArray(cost));
				if ((i + 1) < n_dests) {
					store.impose(new IfThenElse(new XeqY(paths[i][j], paths[i+1][j]), new XeqY(travelCosts[i][j], ZERO), new XeqY(travelCosts[i][j], maxDistance)));
				} else {
					store.impose(new XeqY(travelCosts[i][j], maxDistance));
				}
			}
		}

		// Constraint: combine the rows of travelCosts into one (add their costs)
		IntVar[] costSum = new IntVar[graph_size];
		for (int i = 0; i < graph_size; i++) {
			costSum[i] = new IntVar(store, "costSum[" + i + "]", 0, sumArray(cost));
			store.impose(new SumInt(store, getColumn(travelCosts, i), "==", costSum[i]));
		}

		// Constraint: final weight of path
		IntVar totalDistance = new IntVar(store, "totalCost", 0, sumArray(cost));
		store.impose(new SumInt(store, costSum, "==", totalDistance));

		/**
		 * Search & print solution
		 */
		System.out.println("Number of variables: " + store.size() +
				"\nNumber of constraints: " + store.numberConstraints());

		Search<IntVar> search = new DepthFirstSearch<>();
		SelectChoicePoint<IntVar> select = new SimpleMatrixSelect<>(paths, null, new IndomainMin<>());

		System.out.println(store);

		if (search.labeling(store, select, totalDistance)) {
			System.out.println("\n*** Found solution.");
			System.out.println("Solution cost is: " + totalDistance.value());
		} else {
			System.out.println("\n*** No solution found.");
		}

		/**
		 * Additional printouts
		 */
		System.out.println("\n*** Distances matrix:");
		printIntMatrix(distances);
		System.out.println("\n*** Paths taken matrix:");
		printIntVarMatrix(paths, dest);
	}

	/**
	 * Gets the sum of an int array
	 */
	private static int sumArray(int[] array) {
		int sum = 0;
		for (int var : array) {
			sum += var;
		}
		return sum;
	}

	/**
	 * Gets the max value in an int array
	 */
	private static int maxValueOfArray(int[] array) {
		int max = -1;
		for (int val : array) {
			if (val > max) {
				max = val;
			}
		}
		return max;
	}

	/**
	 * Gets an IntVar array of the column at an index in a matrix.
	 */
	private static IntVar[] getColumn(IntVar[][] matrix, int index) {
		IntVar[] column = new IntVar[matrix.length];
		for (int i = 0; i < matrix.length; i++) {
			column[i] = matrix[i][index];
		}
		return column;
	}

	/**
	 * Prints a matrix containing ints
     */
	private static void printIntMatrix(int[][] matrix) {
		for (int i = 0; i < matrix.length; i++) {
			for (int j = 0; j < matrix[0].length; j++) {
				System.out.print(matrix[i][j] + "\t");
			}
			System.out.println("");
		}
	}

	/**
	 * Prints a matrix containing IntVars
	 */
	private static void printIntVarMatrix(IntVar[][] matrix, int[] dest) {
		for (int i = 0; i < matrix.length; i++) {
			System.out.print("To reach " + dest[i] + ":\t");
			for (int j = 1; j < matrix[0].length; j++) {
				if ((matrix[i][j].value() - 1) == j) {
					System.out.print(0 + "\t");
				} else {
					System.out.print(matrix[i][j].value() - 1 + "\t");
				}
			}
			System.out.println("");
		}
	}
}
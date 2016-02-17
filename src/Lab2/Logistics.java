package Lab2;

import org.jacop.core.Store;
import org.jacop.core.IntVar;
import org.jacop.constraints.*;
import org.jacop.search.*;
import java.util.ArrayList;
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
		graph_size++;

		// Possible paths
		IntVar[][] paths = new IntVar[n_dests][graph_size];
		// Representation of the graph for easier understanding
		int[][] distances = new int[graph_size][graph_size];

		final IntVar ZERO = new IntVar(store, "ZERO", 0, 0);
		final IntVar ONE = new IntVar(store, "ONE", 1, 1);
		final IntVar START = new IntVar(store, "START", start, start);

		/**
		 * Populate the matrices
		 */
		for (int i = 0; i < n_dests; i++) {
			for (int j = 0; j < graph_size; j++) {
				paths[i][j] = new IntVar(store, "paths[" + i + "," + j + "]", 0, graph_size);
			}
		}

		// Fill matrix with possible paths (with the cost of travel), 0 to not move at a node or move to start, -1 if
		// path is not possible to take.
		for (int i = 0; i < graph_size; i++) {
			Arrays.fill(distances[i], -1);
			for (int j = 0; j < graph_size; j++) {
				if (i == j) {
					distances[i][j] = 0;
				}
			}
		}

		for (int i = 0; i < n_edges; i++) {
			distances[from[i]][to[i]] = cost[i];
			distances[to[i]][from[i]] = cost[i];
		}

		for (int i = 0; i < n_dests; i++) {
			distances[dest[i]][start] = 0;
		}

		/**
		 * Add constraints
		 */
		// Constraint: make each row a sub-circuit (there must be a path to each destination)
		// Constraint: the sub-circuit has to start in start and end in dest

		// Constraint: Make the search minimize the result based on cost.
		IntVar maxDistance = new IntVar(store, "cost", 0, sumArray(cost));
		store.impose(new SumInt(store, distance, "==", maxDistance));

		/**
		 * Search & print solution
		 */
		System.out.println("Number of variables: " + store.size() +
				"\nNumber of constraints: " + store.numberConstraints());

		Search<IntVar> search = new DepthFirstSearch<>();
		SelectChoicePoint<IntVar> select = new SimpleSelect<>(toIntVarArray(paths), null, new IndomainMin<>());

		//System.out.println(store);

		if (search.labeling(store, select, maxDistance)) {
			System.out.println("\n*** Found solution.");
			System.out.println("Solution cost is: " + maxDistance.value());
		} else {
			System.out.println("\n*** No solution found.");
		}
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
	 * Takes all values from a matrix and inserts into an array
	 */
	private static IntVar[] toIntVarArray(IntVar[][] matrix) {
		IntVar[] array = new IntVar[matrix.length * matrix[0].length];
		int index = 0;
		for (int i = 0; i < matrix.length; i++) {
			for (int j = 0; j < matrix[0].length; j++) {
				array[index] = matrix[i][j];
				index++;
			}
		}
		return array;
	}
}
import org.jacop.core.Store;
import org.jacop.core.IntVar;
import org.jacop.constraints.*;
import org.jacop.search.*;

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

	@SuppressWarnings("deprecation")
	private static void optimalDistance(int graph_size, int start, int n_dests, int[] dest, int n_edges, int[] from,
			int[] to, int[] cost) {
		/**
		 * Initialize variables
		 */
		Store store = new Store();

		IntVar[][] paths = new IntVar[n_dests][graph_size];
		int[][] distances = new int[graph_size][graph_size];

		final IntVar ZERO = new IntVar(store, "ZERO", 0, 0);
		final IntVar ONE = new IntVar(store, "ONE", 1, 1);

		/**
		 * Populate all matrices
		 */
		for (int i = 0; i < n_dests; i++) {
			for (int j = 0; j < graph_size; j++) {
				paths[i][j] = new IntVar(store, "paths[" + i + "," + j + "]", 0, graph_size);
			}
		}

		for (int i = 0; i < graph_size; i++) {
			for (int j = 0; j < graph_size; j++) {
				for (int k = 0; k < cost.length; k++) {
					if ((i + 1 == from[k] && j + 1 == to[k]) || (i + 1 == to[k] && j + 1 == from[k])) {
						distances[i][j] = cost[k];
					} else {
						distances[i][j] = 0;
					}

				}
			}
		}

		/**
		 * Add constraints
		 */
		// Constraint: make each row a sub-circuit (there must be a path to each destination)
		for (int i = 0; i < n_dests; i++) {
			store.impose(new Subcircuit(paths[i]));
		}

		// Constraint: minimize by considering which paths were taken and what their cost was
		IntVar distance = new IntVar(store, "cost", 0, sumArray(cost));
		store.impose(new SumWeight(toIntVarArray(paths), toIntArray(distances), distance));

		/**
		 * Search & print solution
		 */
		System.out.println("Number of variables: " + store.size() +
				"\nNumber of constraints: " + store.numberConstraints());

		Search<IntVar> search = new DepthFirstSearch<>();
		SelectChoicePoint<IntVar> select = new SimpleMatrixSelect<>(paths, null, new IndomainMin<>());

		if (search.labeling(store, select, distance)) {
			System.out.println("\n*** Found solution.");
			// Print path
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
	 * Takes all values from a matrix and inserts into an array
     */
	private static int[] toIntArray(int[][] matrix) {
		int[] array = new int[matrix.length * matrix[0].length];
		int index = 0;
		for (int i = 0; i < matrix.length; i++) {
			for (int j = 0; j < matrix[0].length; j++) {
				array[index] = matrix[i][j];
				index++;
			}
		}
		return array;
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
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
		IntVar[][] costs = new IntVar[n_dests][graph_size];

		final IntVar ZERO = new IntVar(store, "ZERO", 0, 0);
		final IntVar ONE = new IntVar(store, "ONE", 1, 1);
		final IntVar START = new IntVar(store, "START", start, start);

		/**
		 * Populate the matrix
		 */
		for (int i = 0; i < n_dests; i++) {
			int fromNode = 1;
			for (int j = 0; j < graph_size; j++) {
				paths[i][j] = new IntVar(store, "paths[" + i + "," + j + "]");
				costs[i][j] = new IntVar(store, "costs[" + i + "," + j + "]");
				for (int k = 0; k < from.length; k++) {
					if (from[k] == fromNode) {
						paths[i][j].addDom(to[k], to[k]);
						costs[i][j].addDom(cost[k], cost[k]);

						// Constraint: you need to 'pay' the corresponding distance for each path
						IntVar pathVar = new IntVar(store, "pathVar[" + i + "," + j + "," + k + "]", to[k], to[k]);
						IntVar costVar = new IntVar(store, "costVar[" + i + "," + j + "," + k + "]", cost[k], cost[k]);

						PrimitiveConstraint c1 = new XeqY(paths[i][j], pathVar);
						PrimitiveConstraint c2 = new XeqY(costs[i][j], costVar);
						store.impose(new IfThen(c1, c2));
					}
				}
				// You can go back to start from every destination
				paths[i][j].addDom(start, start);
				costs[i][j].addDom(0, 0);

				PrimitiveConstraint c1 = new XeqY(paths[i][j], START);
				PrimitiveConstraint c2 = new XeqY(costs[i][j], ZERO);
				store.impose(new IfThen(c1, c2));

				fromNode++;
			}
		}

		/**
		 * Add constraints
		 */
		// Constraint: make each row a sub-circuit (there must be a path to each destination)
		for (int i = 0; i < n_dests; i++) {
			store.impose(new Subcircuit(paths[i]));
		}

		// Constraint: element
		for (int i = 0; i < n_dests; i++) {
			IntVar costIntVar = new IntVar(store, "costIntVar[" + i + "]", cost[i], cost[i]);
			IntVar iIntVar = new IntVar(store, "iIntVar[" + i + "]", i, i);
			store.impose(new Element(iIntVar, paths[i], costIntVar));
		}

		// Constraint: you can only travel for free if you go back to the start node
		for (int i = 0; i < n_dests; i++) {
			for (int j = 0; j < graph_size; j++) {
				PrimitiveConstraint c1 = new XneqY(paths[i][j], START);
				PrimitiveConstraint c2 = new XneqY(costs[i][j], ZERO);
				store.impose(new IfThen(c1, c2));
			}
		}

		// Constraint: minimize by considering which paths were taken and what their cost was
		IntVar maxDistance = new IntVar(store, "cost", 0, sumArray(cost));

		for (int i = 0; i < n_dests; i++) {
			store.impose(new SumInt(store, costs[i], "==", maxDistance));
		}


		/**
		 * Search & print solution
		 */
		System.out.println("Number of variables: " + store.size() +
				"\nNumber of constraints: " + store.numberConstraints());

		Search<IntVar> search = new DepthFirstSearch<>();
		SelectChoicePoint<IntVar> select = new SimpleMatrixSelect<>(paths, null, new IndomainMin<>());

		System.out.println(store);

		if (search.labeling(store, select, maxDistance)) {
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

	/**
	 * Merges two int arrays into one.
	 */
	private static int[] mergeIntArrays(int[] a, int[] b) {
		int[] result = new int[a.length + b.length];
		for (int i = 0; i < (a.length + b.length); i++) {
			if (i < a.length) {
				result[i] = a[i];
			} else {
				result[i] = b[i - a.length];
			}
		}
		return result;
	}
}
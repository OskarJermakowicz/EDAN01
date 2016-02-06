import org.jacop.core.Store;
import org.jacop.core.IntVar;

import java.util.ArrayList;
import java.util.HashSet;

import org.jacop.constraints.*;
import org.jacop.search.*;

public class Logistics {
	public static void main(String[] args) {
		long startTime = System.currentTimeMillis();

		totalDistance(2);

		long endTime = System.currentTimeMillis();
		System.out.println("\n*** Execution time: " + (endTime - startTime) + " ms");

	}

	private static void calculateDistance(int graph_size, int start, int n_dests, int[] dest, int n_edges, int[] from,
			int[] to, int[] cost) {
		Store store = new Store();

		/**
		 * Initialize the variables
		 */
		final IntVar ZERO = new IntVar(store, "ZERO", 0, 0);
		final IntVar ONE = new IntVar(store, "ONE", 1, 1);

		IntVar[] paths = new IntVar[n_edges];

		for (int i = 0; i < n_edges; i++) {
			paths[i] = new IntVar(store, "path[" + i + "]", 0, 1);
		}
		/**
		 * Add constraints
		 */

		// Must take atleast one path
		store.impose(new SumInt(store, paths, "!=", ZERO));

		// Must go to a destination atleast once
		for (int d : dest) {
			IntVar[] indexes = new IntVar[n_edges];

			for (int i = 0; i < n_edges; i++) {
				if (to[i] == d) {
					indexes[i] = new IntVar(store, "res1." + i, 0, 1);
				} else {
					indexes[i] = ZERO;
				}
			}
			store.impose(new SumInt(store, indexes, "!=", ZERO));

			// TODO: rekursivt, kolla vilken edge den kmr ifr�n och sen k�ra
			// samma sak p� n�sta edges, tills man kommer till start
		}

		// Base search on costs to minimize
		IntVar[] costs = new IntVar[n_edges];
		for (int c : cost) {

		}

		/**
		 * Search & print solution
		 */
		System.out.println(
				"Number of variables: " + store.size() + "\nNumber of constraints: " + store.numberConstraints());
		Search<IntVar> search = new DepthFirstSearch<IntVar>();
		SelectChoicePoint<IntVar> select = new SimpleSelect<IntVar>(paths, null, new IndomainMin<IntVar>());
		search.setSolutionListener(new PrintOutListener<IntVar>());
		if (search.labeling(store, select)) {
			System.out.println("\n*** Yes");
			System.out.println("Solution : " + java.util.Arrays.asList(paths));
		} else {
			System.out.println("\n*** No");
		}
	}

	private static void totalDistance(int n) {
		if (n == 1) {
			System.out.println("\n*** Expected result: 20");
			int graph_size = 6;
			int start = 1;
			int n_dests = 1;
			int[] dest = { 6 };
			int n_edges = 7;
			int[] from = { 1, 1, 2, 2, 3, 4, 4 };
			int[] to = { 2, 3, 3, 4, 5, 5, 6 };
			int[] cost = { 4, 2, 5, 10, 3, 4, 11 };
			calculateDistance(graph_size, start, n_dests, dest, n_edges, from, to, cost);
		} else if (n == 2) {
			System.out.println("\n*** Expected result: 20");
			int graph_size = 6;
			int start = 1;
			int n_dests = 2;
			int[] dest = { 5, 6 };
			int n_edges = 7;
			int[] from = { 1, 1, 2, 2, 3, 4, 4 };
			int[] to = { 2, 3, 3, 4, 5, 5, 6 };
			int[] cost = { 4, 2, 5, 10, 3, 4, 11 };
			calculateDistance(graph_size, start, n_dests, dest, n_edges, from, to, cost);
		} else if (n == 3) {
			System.out.println("\n*** Expected result: 11");
			int graph_size = 6;
			int start = 1;
			int n_dests = 2;
			int[] dest = { 5, 6 };
			int n_edges = 9;
			int[] from = { 1, 1, 1, 2, 2, 3, 3, 3, 4 };
			int[] to = { 2, 3, 4, 3, 5, 4, 5, 6, 6 };
			int[] cost = { 6, 1, 5, 5, 3, 5, 6, 4, 2 };
			calculateDistance(graph_size, start, n_dests, dest, n_edges, from, to, cost);
		} else {
			System.out.println("\n*** Invalid n");
		}
	}
}
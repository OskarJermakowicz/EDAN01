import org.jacop.core.Store;
import org.jacop.core.IntVar;
import org.jacop.constraints.*;
import org.jacop.search.*;

public class Pizza {
	public static void main(String[] args) {
		long startTime = System.currentTimeMillis();

		optimalCost(1);

		long endTime = System.currentTimeMillis();
		System.out.println("\n*** Execution time: " + (endTime - startTime) + " ms");
	}

	/**
	 * @param n
	 *            number of pizzas to order
	 * @param price
	 *            list of n pizza prices
	 * @param m
	 *            number of vouchers
	 * @param buy
	 *            buy[i], when buying i pizzas one can get free[i] pizzas (that
	 *            cost lower than lowest)
	 * @param free
	 */
	@SuppressWarnings("deprecation")
	private static void optimalCost(int n, int[] price, int m, int[] buy, int[] free) {
		/**
		 * Initialize variables
		 */
		Store store = new Store();

		IntVar[] paidPizzas = new IntVar[n];
		IntVar[] freePizzas = new IntVar[n];
		IntVar[][] paidPerVoucher = new IntVar[m][n];
		IntVar[][] freePerVoucher = new IntVar[m][n];

		/**
		 * Populate all vectors and matrices with IntVar's & add initial
		 * constraints
		 */
		// Constraint: a pizza cannot be bought and received for free.
		for (int i = 0; i < n; i++) {
			paidPizzas[i] = new IntVar(store, "paidPizza[" + i + "]", 0, 1);
			freePizzas[i] = new IntVar(store, "freePizza[" + i + "]", 0, 1);

			store.impose(new XneqY(paidPizzas[i], freePizzas[i]));
		}

		for (int i = 0; i < m; i++) {
			for (int j = 0; j < n; j++) {
				paidPerVoucher[i][j] = new IntVar(store, "paidPizza[" + i + "," + j + "]", 0, 1);
				freePerVoucher[i][j] = new IntVar(store, "freePizza[" + i + "," + j + "]", 0, 1);
			}
		}

		/**
		 * Add more constraints
		 */
		// Number of pizzas bought and received for free must be equal to n
		store.impose(new Sum(mergeIntVarVectors(paidPizzas, freePizzas), new IntVar(store, "nbr", n, n)));

		// One pizza cannot be used to activate two vouchers, each column must
		// be 0 or 1 just like in paidPizzas & freePizzas
		for (int i = 0; i < n; i++) {
			int paidSum = sumVector(getColumn(paidPerVoucher, i));
			int freeSum = sumVector(getColumn(freePerVoucher, i));
			IntVar paidSumVar = new IntVar(store, "nbr", paidSum, paidSum);
			IntVar freeSumVar = new IntVar(store, "nbr", freeSum, freeSum);
//			store.impose(new XeqY(paidSumVar, paidPizzas[i]));
//			store.impose(new XeqY(freeSumVar, freePizzas[i]));
		}


	}

	/**
	 * Merges two IntVar vectors into one.
	 */
	private static IntVar[] mergeIntVarVectors(IntVar[] a, IntVar[] b) {
		IntVar[] result = new IntVar[a.length + b.length];
		for (int i = 0; i < (a.length + b.length); i++) {
			if (i < a.length) {
				result[i] = a[i];
			} else {
				result[i] = b[i - a.length];
			}
		}
		return result;
	}

	/**
	 * Gets an IntVar vector of the column at an index in a matrix.
	 */
	private static IntVar[] getColumn(IntVar[][] matrix, int index) {
		IntVar[] column = new IntVar[matrix.length];
		for (int i = 0; i < matrix.length; i++) {
			column[i] = matrix[i][index];
		}
		return column;
	}

	/**
	 * Gets the sum of an IntVar vector
	 */
	private static int sumVector(IntVar[] vector) {
		int sum = 0;
		for (IntVar var : vector) {
			sum += var.value();
		}
		return sum;
	}

	private static void optimalCost(int n) {
		if (n == 1) {
			System.out.println("\n*** Expected result: 35");
			int n = 4;
			int[] price = { 10, 5, 20, 15 };
			int m = 2;
			int[] buy = { 1, 2 };
			int[] free = { 1, 1 };
			optimalCost(n, price, m, buy, free);
		} else if (n == 2) {
			System.out.println("\n*** Expected result: 35");
			int n = 4;
			int[] price = { 10, 15, 20, 15 };
			int m = 7;
			int[] buy = {1,2,2,8,3,1,4};
			int[] free = {1,1,2,9,1,0,1};
			optimalCost(n, price, m, buy, free);
		} else if (n == 3) {
			System.out.println("\n*** Expected result: 340");
			int n = 10;
			int[] price = { 70, 10, 60, 60, 30, 100, 60, 40, 60, 20 };
			int m = 4;
			int[] buy = { 1, 2, 1, 1 };
			int[] free = { 1, 1, 1, 0 };
			optimalCost(n, price, m, buy, free);
		} else {
			System.out.println("\n*** Invalid n");
		}
	}

	
}
import org.jacop.core.Store;
import org.jacop.core.IntVar;
import org.jacop.constraints.*;
import org.jacop.search.*;
import java.util.Arrays;

public class Pizza {
	public static void main(String[] args) {
		long startTime = System.currentTimeMillis();

//		System.out.println("\n*** Expected result: 35");
//		int n = 4;
//		int[] price = { 10, 5, 20, 15 };
//		int m = 2;
//		int[] buy = { 1, 2 };
//		int[] free = { 1, 1 };
//		optimalCost(n, price, m, buy, free);

//		System.out.println("\n*** Expected result: 35");
//		int n = 4;
//		int[] price = { 10, 15, 20, 15 };
//		int m = 7;
//		int[] buy = {1,2,2,8,3,1,4};
//		int[] free = {1,1,2,9,1,0,1};
//		optimalCost(n, price, m, buy, free);

		System.out.println("\n*** Expected result: 340");
		int n = 10;
		int[] price = { 70, 10, 60, 60, 30, 100, 60, 40, 60, 20 };
		int m = 4;
		int[] buy = { 1, 2, 1, 1 };
		int[] free = { 1, 1, 1, 0 };
		optimalCost(n, price, m, buy, free);

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

		final IntVar ZERO = new IntVar(store, "ZERO", 0, 0);
        final IntVar ONE = new IntVar(store, "ONE", 1, 1);

		/**
		 * Populate all arrays and matrices
		 */
		for (int i = 0; i < n; i++) {
			paidPizzas[i] = new IntVar(store, "paidPizza[" + i + "]", 0, 1);
			freePizzas[i] = new IntVar(store, "freePizza[" + i + "]", 0, 1);
		}

		for (int i = 0; i < m; i++) {
			for (int j = 0; j < n; j++) {
				paidPerVoucher[i][j] = new IntVar(store, "paidPizza[" + i + "," + j + "]", 0, 1);
				freePerVoucher[i][j] = new IntVar(store, "freePizza[" + i + "," + j + "]", 0, 1);
			}
		}

		/**
		 * Add constraints
		 */
		// Constraint: number of pizzas bought and received for free must be equal to n
		store.impose(new Sum(mergeIntVarArrays(paidPizzas, freePizzas), new IntVar(store, "nbr", n, n)));

		for (int i = 0; i < n; i++) {
            // Constraint: one pizza cannot be used to activate two vouchers, each column must
            // be 0 or 1 just like in paidPizzas & freePizzas
			store.impose(new Sum(getColumn(paidPerVoucher, i), paidPizzas[i]));
            store.impose(new Sum(getColumn(freePerVoucher, i), freePizzas[i]));

            // Constraint: you cannot get a pizza for free if that pizza was used for the voucher
            store.impose(new XneqY(paidPizzas[i], freePizzas[i]));
		}


        for (int i = 0; i < m; i++) {
			// Constraint: number of free pizzas cannot be more than what the voucher says
            IntVar maxFreePizzas = new IntVar(store, "maxFreePizzas", free[i], free[i]);
            store.impose(new SumInt(store, freePerVoucher[i], "<=", maxFreePizzas));

			// Constraint: you cannot get free pizzas if you don't pay for the amount of pizzas the voucher says
			IntVar mustBuy = new IntVar(store, "mustBuy[" + i + "]", buy[i], buy[i]);
			store.impose(new IfThen(new SumInt(store, paidPerVoucher[i], "<", mustBuy), new SumInt(store, freePerVoucher[i], "==", ZERO)));
        }

		// Constraint: free pizzas cannot be more expensive than the cheapest that was bought
        for(int i = 0; i < m; i++){
            for(int j = 0; j < n; j++){
                PrimitiveConstraint notBoughtCons = new XeqY(paidPerVoucher[i][j], ZERO);
                PrimitiveConstraint boughtCons = new XeqY(paidPerVoucher[i][j], ONE);
                for(int k = j - 1; k >= 0; k--){
                    PrimitiveConstraint notFreeCons = new XeqY(freePerVoucher[i][k], ZERO);
                    PrimitiveConstraint freeCons = new XeqY(freePerVoucher[i][k], ONE);

                    store.impose(new IfThen(notBoughtCons, new Or(notFreeCons, freeCons)));
                    store.impose(new IfThen(boughtCons, notFreeCons));
                }
            }
        }

		// Minimize the solution based on cost
		IntVar cost = new IntVar(store, "Cost ", 0, sumArray(price));
		Arrays.sort(price);
		price = reverseArray(price);
		store.impose(new SumWeight(paidPizzas, price, cost));

		/**
		 * Search & print solution
		 */
		System.out.println("Number of variables: " + store.size() +
				"\nNumber of constraints: " + store.numberConstraints());

		Search<IntVar> search = new DepthFirstSearch<>();
		SelectChoicePoint<IntVar> select = new SimpleMatrixSelect<>(addRows(paidPerVoucher, freePerVoucher), null, new IndomainMin<>());

		if (search.labeling(store, select, cost)) {
			System.out.println("\n*** Found solution.");
		} else {
			System.out.println("\n*** No solution found.");
		}
	}

	/**
	 * Merges two IntVar arrays into one.
	 */
	private static IntVar[] mergeIntVarArrays(IntVar[] a, IntVar[] b) {
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
	 * Swaps the order of an int array
     */
	private static int[] reverseArray(int[] array) {
		for( int i = 0; i < (array.length / 2); ++i )
		{
			int swap = array[i];
			array[i] = array[array.length - i - 1];
			array[array.length - i - 1] = swap;
		}
		return array;
	}

	/**
	 * Adds the rows of matrix2 to the end of matrix1
	 */
	private static IntVar[][] addRows(IntVar[][] matrix1, IntVar[][] matrix2){
		IntVar[][] matrix = new IntVar[matrix1.length * 2][matrix1[0].length];
		for(int i = 0; i < matrix1.length; i++){
			for(int j = 0; j < matrix1[0].length; j++){
				matrix[i][j] = matrix1[i][j];
			}
		}
		for(int i = 0; i < matrix2.length; i++){
			for(int j = 0; j < matrix2[0].length; j++){
				matrix[matrix1.length + i][j] = matrix2[i][j];
			}
		}
		return matrix;
	}
}
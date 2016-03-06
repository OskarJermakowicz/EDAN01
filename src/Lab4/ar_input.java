package Lab4;

public class ar_input {
    /**
     * Default values, for different values ar_input needs to be extended with a subclass
     */
    public static int del_add = 1;
    public static int del_mul = 2;

    public static int number_add = 1;
    public static int number_mul = 1;
    public static int n = 28;

    public static int[] last = {13,14,27,28};
    public static int[] add = {9,10,11,12,13,14,19,20,25,26,27,28};
    public static int[] mul = {1,2,3,4,5,6,7,8,15,16,17,18,21,22,23,24};

    // For example index 9 of dependencies indicates that mul index 0 and 1 must be executed before 9 in add.
    public static int[][] dependencies = {
            {},
            {},
            {},
            {},
            {},
            {},
            {},
            {},
            {0, 1},
            {2, 3},
            {4, 5},
            {6, 7},
            {9},
            {10},
            {12},
            {13},
            {12},
            {13},
            {14, 15},
            {16, 17},
            {18},
            {19},
            {18},
            {19},
            {20, 21},
            {22, 23},
            {24, 8},
            {11, 25}
    };
}
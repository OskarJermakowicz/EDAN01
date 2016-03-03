package Lab4;

public class ar_1_1 extends ar_input {
    public int del_add = 1;
    public int del_mul = 2;

    public int number_add = 1;
    public int number_mul = 1;
    public int n = 28;

    public int[] last = {13,14,27,28};

    public int[] add = {9,10,11,12,13,14,19,20,25,26,27,28};

    public int[] mul = {1,2,3,4,5,6,7,8,15,16,17,18,21,22,23,24};

    public int[][] dependencies = {
            {9}, // Första operationen dvs add(9) kan inte köras förrens mul(1) är klar.
            {9},
            {10},
            {10},
            {11},
            {11},
            {12},
            {12},
            {27},
            {28},
            {13},
            {14},
            {16,17},
            {15,18},
            {19},
            {19},
            {20},
            {20},
            {22,23},
            {21,24},
            {25},
            {25},
            {26},
            {26},
            {27},
            {28},
            {},
            {},
    };
}
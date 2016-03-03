include "globals.mzn";

% Input data
int: n;
int: n_commercial;
int: n_residential;
array[0..n] of int: point_distribution;

% Variables (0 = commercial, 1 = residential)
array[0..n-1,0..n-1] of var 0..1: grid;

array[0..n-1] of var int: rowsCost;
array[0..n-1] of var int: colsCost;
var int: cost;

% Declare symmetries in grid
constraint lex2(grid);

% Constraint: n_residential must be equal to the sum of 1s in grid
constraint 
    sum(i in 0..n-1)
        ( sum(j in 0..n-1)
            ( grid[i,j] ) ) = n_residential;

% Constraint: calculate the points for the rows in grid
constraint forall (i in 0..n-1) (
    rowsCost[i] = point_distribution[sum(row(grid, i))]
);

% Constraint: calculate the points for the columns in grid
constraint forall (i in 0..n-1) (
    colsCost[i] = point_distribution[sum(col(grid, i))]
);

% Solve
constraint cost = sum(rowsCost) + sum(colsCost);
solve :: int_search([grid[i,j] | i, j in 0..n-1], occurrence, indomain_max, complete) maximize(cost);
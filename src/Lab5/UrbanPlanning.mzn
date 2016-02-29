include "globals.mzn";
include "lex_lesseq.mzn";

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
% Based on http://people.eng.unimelb.edu.au/pstuckey/minisym/paper.pdf page 6.

% Rows need to be in the same lexicographical order
predicate row_var_seq_sym(array[int,int] of var int: x) =
    let { int: l1 = min(index_set_1of2(x)),
          int: u1 = max(index_set_1of2(x)),
          int: l2 = min(index_set_2of2(x)),
          int: u2 = max(index_set_2of2(x)) }
    in forall (i in l1..u1-1) ( 
          lex_lesseq([x[i,j] | j in l2..u2],[x[i+1,j] | j in l2..u2])
    );

% Columns need to be in the same lexicographical order
predicate col_var_seq_sym(array[int,int] of var int: x) =
    let { int: l1 = min(index_set_1of2(x)),
          int: u1 = max(index_set_1of2(x)),
          int: l2 = min(index_set_2of2(x)),
          int: u2 = max(index_set_2of2(x)) } 
    in forall (i in l2..u2-1) (
          lex_lesseq([x[i,j] | j in l1..u1],[x[j,i+1] | j in l1..u1])
    );

constraint row_var_seq_sym(grid);
constraint col_var_seq_sym(grid);

% Constraint: n_residential must be equal to the sum of 1s in grid
constraint 
    sum(i in 0..n-1)
        ( sum(j in 0..n-1)
            ( grid[i,j] ) ) = n_residential;

% Constraint: calculate the points for the rows in grid
constraint forall (i in 0..n-1) (
    let { var int: rowSum = sum(row(grid, i)) }
    in rowsCost[i] = point_distribution[rowSum]
);

% Constraint: calculate the points for the columns in grid
constraint forall (i in 0..n-1) (
    let { var int: colSum = sum(col(grid, i)) }
    in colsCost[i] = point_distribution[colSum]
);

% Solve
constraint cost = sum(rowsCost) + sum(colsCost);
solve maximize(cost);
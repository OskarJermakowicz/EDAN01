int: n;
int: n_commercial;
int: n_residential;
array[0..n] of int: point_distribution;

array[1..n] of int: rows;
array[1..n] of int: cols;
array[rows,cols] of var int: grid;

var int: cost;

constraint cost = /* something */;

solve maximize(cost);
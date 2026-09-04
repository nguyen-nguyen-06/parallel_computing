# Parallel Computing on CPU

My exploration of parallelism on CPU through sorting and matrix multiplication. I built the programs from scratch using Java Fork/Join framework with divide-and-conquer algorithms for scaling with multiple processors. Benchmarked on local hardware (8-core, 16-thread) and AWS EC2 (64 vCPUs) to measure the speedup.

## Table of Contents

- [Algorithms](#algorithms)
  - [Parallel Sorting](#parallel-sorting)
  - [Parallel Matrix Multiplication](#parallel-matrix-multiplication)
- [Results](#results)
- [Structure](#structure)
- [How to Try It Yourself](#how-to-try-it-yourself)
  - [Compile](#compile)
  - [Run Interactive Client](#run-interactive-client)
  - [Run Benchmarks](#run-benchmarks)
  - [Clean Up Results](#clean-up-results)
- [How To Use In Your Code](#how-to-use-in-your-code)
  - [Sorting](#sorting)
  - [Matrix Multiplication](#matrix-multiplication)
  - [Compiling and Running from Command Line](#compiling-and-running-from-command-line)

## Algorithms

### Parallel Sorting

| Algorithm | Approach | Work | Span |
|---|---|---|---|
| **Parallel Merge Sort** | Parallel divide + parallel merge using median-based binary search splitting | O(n log n) | O(log^3 n) |
| **Parallel Quick Sort** | Fully parallel partitioning using map, prefix sum, and filter | O(n log n) | O(log^2 n) |
| **Parallel Quick Sort Simple** | Sequential in-place partition, parallel recursion on subarrays | O(n log n) | O(n) |

### Parallel Matrix Multiplication

| Approach | Details |
|---|---|
| **Quadrant decomposition** | Recursively splits result matrix into 4 quadrants, forks each as a parallel task |
| **Dot product** | Computed sequentially per cell (parallelizing individual dot products adds more overhead than benefit) |

## Results

### Sorting — AWS EC2 c7a.16xlarge (64 vCPUs)

| Array Size | Arrays.sort | Par Quick | Par Quick Simple | Par Merge |
|---|---|---|---|---|
| 100K | 4.86 ms | 6.98 ms | 1.72 ms | **0.97 ms** |
| 500K | 28.96 ms | 19.21 ms | 5.85 ms | **3.12 ms** |
| 1M | 59.94 ms | 14.13 ms | 9.44 ms | **3.66 ms** |
| 5M | 335.77 ms | 93.67 ms | 48.33 ms | **12.00 ms** |
| 20M | 1,433.80 ms | 205.87 ms | 181.84 ms | **50.86 ms** |

**Best result: Parallel Merge Sort — 54x speedup** over sequential, **28x faster** than Arrays.sort() on 20M elements.

### Matrix Multiplication — AWS EC2 c7a.16xlarge (64 vCPUs)

| Size | Sequential | Parallel | Speedup |
|---|---|---|---|
| 2000x2000 | 37,395 ms | 2,278 ms | **16.4x** |

## Structure

```
parallel_computing/
├── parallel_sorting/
│   ├── Sorting.java               # Base class
│   ├── Sequential_quick.java      # Sequential quicksort
│   ├── Sequential_merge.java      # Sequential merge sort
│   ├── Parallel_quick.java        # Parallel quicksort with prefix sum partitioning
│   ├── Parallel_quick_simple.java # Parallel quicksort with sequential partition
│   ├── Parallel_merge.java        # Parallel merge sort
│   ├── BenchMark.java             # Sorting benchmark
│   └── Testing.java               # Tests
├── parallel_matrix_mul/
│   ├── Matrix_mul.java            # Base class
│   ├── Sequential_matrix.java     # Sequential multiplication
│   ├── Parallel_matrix.java       # Parallel multiplication
│   ├── BenchMark.java             # Matrix benchmark
│   └── Testing.java               # Tests
├── Client.java                    # Interactive CLI client
└── Makefile
```

## How to Try It Yourself

### Java

This project requires **JDK 11 or later** (any recent JDK works).

You also need `make` (GNU make) to use the commands below.

### Compile

```bash
make compile
```

### Run Interactive Client

```bash
make run
```
This command runs a program that accept your file name as a parameter and executes the content inside the given file.

It will let you choose sorting or matrix multiplication, input via file, then runs with sequential vs parallel and prints the comparison.

### Run Benchmarks 

```bash
make benchmark-sorting
make benchmark-matrix
make benchmark          # run both
```
This command will run the BenchMark.java in each folder with random input and different sizes to measure the speed up between parallel code and sequential code.

Results are saved to `sorting_benchmark.csv` and `matrix_benchmark.csv`.

### Clean Up 

```bash
make clean
```

## How To Use In Your Code

If you're using an IDE like VS Code with Java installed (Java Extension Pack) or IntelliJ, import the classes and use them like any normal Java class. You can run your code with the run button as usual.

#### Sorting

All sorting classes extend `Sorting` and share the same interface.

Available parallel sorting classes: Parallel_merge, Parallel_quick, Parallel_quick_simple. 

Note: Based on your array size and number of processors, you may need to tune the CUTOFF. The CUTOFF can be set to any value but the default is shown in the examples below.

```java
import parallel_computing.parallel_sorting.*;

// This part on how to set CUTOFF values is optional.  
Parallel_merge.CUTOFFMERGE = 4000;          // Here is the default value
Parallel_quick.CUTOFFPARTITION = 10000;
Parallel_quick.CUTOFFQUICK = 4000;
//

// How to sort an array
int[] data = {5, 3, 8, 1, 9, 2, 7};

// All three return a new sorted array {1, 2, 3, 5, 7, 8, 9}
int[] result1 = new Parallel_merge(data).sort();        //fastest
int[] result2 = new Parallel_quick(data).sort();
int[] result3 = new Parallel_quick_simple(data).sort();


// How to sort an array from a file
// File format: <size> <val1> <val2> ... <valN>
// Example file contents: 5 3 1 4 1 5
// Return: sorted array {1, 1, 3, 4, 5}
Parallel_merge sorter = new Parallel_merge("input.txt");
int[] sorted = sorter.sort();
```

#### Matrix Multiplication

All matrix classes extend `Matrix_mul` and share the same interface.

Available parallel matrix classes: Parallel_matrix

```java
import parallel_computing.parallel_matrix_mul.*;

// Similar to Sorting classes, this part on how to set CUTOFF value is optional
Parallel_matrix.MATRIX_CUTOFF = 128;    // Default value
Parallel_matrix.DOT_CUTOFF = 10000;     

// From arrays
double[][] matA = {{1, 2}, {3, 4}};
double[][] matB = {{5, 6}, {7, 8}};

double[][] result = new Parallel_matrix(matA, matB).multiply();

// From a file (format: <rowA> <colA> <values...> <rowB> <colB> <values...>)
// Example file contents: 2 2 1.0 2.0 3.0 4.0 2 2 5.0 6.0 7.0 8.0
Parallel_matrix mul = new Parallel_matrix("input.txt");
double[][] result = mul.multiply();

// Write result to file
mul.multiplyPrintFile("output.txt");
```

#### Compiling and Running from Command Line

Ignore this if you run on IntelliJ or VS Code (with Java Extension Pack).

```bash
# 1. Compile the library
make compile    # saves .class files to out/ folder
                # same level as parallel_matrix_mul and parallel_sorting folders

# 2. Compile your code and save to the same folder
javac -d out -cp out YourProgram.java

# 3. Run
java -cp out YourProgram
```

See the Makefile for more information.
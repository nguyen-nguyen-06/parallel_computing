# Parallel Computing on CPU

Parallel implementations of sorting and matrix multiplication algorithms in Java using the Fork/Join framework. Benchmarked on local hardware (8-core, 16-thread) and AWS EC2 (64 vCPUs).

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
    ├── Matrix_mul.java            # Base class
    ├── Sequential_matrix.java     # Sequential multiplication
    ├── Parallel_matrix.java       # Parallel multiplication
    ├── BenchMark.java             # Matrix benchmark
    └── Testing.java               # Tests
```

## How to Run

### Compile

```bash
mkdir -p out
javac -d out parallel_sorting/Sorting.java parallel_sorting/Sequential_quick.java parallel_sorting/Sequential_merge.java parallel_sorting/Parallel_quick.java parallel_sorting/Parallel_quick_simple.java parallel_sorting/Parallel_merge.java parallel_sorting/BenchMark.java
javac -d out parallel_matrix_mul/Matrix_mul.java parallel_matrix_mul/Sequential_matrix.java parallel_matrix_mul/Parallel_matrix.java parallel_matrix_mul/BenchMark.java
```

### Run Benchmarks

```bash
java -cp out parallel_computing.parallel_sorting.BenchMark
java -cp out parallel_computing.parallel_matrix_mul.BenchMark
```

Results are saved to `sorting_benchmark.csv` and `matrix_benchmark.csv`.


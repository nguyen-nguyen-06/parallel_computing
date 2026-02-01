package parallel_computing.parallel_matrix_mul;

import java.util.*;
import java.io.*;

/**
 * Benchmark class for comparing sequential and parallel matrix multiplication.
 * Generates CSV data for performance analysis.
 */
public class BenchMark {

    private static final int WARMUP_ITERATIONS = 2;
    private static final int TEST_ITERATIONS = 3;
    private static final int SEED = 50;

    private static final int[] SIZES = {
        100, 200, 300, 400, 500, 600, 700, 800, 900, 1000,
        1200, 1400, 1600, 1800, 2000, 2500, 3000, 4000, 5000
    };

    public static void main(String[] args) {
        setUp();

        System.out.println("Matrix Multiplication Benchmark");
        System.out.println("Available processors: " + Runtime.getRuntime().availableProcessors());
        System.out.println("========================================\n");

        generateFullComparisonCSV();

        System.out.println("\n=== Benchmark completed ===");
    }

    private static void setUp() {
        Parallel_matrix.MATRIX_CUTOFF = 128;
        Parallel_matrix.DOT_CUTOFF = 256;
    }

    private static double[][] generateRandomMatrix(int rows, int cols) {
        Random rand = new Random(SEED);
        double[][] matrix = new double[rows][cols];
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                matrix[i][j] = rand.nextDouble() * 10;
            }
        }
        return matrix;
    }

    private static double measureMultiplyTime(double[][] matA, double[][] matB, String multiplierType) {
        // Warmup
        for (int i = 0; i < WARMUP_ITERATIONS; i++) {
            runMultiply(matA, matB, multiplierType);
        }

        // Actual measurement
        long totalTime = 0;
        for (int i = 0; i < TEST_ITERATIONS; i++) {
            long start = System.nanoTime();
            runMultiply(matA, matB, multiplierType);
            long end = System.nanoTime();
            totalTime += (end - start);
        }

        return (totalTime / TEST_ITERATIONS) / 1_000_000.0;
    }

    private static void runMultiply(double[][] matA, double[][] matB, String multiplierType) {
        if (multiplierType.equals("Sequential")) {
            new Sequential_matrix(matA, matB).multiply();
        } else if (multiplierType.equals("Parallel")) {
            new Parallel_matrix(matA, matB).multiply();
        }
    }

    private static void generateFullComparisonCSV() {
        System.out.println("Generating matrix_benchmark.csv...");

        try {
            PrintWriter writer = new PrintWriter(new File("matrix_benchmark.csv"));
            writer.println("n,Sequential,Parallel,Speedup");

            for (int size : SIZES) {
                System.out.println("  Testing size: " + size + "x" + size);
                
                double[][] matA = generateRandomMatrix(size, size);
                double[][] matB = generateRandomMatrix(size, size);

                double seqTime = measureMultiplyTime(matA, matB, "Sequential");
                double parTime = measureMultiplyTime(matA, matB, "Parallel");
                double speedup = seqTime / parTime;

                writer.printf("%d,%.3f,%.3f,%.3f%n", size, seqTime, parTime, speedup);
            }

            writer.close();
            System.out.println("\n  -> matrix_benchmark.csv created");

        } catch (IOException e) {
            System.out.println("Error creating CSV: " + e.getMessage());
        }
    }
}

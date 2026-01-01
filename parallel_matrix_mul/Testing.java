package parallel_computing.parallel_matrix_mul;

import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

import java.util.Random;

public class Testing {
    private static final double EPSILON = 1e-9;
    private static Random rand;

    @BeforeAll
    public static void setup() {
        Parallel_matrix.MATRIX_CUTOFF = 128;
        Parallel_matrix.DOT_CUTOFF = 256;
        rand = new Random(42);
    }

    @Test
    public void testCorrectness_SmallMatrix() {
        double[][] matA = generateRandomMatrix(10, 10);
        double[][] matB = generateRandomMatrix(10, 10);

        Sequential_matrix sequential = new Sequential_matrix(matA, matB);
        Parallel_matrix parallel = new Parallel_matrix(matA, matB);

        double[][] sequentialResult = sequential.multiply();
        double[][] parallelResult = parallel.multiply();

        assertMatricesEqual(sequentialResult, parallelResult);
    }

    @Test
    public void testCorrectness_MediumMatrix() {
        double[][] matA = generateRandomMatrix(100, 100);
        double[][] matB = generateRandomMatrix(100, 100);

        Sequential_matrix sequential = new Sequential_matrix(matA, matB);
        Parallel_matrix parallel = new Parallel_matrix(matA, matB);

        double[][] sequentialResult = sequential.multiply();
        double[][] parallelResult = parallel.multiply();

        assertMatricesEqual(sequentialResult, parallelResult);
    }

    @Test
    public void testCorrectness_LargeMatrix() {
        double[][] matA = generateRandomMatrix(1000, 1000);
        double[][] matB = generateRandomMatrix(1000, 1000);

        Sequential_matrix sequential = new Sequential_matrix(matA, matB);
        Parallel_matrix parallel = new Parallel_matrix(matA, matB);

        double[][] sequentialResult = sequential.multiply();
        double[][] parallelResult = parallel.multiply();

        assertMatricesEqual(sequentialResult, parallelResult);
    }



    @Test
    public void testSpeedup_100x100() {
        double[][] matA = generateRandomMatrix(100, 100);
        double[][] matB = generateRandomMatrix(100, 100);

        long sequentialTime = measureSequentialTime(matA, matB);
        long parallelTime = measureParallelTime(matA, matB);

        double speedup = (double) sequentialTime / parallelTime;
        System.out.print("Number of threads: " + Runtime.getRuntime().availableProcessors() + "- ");
        System.out.println("100x100 - Sequential: " + sequentialTime + "ms, Parallel: " + parallelTime + 
                            "ms, Speedup: " + String.format("%.2f", speedup) + "x");
    }

    @Test
    public void testSpeedup_500x500() {
        double[][] matA = generateRandomMatrix(500, 500);
        double[][] matB = generateRandomMatrix(500, 500);

        long sequentialTime = measureSequentialTime(matA, matB);
        long parallelTime = measureParallelTime(matA, matB);

        double speedup = (double) sequentialTime / parallelTime;
        System.out.print("Number of threads: " + Runtime.getRuntime().availableProcessors() + "- ");
        System.out.println("500x500 - Sequential: " + sequentialTime + "ms, Parallel: " + parallelTime + 
                            "ms, Speedup: " + String.format("%.2f", speedup) + "x");
    }

    @Test
    public void testSpeedup_1000x1000() {
        double[][] matA = generateRandomMatrix(1000, 1000);
        double[][] matB = generateRandomMatrix(1000, 1000);

        long sequentialTime = measureSequentialTime(matA, matB);
        long parallelTime = measureParallelTime(matA, matB);

        double speedup = (double) sequentialTime / parallelTime;
        System.out.print("Number of threads: " + Runtime.getRuntime().availableProcessors() + "- ");
        System.out.println("1000x1000 - Sequential: " + sequentialTime + "ms, Parallel: " + parallelTime + 
                            "ms, Speedup: " + String.format("%.2f", speedup) + "x");
    }

    @Test
    public void testSpeedup_2000x2000() {
        double[][] matA = generateRandomMatrix(2000, 2000);
        double[][] matB = generateRandomMatrix(2000, 2000);

        long sequentialTime = measureSequentialTime(matA, matB);
        long parallelTime = measureParallelTime(matA, matB);

        double speedup = (double) sequentialTime / parallelTime;
        System.out.print("Number of threads: " + Runtime.getRuntime().availableProcessors() + "- ");
        System.out.println("Matrix size: 2000x2000 - Sequential: " + sequentialTime + "ms, Parallel: " + parallelTime + 
                            "ms, Speedup: " + String.format("%.2f", speedup) + "x");
    }

    @Test
    public void testSpeedup_3000x3000() {
        double[][] matA = generateRandomMatrix(3000, 3000);
        double[][] matB = generateRandomMatrix(3000, 3000);

        long sequentialTime = measureSequentialTime(matA, matB);
        long parallelTime = measureParallelTime(matA, matB);

        double speedup = (double) sequentialTime / parallelTime;
        System.out.print("Number of threads: " + Runtime.getRuntime().availableProcessors() + "- ");
        System.out.println("3000x3000 - Sequential: " + sequentialTime + "ms, Parallel: " + parallelTime + 
                            "ms, Speedup: " + String.format("%.2f", speedup) + "x");
    }

    @Test
    public void testSpeedup_5000x5000() {
        double[][] matA = generateRandomMatrix(5000, 5000);
        double[][] matB = generateRandomMatrix(5000, 5000);

        long sequentialTime = measureSequentialTime(matA, matB);
        long parallelTime = measureParallelTime(matA, matB);

        double speedup = (double) sequentialTime / parallelTime;
        System.out.print("Number of threads: " + Runtime.getRuntime().availableProcessors() + "- ");
        System.out.println("5000x5000 - Sequential: " + sequentialTime + "ms, Parallel: " + parallelTime + 
                            "ms, Speedup: " + String.format("%.2f", speedup) + "x");
    }

    @Test
    public void testSpeedup_10000x10000() {
        double[][] matA = generateRandomMatrix(10000, 10000);
        double[][] matB = generateRandomMatrix(10000, 10000);

        long sequentialTime = measureSequentialTime(matA, matB);
        long parallelTime = measureParallelTime(matA, matB);

        double speedup = (double) sequentialTime / parallelTime;
        System.out.print("Number of threads: " + Runtime.getRuntime().availableProcessors() + "- ");
        System.out.println("10000x10000 - Sequential: " + sequentialTime + "ms, Parallel: " + parallelTime + 
                            "ms, Speedup: " + String.format("%.2f", speedup) + "x");
    }


    private long measureSequentialTime(double[][] matA, double[][] matB) {
        Sequential_matrix sequential = new Sequential_matrix(matA, matB);
        long start = System.currentTimeMillis();
        sequential.multiply();
        return System.currentTimeMillis() - start;
    }

    private long measureParallelTime(double[][] matA, double[][] matB) {
        Parallel_matrix parallel = new Parallel_matrix(matA, matB);
        long start = System.currentTimeMillis();
        parallel.multiply();
        return System.currentTimeMillis() - start;
    }

    private double[][] generateRandomMatrix(int rows, int cols) {
        double[][] matrix = new double[rows][cols];
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                matrix[i][j] = rand.nextDouble() * 10;
            }
        }
        return matrix;
    }

    private void assertMatricesEqual(double[][] expected, double[][] actual) {
        assertEquals(expected.length, actual.length, "Row count mismatch");
        assertEquals(expected[0].length, actual[0].length, "Column count mismatch");

        for (int i = 0; i < expected.length; i++) {
            for (int j = 0; j < expected[0].length; j++) {
                assertEquals(expected[i][j], actual[i][j], EPSILON,
                    "Mismatch at [" + i + "][" + j + "]");
            }
        }
    }
}

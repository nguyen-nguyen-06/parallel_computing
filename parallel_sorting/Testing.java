package parallel_computing.parallel_sorting;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.Random;

public class Testing {

    private static final int SMALL_SIZE = 100;
    private static final int MEDIUM_SIZE = 10_000;
    private static final int LARGE_SIZE = 1_000_000;
    private static final int SPEEDUP_TEST_SIZE = 5_000_000;
    private static final int WARMUP_ITERATIONS = 3;
    private static final int TEST_ITERATIONS = 5;

    @Before
    public void setUp() {
        // Set cutoff values for parallel quicksort
        Parallel_quick.CUTOFF = 500;
        Parallel_quick.CUTOFFQUICK = 100;
    }

    // ==================== CORRECTNESS TESTS ====================

    @Test
    public void testSequentialQuickEmptyArray() {
        int[] arr = {};
        Sequential_quick sorter = new Sequential_quick(arr.clone());
        int[] result = sorter.sort();
        assertArrayEquals(new int[]{}, result);
    }

    @Test
    public void testSequentialQuickSingleElement() {
        int[] arr = {42};
        Sequential_quick sorter = new Sequential_quick(arr.clone());
        int[] result = sorter.sort();
        assertArrayEquals(new int[]{42}, result);
    }

    @Test
    public void testSequentialQuickTwoElements() {
        int[] arr = {5, 2};
        Sequential_quick sorter = new Sequential_quick(arr.clone());
        int[] result = sorter.sort();
        assertArrayEquals(new int[]{2, 5}, result);
    }

    @Test
    public void testSequentialQuickAlreadySorted() {
        int[] arr = {1, 2, 3, 4, 5, 6, 7, 8, 9, 10};
        Sequential_quick sorter = new Sequential_quick(arr.clone());
        int[] result = sorter.sort();
        assertArrayEquals(new int[]{1, 2, 3, 4, 5, 6, 7, 8, 9, 10}, result);
    }

    @Test
    public void testSequentialQuickReverseSorted() {
        int[] arr = {10, 9, 8, 7, 6, 5, 4, 3, 2, 1};
        Sequential_quick sorter = new Sequential_quick(arr.clone());
        int[] result = sorter.sort();
        assertArrayEquals(new int[]{1, 2, 3, 4, 5, 6, 7, 8, 9, 10}, result);
    }

    @Test
    public void testSequentialQuickWithDuplicates() {
        int[] arr = {3, 1, 4, 1, 5, 9, 2, 6, 5, 3, 5};
        Sequential_quick sorter = new Sequential_quick(arr.clone());
        int[] result = sorter.sort();
        assertArrayEquals(new int[]{1, 1, 2, 3, 3, 4, 5, 5, 5, 6, 9}, result);
    }

    @Test
    public void testSequentialQuickAllSameElements() {
        int[] arr = {7, 7, 7, 7, 7};
        Sequential_quick sorter = new Sequential_quick(arr.clone());
        int[] result = sorter.sort();
        assertArrayEquals(new int[]{7, 7, 7, 7, 7}, result);
    }

    @Test
    public void testSequentialQuickNegativeNumbers() {
        int[] arr = {-5, 3, -1, 0, -8, 2};
        Sequential_quick sorter = new Sequential_quick(arr.clone());
        int[] result = sorter.sort();
        assertArrayEquals(new int[]{-8, -5, -1, 0, 2, 3}, result);
    }

    @Test
    public void testSequentialQuickRandomSmall() {
        int[] arr = generateRandomArray(SMALL_SIZE);
        int[] expected = arr.clone();
        Arrays.sort(expected);

        Sequential_quick sorter = new Sequential_quick(arr.clone());
        int[] result = sorter.sort();

        assertArrayEquals(expected, result);
    }

    @Test
    public void testSequentialQuickRandomMedium() {
        int[] arr = generateRandomArray(MEDIUM_SIZE);
        int[] expected = arr.clone();
        Arrays.sort(expected);

        Sequential_quick sorter = new Sequential_quick(arr.clone());
        int[] result = sorter.sort();

        assertArrayEquals(expected, result);
    }

    // ==================== PARALLEL QUICK CORRECTNESS TESTS ====================

    @Test
    public void testParallelQuickEmptyArray() {
        int[] arr = {};
        Parallel_quick sorter = new Parallel_quick(arr.clone());
        int[] result = sorter.sort();
        assertArrayEquals(new int[]{}, result);
    }

    @Test
    public void testParallelQuickSingleElement() {
        int[] arr = {42};
        Parallel_quick sorter = new Parallel_quick(arr.clone());
        int[] result = sorter.sort();
        assertArrayEquals(new int[]{42}, result);
    }

    @Test
    public void testParallelQuickTwoElements() {
        int[] arr = {5, 2};
        Parallel_quick sorter = new Parallel_quick(arr.clone());
        int[] result = sorter.sort();
        assertArrayEquals(new int[]{2, 5}, result);
    }

    @Test
    public void testParallelQuickAlreadySorted() {
        int[] arr = {1, 2, 3, 4, 5, 6, 7, 8, 9, 10};
        Parallel_quick sorter = new Parallel_quick(arr.clone());
        int[] result = sorter.sort();
        assertArrayEquals(new int[]{1, 2, 3, 4, 5, 6, 7, 8, 9, 10}, result);
    }

    @Test
    public void testParallelQuickReverseSorted() {
        int[] arr = {10, 9, 8, 7, 6, 5, 4, 3, 2, 1};
        Parallel_quick sorter = new Parallel_quick(arr.clone());
        int[] result = sorter.sort();
        assertArrayEquals(new int[]{1, 2, 3, 4, 5, 6, 7, 8, 9, 10}, result);
    }

    @Test
    public void testParallelQuickWithDuplicates() {
        int[] arr = {3, 1, 4, 1, 5, 9, 2, 6, 5, 3, 5};
        Parallel_quick sorter = new Parallel_quick(arr.clone());
        int[] result = sorter.sort();
        assertArrayEquals(new int[]{1, 1, 2, 3, 3, 4, 5, 5, 5, 6, 9}, result);
    }

    @Test
    public void testParallelQuickAllSameElements() {
        int[] arr = {7, 7, 7, 7, 7};
        Parallel_quick sorter = new Parallel_quick(arr.clone());
        int[] result = sorter.sort();
        assertArrayEquals(new int[]{7, 7, 7, 7, 7}, result);
    }

    @Test
    public void testParallelQuickNegativeNumbers() {
        int[] arr = {-5, 3, -1, 0, -8, 2};
        Parallel_quick sorter = new Parallel_quick(arr.clone());
        int[] result = sorter.sort();
        assertArrayEquals(new int[]{-8, -5, -1, 0, 2, 3}, result);
    }

    @Test
    public void testParallelQuickRandomSmall() {
        int[] arr = generateRandomArray(SMALL_SIZE);
        int[] expected = arr.clone();
        Arrays.sort(expected);

        Parallel_quick sorter = new Parallel_quick(arr.clone());
        int[] result = sorter.sort();

        assertArrayEquals(expected, result);
    }

    @Test
    public void testParallelQuickRandomMedium() {
        int[] arr = generateRandomArray(MEDIUM_SIZE);
        int[] expected = arr.clone();
        Arrays.sort(expected);

        Parallel_quick sorter = new Parallel_quick(arr.clone());
        int[] result = sorter.sort();

        assertArrayEquals(expected, result);
    }

    @Test
    public void testParallelQuickRandomLarge() {
        int[] arr = generateRandomArray(LARGE_SIZE);
        int[] expected = arr.clone();
        Arrays.sort(expected);

        Parallel_quick sorter = new Parallel_quick(arr.clone());
        int[] result = sorter.sort();

        assertArrayEquals(expected, result);
    }

    // ==================== EQUIVALENCE TESTS ====================

    @Test
    public void testSequentialAndParallelProduceSameResult() {
        int[] arr = generateRandomArray(MEDIUM_SIZE);

        Sequential_quick sequentialSorter = new Sequential_quick(arr.clone());
        Parallel_quick parallelSorter = new Parallel_quick(arr.clone());

        int[] sequentialResult = sequentialSorter.sort();
        int[] parallelResult = parallelSorter.sort();

        assertArrayEquals(sequentialResult, parallelResult);
    }

    // ==================== SPEEDUP TESTS ====================

    @Test
    public void testSpeedupSmallArray() {
        System.out.println("\n=== Speedup Test: Small Array (" + SMALL_SIZE + " elements) ===");
        measureSpeedup(SMALL_SIZE);
    }

    @Test
    public void testSpeedupMediumArray() {
        System.out.println("\n=== Speedup Test: Medium Array (" + MEDIUM_SIZE + " elements) ===");
        measureSpeedup(MEDIUM_SIZE);
    }

    @Test
    public void testSpeedupLargeArray() {
        System.out.println("\n=== Speedup Test: Large Array (" + LARGE_SIZE + " elements) ===");
        measureSpeedup(LARGE_SIZE);
    }

    @Test
    public void testSpeedupVeryLargeArray() {
        System.out.println("\n=== Speedup Test: Very Large Array (" + SPEEDUP_TEST_SIZE + " elements) ===");
        measureSpeedup(SPEEDUP_TEST_SIZE);
    }

    // ==================== HELPER METHODS ====================

    private int[] generateRandomArray(int size) {
        Random rand = new Random(42); // Fixed seed for reproducibility
        int[] arr = new int[size];
        for (int i = 0; i < size; i++) {
            arr[i] = rand.nextInt(1_000_000) - 500_000; // Range: -500000 to 499999
        }
        return arr;
    }

    private void measureSpeedup(int size) {
        int[] baseArray = generateRandomArray(size);

        // Warmup runs
        System.out.println("Warming up...");
        for (int i = 0; i < WARMUP_ITERATIONS; i++) {
            new Sequential_quick(baseArray.clone()).sort();
            new Parallel_quick(baseArray.clone()).sort();
        }

        // Measure Sequential Quick Sort
        long sequentialTotalTime = 0;
        for (int i = 0; i < TEST_ITERATIONS; i++) {
            int[] arr = baseArray.clone();
            long start = System.nanoTime();
            new Sequential_quick(arr).sort();
            long end = System.nanoTime();
            sequentialTotalTime += (end - start);
        }
        double sequentialAvgMs = (sequentialTotalTime / TEST_ITERATIONS) / 1_000_000.0;

        // Measure Parallel Quick Sort
        long parallelTotalTime = 0;
        for (int i = 0; i < TEST_ITERATIONS; i++) {
            int[] arr = baseArray.clone();
            long start = System.nanoTime();
            new Parallel_quick(arr).sort();
            long end = System.nanoTime();
            parallelTotalTime += (end - start);
        }
        double parallelAvgMs = (parallelTotalTime / TEST_ITERATIONS) / 1_000_000.0;

        // Calculate speedup
        double speedup = sequentialAvgMs / parallelAvgMs;

        // Print results
        System.out.println("Array size: " + size);
        System.out.println("Sequential Quick Sort avg time:    " + String.format("%.3f", sequentialAvgMs) + " ms");
        System.out.println("Parallel Quick Sort avg time: " + String.format("%.3f", parallelAvgMs) + " ms");
        System.out.println("Speedup: " + String.format("%.2f", speedup) + "x");
        System.out.println("Available processors: " + Runtime.getRuntime().availableProcessors());

        // Verify correctness
        int[] expected = baseArray.clone();
        Arrays.sort(expected);

        int[] sequentialResult = new Sequential_quick(baseArray.clone()).sort();
        int[] parallelResult = new Parallel_quick(baseArray.clone()).sort();

        assertArrayEquals("Sequential sort produced incorrect result", expected, sequentialResult);
        assertArrayEquals("Parallel sort produced incorrect result", expected, parallelResult);
    }

    // ==================== STRESS TESTS ====================

    @Test
    public void testMultipleRandomArrays() {
        Random rand = new Random();
        for (int trial = 0; trial < 10; trial++) {
            int size = rand.nextInt(10000) + 100;
            int[] arr = new int[size];
            for (int i = 0; i < size; i++) {
                arr[i] = rand.nextInt();
            }

            int[] expected = arr.clone();
            Arrays.sort(expected);

            int[] sequentialResult = new Sequential_quick(arr.clone()).sort();
            int[] parallelResult = new Parallel_quick(arr.clone()).sort();

            assertArrayEquals("Trial " + trial + " failed for Sequential sort", expected, sequentialResult);
            assertArrayEquals("Trial " + trial + " failed for Parallel sort", expected, parallelResult);
        }
    }

    // Main method for running outside JUnit
    public static void main(String[] args) {
        Testing test = new Testing();
        test.setUp();

        System.out.println("Running Quicksort Tests...\n");

        // Run speedup tests
        test.testSpeedupSmallArray();
        test.testSpeedupMediumArray();
        test.testSpeedupLargeArray();
        test.testSpeedupVeryLargeArray();

        System.out.println("\n=== All tests completed ===");
    }
}

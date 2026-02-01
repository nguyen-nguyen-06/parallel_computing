package parallel_computing.parallel_sorting;

import java.util.*;
import java.io.*;

/**
 * Benchmark class for comparing all sorting algorithms.
 * Generates CSV data for performance analysis.
 */
public class BenchMark {

    private static final int WARMUP_ITERATIONS = 3;
    private static final int TEST_ITERATIONS = 5;
    private static final int SEED = 50;

    // Array sizes
    private static final int[] SIZES = {
        10_000, 20_000, 50_000, 100_000, 200_000, 500_000, 750_000,
        1_000_000, 1_250_000, 1_500_000, 2_000_000, 2_500_000, 3_000_000, 3_500_000,
        4_000_000, 4_500_000, 5_000_000, 20_000_000
    };

    public static void main(String[] args) {
        setUp();

        System.out.println("Sorting Algorithms Benchmark");
        System.out.println("Available processors: " + Runtime.getRuntime().availableProcessors());
        System.out.println("========================================\n");

        generateFullComparisonCSV();

        System.out.println("\n=== Benchmark completed ===");
    }

    private static void setUp() {
        Parallel_quick.CUTOFFPARTITION = 1000;
        Parallel_quick.CUTOFFQUICK = 250;
        Parallel_merge.CUTOFFMERGE = 250;
    }

    private static int[] generateRandomArray(int size) {
        Random rand = new Random(SEED);
        int[] arr = new int[size];
        for (int i = 0; i < size; i++) {
            arr[i] = rand.nextInt(1_000_000) - 500_000;
        }
        return arr;
    }

    private static double measureSortTime(int[] baseArray, String sorterType) {
        // Warmup
        for (int i = 0; i < WARMUP_ITERATIONS; i++) {
            runSort(baseArray.clone(), sorterType);
        }

        // Actual measurement
        long totalTime = 0;
        for (int i = 0; i < TEST_ITERATIONS; i++) {
            int[] arr = baseArray.clone();
            long start = System.nanoTime();
            runSort(arr, sorterType);
            long end = System.nanoTime();
            totalTime += (end - start);
        }

        return (totalTime / TEST_ITERATIONS) / 1_000_000.0;
    }

    private static void runSort(int[] arr, String sorterType) {
        if (sorterType.equals("Arrays.sort")) {
            Arrays.sort(arr);
        } else if (sorterType.equals("Sequential_quick")) {
            new Sequential_quick(arr).sort();
        } else if (sorterType.equals("Sequential_merge")) {
            new Sequential_merge(arr).sort();
        } else if (sorterType.equals("Parallel_quick")) {
            new Parallel_quick(arr).sort();
        } else if (sorterType.equals("Parallel_quick_simple")) {
            new Parallel_quick_simple(arr).sort();
        } else if (sorterType.equals("Parallel_merge")) {
            new Parallel_merge(arr).sort();
        }
    }

    private static void generateFullComparisonCSV() {
        System.out.println("Creating sorting_benchmark.csv...");

        try {
            PrintWriter writer = new PrintWriter(new File("sorting_benchmark.csv"));
            writer.println("n,Arrays.sort,Seq_Quick,Seq_Merge,Par_Quick,Par_Quick_Simple,Par_Merge," +
                          "Quick_Speedup,Quick_Simple_Speedup,Merge_Speedup");

            for (int size : SIZES) {
                System.out.println("  Testing size: " + size);
                int[] baseArray = generateRandomArray(size);

                double arraysSort = measureSortTime(baseArray, "Arrays.sort");
                double seqQuick = measureSortTime(baseArray, "Sequential_quick");
                double seqMerge = measureSortTime(baseArray, "Sequential_merge");
                double parQuick = measureSortTime(baseArray, "Parallel_quick");
                double parQuickSimple = measureSortTime(baseArray, "Parallel_quick_simple");
                double parMerge = measureSortTime(baseArray, "Parallel_merge");

                double quickSpeedup = seqQuick / parQuick;
                double quickSimpleSpeedup = seqQuick / parQuickSimple;
                double mergeSpeedup = seqMerge / parMerge;

                writer.printf("%d,%.3f,%.3f,%.3f,%.3f,%.3f,%.3f,%.3f,%.3f,%.3f%n",
                        size, arraysSort, seqQuick, seqMerge, parQuick, parQuickSimple, parMerge,
                        quickSpeedup, quickSimpleSpeedup, mergeSpeedup);
            }

            writer.close();
            System.out.println("\n  -> sorting_benchmark.csv created");

        } catch (IOException e) {
            System.out.println("Error creating CSV: " + e.getMessage());
        }
    }
}


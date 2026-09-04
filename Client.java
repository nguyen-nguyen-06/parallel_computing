package parallel_computing;

import parallel_computing.parallel_sorting.*;
import parallel_computing.parallel_matrix_mul.*;

import java.io.*;
import java.util.*;

public class Client {
    private static final Scanner scanner = new Scanner(System.in);
    private static boolean compare;

    public static void main(String[] args) {
        System.out.println("=== Parallel Computing Client ===");
        System.out.println("1. Run without comparison (parallel only)");
        System.out.println("2. Run with comparison (parallel vs sequential)");
        System.out.print("Choose (1 or 2): ");
        compare = Integer.parseInt(scanner.nextLine().trim()) == 2;

        System.out.println("\n1. Parallel Sorting");
        System.out.println("2. Parallel Matrix Multiplication");
        System.out.print("Choose (1 or 2): ");
        int choice = Integer.parseInt(scanner.nextLine().trim());

        if (choice == 1) {
            runSorting();
        } else if (choice == 2) {
            runMatrix();
        } else {
            System.out.println("Invalid choice.");
        }
    }

    private static void runSorting() {
        Parallel_merge.CUTOFFMERGE = 4000;

        System.out.println("\nExpected file format:");
        System.out.println("  <size> <val1> <val2> ... <valN>");
        System.out.println("  Example: 5 3 1 4 1 5");
        System.out.print("\nEnter input file path: ");
        String filePath = scanner.nextLine().trim();
        int[] arr;
        try {
            arr = new Sequential_quick(filePath).getList();
        } catch (FileNotFoundException e) {
            System.out.println("File not found: " + filePath);
            return;
        }

        long start;

        if (compare) {
            Parallel_quick.CUTOFFPARTITION = 10000;
            Parallel_quick.CUTOFFQUICK = 4000;

            start = System.nanoTime();
            new Sequential_quick(arr.clone()).sort();
            long seqQuickTime = System.nanoTime() - start;

            start = System.nanoTime();
            new Sequential_merge(arr.clone()).sort();
            long seqMergeTime = System.nanoTime() - start;

            start = System.nanoTime();
            new Parallel_quick(arr.clone()).sort();
            long parQuickTime = System.nanoTime() - start;

            start = System.nanoTime();
            new Parallel_quick_simple(arr.clone()).sort();
            long parQuickSimpleTime = System.nanoTime() - start;

            start = System.nanoTime();
            int[] result = new Parallel_merge(arr.clone()).sort();
            long parMergeTime = System.nanoTime() - start;

            writeArrayToFile(result, "sorting_output.txt");

            double sqMs = seqQuickTime / 1_000_000.0;
            double smMs = seqMergeTime / 1_000_000.0;
            double pqMs = parQuickTime / 1_000_000.0;
            double pqsMs = parQuickSimpleTime / 1_000_000.0;
            double pmMs = parMergeTime / 1_000_000.0;

            System.out.println("\nSorted output written to sorting_output.txt");
            System.out.println("\n=== Results ===");
            System.out.println("Array size: " + arr.length);
            System.out.printf("%-30s %10.3f ms%n", "Sequential Quick Sort:", sqMs);
            System.out.printf("%-30s %10.3f ms%n", "Sequential Merge Sort:", smMs);
            System.out.printf("%-30s %10.3f ms%n", "Parallel Quick Sort:", pqMs);
            System.out.printf("%-30s %10.3f ms%n", "Parallel Quick Simple Sort:", pqsMs);
            System.out.printf("%-30s %10.3f ms%n", "Parallel Merge Sort:", pmMs);
            System.out.printf("%nQuick Sort speedup:        %.2fx%n", sqMs / pqMs);
            System.out.printf("Quick Simple speedup:      %.2fx%n", sqMs / pqsMs);
            System.out.printf("Merge Sort speedup:        %.2fx%n", smMs / pmMs);
        } else {
            start = System.nanoTime();
            int[] result = new Parallel_merge(arr).sort();
            long parMergeTime = System.nanoTime() - start;

            writeArrayToFile(result, "sorting_output.txt");

            System.out.println("\nSorted output written to sorting_output.txt");
            System.out.println("\n=== Results ===");
            System.out.println("Array size: " + arr.length);
            System.out.printf("%-30s %10.3f ms%n", "Parallel Merge Sort:", parMergeTime / 1_000_000.0);
        }

        System.out.println("\nFinished!");
    }

    private static void runMatrix() {
        Parallel_matrix.MATRIX_CUTOFF = 128;
        Parallel_matrix.DOT_CUTOFF = 10000;

        System.out.println("\nExpected file format:");
        System.out.println("  <rowA> <colA>");
        System.out.println("  <values for matrix A, space-separated>");
        System.out.println("  <rowB> <colB>");
        System.out.println("  <values for matrix B, space-separated>");
        System.out.println("  Example (2x2 * 2x2):");
        System.out.println("  2 2  1.0 2.0 3.0 4.0  2 2  5.0 6.0 7.0 8.0");
        System.out.print("\nEnter input file path: ");
        String filePath = scanner.nextLine().trim();
        double[][] matA, matB;
        try {
            Sequential_matrix temp = new Sequential_matrix(filePath);
            matA = temp.getA();
            matB = temp.getB();
        } catch (FileNotFoundException e) {
            System.out.println("File not found: " + filePath);
            return;
        }

        long start;

        start = System.nanoTime();
        double[][] result = new Parallel_matrix(matA, matB).multiply();
        long parTime = System.nanoTime() - start;

        writeMatrixToFile(result, "matrix_output.txt");

        double parMs = parTime / 1_000_000.0;

        System.out.println("\nResult written to matrix_output.txt");
        System.out.println("\n=== Results ===");
        System.out.printf("Matrix size: %dx%d * %dx%d%n",
                matA.length, matA[0].length, matB.length, matB[0].length);

        if (compare) {
            start = System.nanoTime();
            new Sequential_matrix(matA, matB).multiply();
            long seqTime = System.nanoTime() - start;
            double seqMs = seqTime / 1_000_000.0;

            System.out.printf("%-25s %10.3f ms%n", "Sequential:", seqMs);
            System.out.printf("%-25s %10.3f ms%n", "Parallel:", parMs);
            System.out.printf("Speedup: %.2fx%n", seqMs / parMs);
        } else {
            System.out.printf("%-25s %10.3f ms%n", "Parallel:", parMs);
        }

        System.out.println("\nFinished!");
    }

    private static void writeArrayToFile(int[] arr, String fileName) {
        try {
            PrintWriter writer = new PrintWriter(fileName);
            for (int i = 0; i < arr.length; i++) {
                writer.print(arr[i]);
                if (i < arr.length - 1) writer.print(" ");
            }
            writer.println();
            writer.close();
        } catch (FileNotFoundException e) {
            System.out.println("Error writing to " + fileName);
        }
    }

    private static void writeMatrixToFile(double[][] mat, String fileName) {
        try {
            PrintWriter writer = new PrintWriter(fileName);
            for (int i = 0; i < mat.length; i++) {
                for (int j = 0; j < mat[i].length; j++) {
                    writer.printf("%.4f", mat[i][j]);
                    if (j < mat[i].length - 1) writer.print(" ");
                }
                writer.println();
            }
            writer.close();
        } catch (FileNotFoundException e) {
            System.out.println("Error writing to " + fileName);
        }
    }
}
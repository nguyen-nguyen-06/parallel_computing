package parallel_computing.parallel_matrix_mul;

import java.io.*;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveAction;
import java.util.concurrent.RecursiveTask;

/**
 * Parallel matrix multiplication using the ForkJoin framework.
 * Recursively divides the result matrix into quadrants until reaching MATRIX_CUTOFF,
 * then computes each cell's dot product potentially in parallel based on DOT_CUTOFF.
 * 
 * @specfield POOL : ForkJoinPool // shared thread pool for parallel execution
 * @specfield MATRIX_CUTOFF : int // threshold for sequential matrix region computation
 * @specfield DOT_CUTOFF : int // threshold for sequential dot product computation
 */
public class Parallel_matrix extends Matrix_mul{
    static ForkJoinPool POOL = new ForkJoinPool();
    static int MATRIX_CUTOFF;
    static int DOT_CUTOFF;

    /**
     * Constructs a Parallel_matrix by reading 2 matrices from the specified file.
     * Initializes matrices via superclass constructor
     * 
     * @param fileName the path to the file containing matrix data
     * @throws FileNotFoundException if the file does not exist
     */
    public Parallel_matrix(String fileName) throws FileNotFoundException {
        super(fileName);
    }

    /**
     * Constructs a Parallel_matrix with 2 given matrices.
     * Initializes matrices via superclass constructor
     * 
     * @param matA the first matrix
     * @param matB the second matrix
     * @requires matA != null && matB != null && matA[0].length == matB.length
     */
    public Parallel_matrix(double[][] matA, double[][] matB){
        super(matA, matB);
    }

    /**
     * Computes matrix multiplication in parallel using ForkJoin framework.
     * 
     * @return a new 2D array containing the product of matA and matB
     * @requires MATRIX_CUTOFF and DOT_CUTOFF have been set appropriately
     */
    public double[][] multiply(){
        double[][] matA = getA();
        double[][] matB = getB();
        double[][] matAns = new double[matA.length][matB[0].length];
        POOL.invoke(new MatrixMultiplyAction(matA, matB, matAns, 0, matA.length - 1, 0, matB[0].length - 1));
        return matAns;
    }

    /**
     * A RecursiveAction that computes a rectangular subpart of the result matrix.
     * Using divide-and-conquer: splits into four quadrants until region size <= MATRIX_CUTOFF,
     * then computes each cell using DotProductTask.
     * 
     * @specfield matA, matB : double[][] // source matrices
     * @specfield matAns : double[][] // result matrix to populate
     * @specfield startR, endR : int // row range [startR, endR]
     * @specfield startC, endC : int // column range [startC, endC]
     */
    private static class MatrixMultiplyAction extends RecursiveAction{
        private double[][] matA;
        private double[][] matB;
        private double[][] matAns;
        private int startR;
        private int endR; 
        private int startC;
        private int endC; 

        /**
         * Constructs a RecursiveAction for computing a subpart of the result matrix.
         * 
         * @param matA the first matrix
         * @param matB the second matrix
         * @param matAns the result matrix to populate
         * @param startR starting row index (inclusive)
         * @param endR ending row index (inclusive)
         * @param startC starting column index (inclusive)
         * @param endC ending column index (inclusive)
         * @requires 0 <= startR < endR <= matA.length - 1 &&
         *           0 <= startC < endC <= matB[0].length - 1
         */
        public MatrixMultiplyAction(double[][] matA, double[][] matB, double[][] matAns,
                                     int startR, int endR, int startC, int endC){
            this.matA = matA;
            this.matB = matB;
            this.matAns = matAns;
            this.startR = startR;
            this.endR = endR;
            this.startC = startC;
            this.endC = endC;
        }

        /**
         * Executes the parallel matrix multiplication for this subpart.
         * 
         * @requires Matrix valid and indices are in bounds
         * @effects if subpart size <= MATRIX_CUTOFF, computes sequentially using dot products;
         *          otherwise, forks four subtasks for quadrants and joins them
         * @modifies matAns[startR..endR][startC..endC]
         */
        public void compute(){
            if(endR - startR <= MATRIX_CUTOFF || endC - startC <= MATRIX_CUTOFF){
                for(int i = startR; i <= endR; i++){
                    for(int j = startC; j <= endC; j++){
                        //matAns[i][j] = POOL.invoke(new DotProductTask(matA, matB, i, j, 0, matA[0].length));
                        //might be slower
                        matAns[i][j] = new DotProductTask(matA, matB, i, j, 0, matA[0].length - 1).compute();
                    }
                }
            } else{
                int midrow = startR + (endR - startR)/2;
                int midcol = startC + (endC - startC)/2;
                MatrixMultiplyAction topleft = new MatrixMultiplyAction(matA, matB, matAns, startR, midrow, startC, midcol);
                topleft.fork();
                MatrixMultiplyAction topright = new MatrixMultiplyAction(matA, matB, matAns, startR, midrow, midcol + 1, endC);
                topright.fork();
                MatrixMultiplyAction botleft = new MatrixMultiplyAction(matA, matB, matAns, midrow + 1, endR, startC, midcol);
                botleft.fork();
                MatrixMultiplyAction botright = new MatrixMultiplyAction(matA, matB, matAns, midrow + 1, endR, midcol + 1, endC);
                botright.compute();
                topleft.join();
                topright.join();
                botleft.join();
            }
        }

    }

    /**
     * A RecursiveTask that computes the dot product of a row from matA and a column from matB.
     * Using divide-and-conquer on the summation range until size <= DOT_CUTOFF.
     * 
     * @specfield matA, matB : double[][] // source matrices
     * @specfield row : int // row index in matA
     * @specfield col : int // column index in matB
     * @specfield start, end : int // summation range [start, end]
     */
    private static class DotProductTask extends RecursiveTask<Double>{
        private double[][] matA;
        private double[][] matB;
        private int row;
        private int col;
        private int start; 
        private int end; 

        /**
         * Constructs a RecursiveTask for computing the dot product of a row and column.
         * 
         * @param matA the first matrix
         * @param matB the second matrix
         * @param row the row index in matA
         * @param col the column index in matB
         * @param start starting index for the summation (inclusive)
         * @param end ending index for the summation (inclusive)
         * @requires 0 <= row <= matA.length - 1 &&
         *           0 <= col <= matB[0].length - 1 && 
         *           0 <= start < end <= matA[0].length - 1
         */
        public DotProductTask(double[][] matA, double[][] matB, int row, int col, int start, int end){
            this.matA = matA;
            this.matB = matB;
            this.row = row;
            this.col = col;
            this.start = start;
            this.end = end;
        }

        /**
         * Computes the partial dot product for the specified range.
         * 
         * @return the sum of matA[row][i] * matB[i][col] for i in [start, end]
         * @requires indices are valid
         * @effects if range <= DOT_CUTOFF, computes sequentially; otherwise forks left half and computes right
         */
        public Double compute(){
            if(end - start <= DOT_CUTOFF){
                Double sum = 0.0;
                for(int i = start; i <= end; i++){
                    sum += matA[row][i] * matB[i][col];
                }
                return sum;
            } else {
                int mid = start + (end - start) / 2;
                DotProductTask left = new DotProductTask(matA, matB, row, col, start, mid);
                DotProductTask right = new DotProductTask(matA, matB, row, col, mid + 1, end);
                left.fork();
                Double rightret = right.compute();
                Double leftret = left.join();
                return rightret + leftret;
            }
        }

    }
}


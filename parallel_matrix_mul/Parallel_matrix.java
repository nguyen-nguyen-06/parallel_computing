package parallel_computing.parallel_matrix_mul;

import java.io.*;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveAction;
import java.util.concurrent.RecursiveTask;

public class Parallel_matrix extends Matrix_mul{
    static ForkJoinPool POOL = new ForkJoinPool();
    static int MATRIX_CUTOFF;
    static int DOT_CUTOFF;

    public Parallel_matrix(String fileName) throws FileNotFoundException {
        super(fileName);
    }

    public Parallel_matrix(double[][] matA, double[][] matB){
        super(matA, matB);
    }

    public double[][] multiply(){
        double[][] matA = getA();
        double[][] matB = getB();
        double[][] matAns = new double[matA.length][matB[0].length];
        POOL.invoke(new MatrixMultiplyAction(matA, matB, matAns, 0, matA.length, 0, matB[0].length));
        return matAns;
    }

    private static class MatrixMultiplyAction extends RecursiveAction{
        private double[][] matA;
        private double[][] matB;
        private double[][] matAns;
        private int startR;
        private int endR; // exclusively
        private int startC;
        private int endC; // exclusively

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

        public void compute(){
            if(endR - startR <= MATRIX_CUTOFF || endC - startC <= MATRIX_CUTOFF){
                for(int i = startR; i < endR; i++){
                    for(int j = startC; j < endC; j++){
                        //matAns[i][j] = POOL.invoke(new DotProductTask(matA, matB, i, j, 0, matA[0].length));
                        //might be slower
                        matAns[i][j] = new DotProductTask(matA, matB, i, j, 0, matA[0].length).compute();
                    }
                }
            } else{
                int midrow = startR + (endR - startR)/2;
                int midcol = startC + (endC - startC)/2;
                MatrixMultiplyAction topleft = new MatrixMultiplyAction(matA, matB, matAns, startR, midrow, startC, midcol);
                topleft.fork();
                MatrixMultiplyAction topright = new MatrixMultiplyAction(matA, matB, matAns, startR, midrow, midcol, endC);
                topright.fork();
                MatrixMultiplyAction botleft = new MatrixMultiplyAction(matA, matB, matAns, midrow, endR, startC, midcol);
                botleft.fork();
                MatrixMultiplyAction botright = new MatrixMultiplyAction(matA, matB, matAns, midrow, endR, midcol, endC);
                botright.compute();
                topleft.join();
                topright.join();
                botleft.join();
            }
        }

    }

    private static class DotProductTask extends RecursiveTask<Double>{
        // TODO: select fields
        private double[][] matA;
        private double[][] matB;
        private int row;
        private int col; //inclusively
        private int start; 
        private int end; // exclusively

        public DotProductTask(double[][] matA, double[][] matB, int row, int col, int start, int end){
            // TODO: implement constructor
            this.matA = matA;
            this.matB = matB;
            this.row = row;
            this.col = col;
            this.start = start;
            this.end = end;
        }

        public Double compute(){
            if(end - start <= DOT_CUTOFF){
                Double sum = 0.0;
                for(int i = start; i < end; i++){
                    sum += matA[row][i] * matB[i][col];
                }
                return sum;
            } else {
                int mid = start + (end - start) / 2;
                DotProductTask left = new DotProductTask(matA, matB, row, col, start, mid);
                DotProductTask right = new DotProductTask(matA, matB, row, col, mid, end);
                left.fork();
                Double rightret = right.compute();
                Double leftret = left.join();
                return rightret + leftret;
            }
        }

    }
}


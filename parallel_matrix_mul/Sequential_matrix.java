package parallel_computing.parallel_matrix_mul;

import java.io.*;

public class Sequential_matrix extends Matrix_mul{

    public Sequential_matrix(String fileName) throws FileNotFoundException {
        super(fileName);
    }

    public Sequential_matrix(double[][] matA, double[][] matB){
        super(matA, matB);
    }

    public double[][] multiply(){
        double[][] matA = getA();
        double[][] matB = getB();
        double[][] matAns = new double[matA.length][matB[0].length];
        for(int row = 0; row < matA.length; row++){
            for(int col = 0; col < matB[0].length; col++){
                matAns[row][col] = 0;
                for(int i = 0; i < matA[0].length; i++){
                    matAns[row][col] += matA[row][i] * matB[i][col];
                }
            }
        }
        return matAns;
    }
}


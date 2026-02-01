package parallel_computing.parallel_matrix_mul;

import java.io.*;

/**
 * Sequential matrix multiplication using triple loops. 
 * Built for speed-up comparison
 */
public class Sequential_matrix extends Matrix_mul{

    /**
     * Constructs a Sequential_matrix by reading matrices from the specified file.
     * Initializes matA and matB via superclass
     * 
     * @param fileName the path to the file containing matrix data
     * @throws FileNotFoundException if the file does not exist
     */
    public Sequential_matrix(String fileName) throws FileNotFoundException {
        super(fileName);
    }

    /**
     * Constructs a Sequential_matrix with the given 2 matrices.
     * Initializes matA and matB via superclass
     * 
     * @param matA the first matrix
     * @param matB the second matrix
     * @requires matA != null && matB != null && matA[0].length == matB.length
     */
    public Sequential_matrix(double[][] matA, double[][] matB){
        super(matA, matB);
    }

    /**
     * Computes matrix multiplication sequentially using triples loop 
     * 
     * @return a new 2D array containing matA * matB
     * @requires matA and matB have compatible dimensions
     */
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


package parallel_computing.parallel_matrix_mul;

import java.io.*;
import java.util.*;

/**
 * Abstract base class for matrix multiplication operations.
 * Provides storage for two matrices and common I/O functionality.
 * Subclasses must implement the multiply() method
 * 
 * @specfield matA : double[][] // the first matrix
 * @specfield matB : double[][] // the second matrix
 * Rep Invariant: matA[0].length == matB.length (compatible dimensions for multiplication)
 */
public abstract class Matrix_mul {
    private double[][] matA;
    private double[][] matB;

    /**
     * Constructor that reads two matrices from a file and initializes matA and matB.
     * 
     * @param fileName the path to the file containing matrix data
     * @throws FileNotFoundException if the file does not exist
     * @throws IllegalArgumentException if colA != rowB (matrix multiplication dimension mismatch)
     */
    public Matrix_mul(String fileName) throws FileNotFoundException{
        Scanner in = new Scanner(new File(fileName));
        Integer rowA = Integer.parseInt(in.next());
        Integer colA = Integer.parseInt(in.next());
        matA = new double[rowA][colA];
        for(int i = 0; i < colA; i++){
            for(int j = 0; j < rowA; j++){
                matA[i][j] = Double.parseDouble(in.next());
            }
        }
        Integer rowB = Integer.parseInt(in.next());
        Integer colB = Integer.parseInt(in.next());
        if(colA != rowB){
            throw new IllegalArgumentException("Size Mismatch");
        }
        matB = new double[rowB][colB];
        for(int i = 0; i < colB; i++){
            for(int j = 0; j < rowB; j++){
                matA[i][j] = Double.parseDouble(in.next());
            }
        }
    }

    /**
     * Constructor that reads two matrices from a file and initializes matA and matB.
     * 
     * @param matA the first matrix 
     * @param matB the second matrix 
     * @requires matA != null && matB != null && matA[0].length == matB.length
     * @throws IllegalArgumentException if matA or matB is null, or if column count of A != row count of B
     */
    public Matrix_mul(double[][] matA, double[][] matB){
        if(matA == null || matB == null){
            throw new IllegalArgumentException("Missing Input");
        }
        if(matA[0].length != matB.length){
            throw new IllegalArgumentException("Size Mismatch");
        }

        this.matA = matA;
        this.matB = matB;
    }

    /**
     * Returns the first matrix.
     * 
     * @return matA, the first matrix 
     */
    public double[][] getA(){
        return matA;
    }

    /**
     * Returns the second matrix.
     * 
     * @return matB, the second matrix 
     */
    public double[][] getB(){
        return matB;
    }

    /**
     * Computes the matrix multiplication and writes the result to the specified file.
     * Values seperated by space, one row per line
     * 
     * @param fileName the path to the output file
     * @requires fileName != null && fileName is a valid file path
     * @throws FileNotFoundException if the output file cannot be created
     */
    public void multiplyPrintFile(String fileName) throws FileNotFoundException{
        double[][] result = multiply();

        PrintStream out = new PrintStream(new File(fileName));
        for (int i = 0; i < result.length; i++) {
            for (int j = 0; j < result[i].length; j++) {
                out.print(result[i][j]);
                if (j < result[i].length - 1) {
                    out.print(" ");
                }
            }
            out.println();
        }
    }

    /**
     * Computes and returns the product of matA and matB.
     * 
     * @return a new 2D array representing matA * matB
     * @requires matA and matB have been initialized with compatible dimensions
     */
    public abstract double[][] multiply();
}


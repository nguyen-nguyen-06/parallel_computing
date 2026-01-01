package parallel_computing.parallel_matrix_mul;

import java.io.*;
import java.util.*;

public abstract class Matrix_mul {
    private double[][] matA;
    private double[][] matB;

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

    public double[][] getA(){
        return matA;
    }

    public double[][] getB(){
        return matB;
    }

    public void output() throws FileNotFoundException{
        double[][] result = multiply();

        PrintStream out = new PrintStream(new File("result.txt"));
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

    public abstract double[][] multiply();
}


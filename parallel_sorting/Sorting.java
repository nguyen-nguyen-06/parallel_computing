package parallel_computing.parallel_sorting;

import java.util.*;
import java.io.*;

/**
 * Abstract base class for sorting algorithms.
 * Provides storage for an integer array and common I/O.
 * Subclasses must implement the sort() method to define the sorting algorithm.
 * 
 * @specfield list : int[] // the array to be sorted
 */
public abstract class Sorting{
    private int[] list;

    /**
     * Constructs a Sorting instance by reading integers from a file.
     * 
     * @param fileName path to file with format: size, then array of integer with values seperated by space
     * @throws FileNotFoundException if file does not exist
     * @requires file format: first token is size, followed by <size> numbers of integer tokens
     */
    public Sorting (String fileName) throws FileNotFoundException{
        Scanner in = new Scanner(new File(fileName));
        int size = Integer.parseInt(in.next());
        list = new int[size];
        int index = 0;
        while(in.hasNext()){
            list[index] = Integer.parseInt(in.next());
            index++;
        }
        in.close();
    }

    /**
     * Constructs a Sorting instance with the given array.
     * 
     * @param list the array to be sorted
     * @requires list != null
     */
    public Sorting(int[] list){
        this.list = list;
    }

    /**
     * Returns the array.
     * 
     * @return the array
     */
    public int[] getList(){
        return list;
    }

    /**
     * Sorts the internal array and returns it.
     * 
     * @return the sorted array
     */
    public abstract int[] sort();
}


package parallel_computing.parallel_sorting;

import java.io.*;

/**
 * Sequential merge sort implementation using divide-and-conquer.
 * Recursively divides the array in half, sorts each half, and merges the sorted halves.
 */
public class Sequential_merge extends Sorting {

    /**
     * Constructs a Sequential_merge sorter with the given array.
     * 
     * @param list the array to sort
     * @requires list != null
     */
    public Sequential_merge(int[] list){
        super(list);
    }

    /**
     * Constructs a Sequential_merge sorter by reading from a file.
     * 
     * @param fileName path to file containing the array
     * @throws FileNotFoundException if file does not exist
     */
    public Sequential_merge(String fileName) throws FileNotFoundException{
        super(fileName);
    }

    /**
     * Sorts the array using sequential merge sort.
     * 
     * @return the sorted array
     */
    public int[] sort(){
        int[] list = getList();
        if (list.length <= 1){
            return list;
        } 
        int[] temp = new int[list.length];
        divide(list, 0, list.length - 1, temp);
        return list;
    }

    /**
     * Recursively divides and conquers the array for merge sort.
     * Recursively sorts left and right halves, then merges
     * 
     * @param list the array to sort
     * @param start starting index (inclusive)
     * @param end ending index (inclusive)
     * @param temp temporary array for merging
     * @requires start <= end && temp.length >= list.length
     */
    private void divide(int[] list, int start, int end, int[] temp){
        if (start < end){
            int mid = start + (end - start) / 2;

            divide(list, start, mid, temp);
            divide(list, mid + 1, end, temp);

            merge(list, start, mid, end, temp);
        }
    }

    /**
     * Merges two sorted subarrays into one sorted subarray.
     * 
     * @param list the array containing both halves
     * @param start starting index of left half
     * @param mid ending index of left half
     * @param end ending index of right half
     * @param temp temporary storage array
     * @requires list[start..mid] and list[mid+1..end] are sorted
     */
    private void merge(int[] list, int start, int mid, int end, int[] temp){
        for (int i = start; i <= end; i++) {
            temp[i] = list[i];
        }

        int i = start;      
        int j = mid + 1;    
        int k = start;      

        while (i <= mid && j <= end) {
            if (temp[i] <= temp[j]) {
                list[k++] = temp[i++];
            } else {
                list[k++] = temp[j++];
            }
        }

        while (i <= mid) {
            list[k++] = temp[i++];
        }
    }
}

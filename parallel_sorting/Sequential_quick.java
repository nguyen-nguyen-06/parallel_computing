package parallel_computing.parallel_sorting;

import java.io.*;

/**
 * Sequential quicksort implementation using in-place partitioning.
 * Using median-of-three pivot selection to avoid worst-case O(n^2) on sorted inputs.
 */
public class Sequential_quick extends Sorting{

    /**
     * Constructs a Sequential_quick sorter by reading from a file.
     * 
     * @param fileName path to file containing the array
     * @throws FileNotFoundException if file does not exist
     */
    public Sequential_quick(String fileName) throws FileNotFoundException{
        super(fileName);
    }

    /**
     * Constructs a Sequential_quick sorter with the given array.
     * 
     * @param list the array to sort
     * @requires list != null
     */
    public Sequential_quick(int[] list){
        super(list);
    }

    /**
     * Sorts the array using sequential quicksort (in-place).
     * 
     * @return the sorted array
     */
    public int[] sort(){
        int[] list = getList();
        sort(getList(), 0, list.length - 1);
        return list;
    }

    /**
     * Recursively sorts the subarray list[start..end] in place.
     * Partitions around pivot and recursively sorts left and right partitions
     * 
     * @param list the array being sorted
     * @param start starting index (inclusive)
     * @param end ending index (inclusive)
     * @requires 0 <= start <= end < list.length (or start > end for base case)
     */
    private void sort(int[] list, int start, int end){
        if(start < end){
            int ptr1 = start - 1;
            int ptr2 = start;
            int pivot = medianOfThree(list, start, end);
            int pivotVal = list[pivot];
            swap(list, pivot, end);
            while(ptr2 <= end){
                if(list[ptr2] <= pivotVal){
                    swap(list, ++ptr1, ptr2);
                } 
                ptr2++;
            }
            sort(list, start, ptr1 - 1);
            sort(list, ptr1 + 1, end);
        }
    }

    /**
     * Swaps two elements in the array.
     * list[a] and list[b] are exchanged
     * 
     * @param list the array
     * @param a first index
     * @param b second index
     * @requires 0 <= a, b < list.length
     */
    private void swap(int[] list, int a, int b){
        int temp = list[a];
        list[a] = list[b];
        list[b] = temp;
    }

    /**
     * Finds the index of the median of three elements for pivot selection.
     * 
     * @param list the array
     * @param start starting index
     * @param end ending index
     * @return index of the median among list[start], list[mid], list[end]
     * @requires start <= end && indices are valid
     */
    private int medianOfThree(int[] list, int start, int end){
        // Median of list[start], list[mid], list[end]
        int mid = start + (end - start) / 2;

        int a = list[start];
        int b = list[mid];
        int c = list[end];

        // Return index of median value
        if ((a <= b && b <= c) || (c <= b && b <= a)) {
            return mid;
        } else if ((b <= a && a <= c) || (c <= a && a <= b)) {
            return start;
        } else {
            return end;
        }
    }
}


package parallel_computing.parallel_sorting;

import java.io.*;
import java.util.*;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveAction;

/**
 * Simplified parallel quicksort with sequential partitioning and parallel recursion.
 * Uses partition sequentially, then forks left and right subarrays for parallel sorting.
 * More practical than fully parallel partitioning for typical use cases.
 * 
 * Time complexity: O(n log n) work 
 * Span: O(n) with parallel task
 * 
 * Trade-offs vs Parallel_quick:
 * - Lower memory overhead (partition is in-place)
 * - Better cache locality
 * - Partition step is sequential (O(n) work not parallelized)
 * - Still achieves good speedup through parallel recursive calls
 * 
 * @specfield CUTOFFQUICK : int // threshold for switching to Arrays.sort
 * @specfield POOL : ForkJoinPool // shared thread pool
 */

public class Parallel_quick_simple extends Sorting{
    static int CUTOFFQUICK;
    static final ForkJoinPool POOL = new ForkJoinPool();

    /**
     * Constructs a Parallel_quick_simple sorter with the given array.
     * 
     * @param list the array to sort
     * @requires list != null
     */
    public Parallel_quick_simple(int[] list){
        super(list);
    }

    /**
     * Constructs a Parallel_quick_simple sorter by reading from a file.
     * 
     * @param fileName path to file containing the array
     * @throws FileNotFoundException if file does not exist
     */
    public Parallel_quick_simple(String fileName) throws FileNotFoundException{
        super(fileName);
    }

    /**
     * Sorts the array using parallel quicksort with sequential Lomuto partitioning.
     * Spawns parallel tasks for recursive sorting after sequential partition
     * 
     * @return a new sorted array
     * @requires CUTOFFQUICK is set appropriately
     */
    public int[] sort(){
        int[] input = getList();
        if (input.length <= 1){
            return input;
        } 
        int[] output = new int[input.length];
        POOL.invoke(new ParallelDivide(input, output, 0, input.length - 1));
        return output;
    }

    /**
     * A RecursiveAction that performs parallel quicksort with sequential partitioning.
     * Partitions input[lo..hi] using same method as sequential quicksort then forks
     *  left/right subtasks.
     * 
     * @specfield input : int[] // the array being partitioned and sorted
     * @specfield output : int[] // the destination array for sorted results
     * @specfield lo, hi : int // range to sort (inclusive)
     */
    public class ParallelDivide extends RecursiveAction{
        private int[] input;
        private int[] output;
        private int lo;
        private int hi;

        /**
         * Constructs a ParallelDivide task for sorting input[lo..hi].
         * 
         * @param input the source array
         * @param output the destination array for sorted results
         * @param lo starting index (inclusive)
         * @param hi ending index (inclusive)
         */
        public ParallelDivide(int[] input, int[] output, int lo, int hi){
            this.input = input;
            this.output = output;
            this.lo = lo;
            this.hi = hi;
        }

        /**
         * Executes parallel quicksort for this subarray.
         * 
         * @effects if hi-lo <= CUTOFF, sorts using Arrays.sort and copies to output;
         *          otherwise partitions sequentially and forks left/right subtasks
         */
        public void compute(){
            if(hi - lo <= CUTOFFQUICK){
                Arrays.sort(input, lo, hi + 1);
                System.arraycopy(input, lo, output, lo, hi - lo + 1);
            }else{
                int pivotPos = sequentialFilter();
                output[pivotPos] = input[pivotPos];
                ParallelDivide leftTask = new ParallelDivide(input, output, lo, pivotPos - 1);
                ParallelDivide rightTask = new ParallelDivide(input, output, pivotPos + 1, hi);

                leftTask.fork();
                rightTask.compute();
                leftTask.join();
            }
        }

        /**
         * Partitions input[lo..hi] around a pivot using Lomuto partitioning.
         * elements <= pivot are moved left of pivot, elements > pivot are moved right
         * 
         * @return the final index of the pivot element
         */
        private int sequentialFilter(){
            int ptr1 = lo - 1;
            int ptr2 = lo;
            int pivotPos = medianOfThree();
            int pivotVal = input[pivotPos];
            swap(pivotPos, hi);
            while(ptr2 <= hi){
                if(input[ptr2] <= pivotVal){
                    swap(++ptr1, ptr2);
                }
                ptr2++;
            }
            return ptr1;
        }

        /**
         * Swaps two elements in the input array.
         * 
         * @param a first index
         * @param b second index
         */
        private void swap(int a, int b){
            int temp = input[a];
            input[a] = input[b];
            input[b] = temp;
        }

        /**
         * Selects pivot index using median-of-three strategy for this subarray.
         * 
         * @return index of median among input[lo], input[mid], input[hi]
         */
        private int medianOfThree(){
            int mid = lo + (hi - lo) / 2;

            int a = input[lo];
            int b = input[mid];
            int c = input[hi];

            if ((a <= b && b <= c) || (c <= b && b <= a)) {
                return mid;
            } else if ((b <= a && a <= c) || (c <= a && a <= b)) {
                return lo;
            } else {
                return hi;
            }
        }
    }
}

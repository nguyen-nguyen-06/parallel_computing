package parallel_computing.parallel_sorting;

import java.io.*;
import java.util.*;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveAction;

/**
 * Parallel merge sort implementation with parallel divide and parallel merge.
 * The divide phase recursively splits the array and sorts halves in parallel.
 * The merge phase uses binary search on the longer array's median to create
 * balanced subproblems, ensuring O(log n) span for merging.
 * 
 * Time complexity: O(n log n) work
 * Span: O(log^3 n) with parallel merge
 * 
 * @specfield POOL : ForkJoinPool // shared thread pool
 * @specfield CUTOFFMERGE : int // threshold for sequential operations
 */

public class Parallel_merge extends Sorting{
    static ForkJoinPool POOL = new ForkJoinPool();
    static int CUTOFFMERGE;

    /**
     * Constructs a Parallel_merge sorter with the given array.
     * 
     * @param input the array to sort
     * @requires input != null
     */
    public Parallel_merge(int[] input){
        super(input);
    }

    /**
     * Constructs a Parallel_merge sorter by reading from a file.
     * 
     * @param fileName path to file containing the array
     * @throws FileNotFoundException if file does not exist
     */
    public Parallel_merge(String fileName) throws FileNotFoundException{
        super(fileName);
    }

    /**
     * Sorts the array using parallel merge sort with parallel divide and merge.
     * 
     * @return a new sorted array
     * @requires CUTOFFMERGE is set appropriately
     * @effects spawns parallel tasks for divide and merge operations
     */
    public int[] sort(){
        int[] input = getList();
        ParallelDivide sortingTask = new ParallelDivide(input, 0, input.length - 1);
        POOL.invoke(sortingTask);
        return sortingTask.output;
    }

    /**
     * ==================== PARALLEL MERGE SORT OVERVIEW ====================
     * A parallel implementation of merge sort that parallelizes both the
     * divide phase and the merge phase.
     * 
     * Two-phase algorithm:
     * 
     *   Phase 1 - Parallel Divide (ParallelDivide):
     *     - Recursively divide array into halves
     *     - Fork left half, compute right half, join
     *     - Base case: use Arrays.sort when size <= CUTOFFMERGE
     *     - Produces two sorted subarrays to be merged
     * 
     *   Phase 2 - Parallel Merge (ParallelMerge):
     *     - Find median of longer array
     *     - Binary search for split point in shorter array
     *     - Place median directly in output
     *     - Fork left merge, compute right merge, join
     *     - Base case: sequential two-pointer merge when size <= CUTOFFMERGE
     */


    /**
     * A RecursiveAction that performs the divide phase of parallel merge sort.
     * Recursively divides the array, forks left half, computes right half,
     * then invokes ParallelMerge to combine the sorted halves.
     * 
     * @specfield input : int[] // source array
     * @specfield output : int[] // sorted result (set after compute)
     * @specfield lo, hi : int // range to sort (inclusive)
     */
    public static class ParallelDivide extends RecursiveAction{
        private int[] input;
        private int[] output;
        private int lo;
        private int hi;

        /**
         * Constructs a ParallelDivide task for sorting input[lo..hi].
         * 
         * @param input the array to sort
         * @param lo starting index (inclusive)
         * @param hi ending index (inclusive)
         */
        public ParallelDivide(int[] input, int lo, int hi){
            this.input = input;
            this.lo = lo;
            this.hi = hi;
        }

        /**
         * Executes parallel merge sort divide step.
         * 
         * @effects if hi-lo <= CUTOFFMERGE, sorts using Arrays.sort;
         *          otherwise forks left half, computes right, and merges in parallel
         * @modifies output field contains the sorted result
         */
        public void compute(){
            if(hi - lo <= CUTOFFMERGE){
                output = new int[hi - lo + 1];
                System.arraycopy(input, lo, output, 0, hi - lo + 1);
                Arrays.sort(output);
            } else {
                int mid = lo + (hi - lo)/2;
                ParallelDivide leftTask = new ParallelDivide(input, lo, mid);
                ParallelDivide rightTask = new ParallelDivide(input, mid + 1, hi);

                leftTask.fork();
                rightTask.compute();
                leftTask.join();

                int[] leftSorted = leftTask.output;
                int[] rightSorted = rightTask.output;
                int[] mergedArr = new int[leftSorted.length + rightSorted.length];

                ParallelMerge mergeTask = new ParallelMerge(leftSorted, 0, leftSorted.length - 1,
                                                 rightSorted, 0, rightSorted.length -1, mergedArr, 0);
                POOL.invoke(mergeTask);
                output = mergeTask.mergedArr;
            }
        }
    }

    /**
     * ==================== PARALLEL MERGE ====================
     * Merges two sorted arrays in parallel using a divide-and-conquer approach.
     * The key is to split the merge problem into independent subproblems
     * that can be solved in parallel.
     * 
     * Algorithm:
     *   Base case (total length <= CUTOFFMERGE):
     *     - Use sequential two-pointer merge (mergeSortedArr)
     *   
     *   Recursive case:
     *     1. Ensure longArr is the longer array (swap if necessary)
     *     2. Find median of longArr: medianIndex = longStart + longLen/2
     *     3. Binary search in shortArr for split point:
     *        - Find highest index j where shortArr[j] <= longArr[medianIndex]
     *     4. Place median element directly in output:
     *        - Position = offset + (elements before median in longArr) + (elements before split in shortArr)
     *     5. Fork left merge: shortArr[start..j] with longArr[start..median-1]
     *     6. Compute right merge: shortArr[j+1..end] with longArr[median+1..end]
     *     7. Join left merge
     * 
     * Why this works:
     *   - All elements in left portions are <= median
     *   - All elements in right portions are >= median
     *   - Left and right merges are completely independent
     *   - Choosing median of longer array guarantees each subproblem is at most 3/4 of original
     * 
     * Complexity:
     *   Work: O(n) - each element is placed exactly once
     *   Span: O(log^2 n) - O(log n) levels of recursion, O(log n) for binary search at each level
     * 
     * @specfield shortArr, longArr : int[] // two sorted arrays to merge
     * @specfield mergedArr : int[] // destination for merged result
     * @specfield offset : int // starting position in mergedArr
     */

    /**
     * A RecursiveAction that merges two sorted arrays in parallel.
     * Uses the median of the longer array to split both arrays into balanced subproblems.
     * The median element is placed directly, then left and right merges proceed in parallel.
     * 
     * @specfield shortArr, longArr : int[] // the two sorted arrays to merge
     * @specfield shortStart, shortEnd : int // bounds of shortArr
     * @specfield longStart, longEnd : int // bounds of longArr
     * @specfield mergedArr : int[] // destination array
     * @specfield offset : int // starting position in mergedArr
     */
    public static class ParallelMerge extends RecursiveAction{
        private int[] shortArr; 
        private int shortStart;
        private int shortEnd;
        private int[] longArr;
        private int longStart;
        private int longEnd;
        private int[] mergedArr;
        private int offset; //num of element in front in output

        /**
         * Constructs a ParallelMerge task for merging two sorted subarrays.
         * 
         * @param shortArr first sorted array
         * @param shortStart starting index of first array (inclusive)
         * @param shortEnd ending index of first array (inclusive)
         * @param longArr second sorted array
         * @param longStart starting index of second array (inclusive)
         * @param longEnd ending index of second array (inclusive)
         * @param mergedArr destination array
         * @param offset starting position in mergedArr
         */
        public ParallelMerge(int[] shortArr, int shortStart, int shortEnd, int[] longArr, int longStart, int longEnd, int[] mergedArr, int offset){
            this.shortArr = shortArr;
            this.shortStart = shortStart;
            this.shortEnd = shortEnd;
            this.longArr = longArr;
            this.longStart = longStart;
            this.longEnd = longEnd;
            this.mergedArr = mergedArr;
            this.offset = offset;
        }

        /**
         * Executes parallel merge of two sorted subarrays.
         * 
         * @effects if total length <= CUTOFFMERGE, merges sequentially;
         *          otherwise swaps arrays if needed, finds median split point,
         *          places median, forks left merge, computes right merge
         * @modifies mergedArr[offset..offset+total_length-1]
         */
        public void compute(){
            int shortLen = shortEnd - shortStart + 1;
            int longLen = longEnd - longStart + 1;
            if(longLen <= CUTOFFMERGE){
                mergeSortedArr();
            } else{
                if(shortLen > longLen){
                    swapArr();
                }

                //median of longer arr to make sure the total size to compute after 
                //each falls somewhere between 1/4 and 3/4 
                int longMed = longStart + longLen/2; // the array is sorted so the mid point is also median

                //the index where shortStart -> j are smaller than longArr[medLong]
                int leftLower = lowerBoundInShortArr(shortStart, shortEnd, longArr[longMed]);
                
                // Calculate output positions
                // Left side: long[Start..Med -1] + short[start..j]
                // Middle: Med 
                // Right side: long[Med+1..end] + B[j+1..end]
                int numBeforeMedInLong = longMed - longStart;
                int numBeforeMedInShort = leftLower - shortStart + 1;
                int medPositionAtMergedOutput = numBeforeMedInLong + numBeforeMedInShort + offset;
                mergedArr[medPositionAtMergedOutput] = longArr[longMed];
                //offset left = offset (or shortStart)
                //offset right = medPositionAtMergedOutput + 1
                ParallelMerge leftTask = new ParallelMerge(shortArr, shortStart, leftLower,
                                             longArr, longStart, longMed - 1, mergedArr, offset);
                ParallelMerge rightTask = new ParallelMerge(shortArr, leftLower + 1, shortEnd,
                                             longArr, longMed + 1, longEnd,
                                             mergedArr, medPositionAtMergedOutput + 1);
                leftTask.fork();
                rightTask.compute();
                leftTask.join();
            }
        }

        /**
         * Sequentially merges two sorted subarrays using two-pointer.
         * 
         * @effects merges shortArr[shortStart..shortEnd] and longArr[longStart..longEnd]
         *          into mergedArr starting at offset
         */
        private void mergeSortedArr(){
            int index = offset;
            int i = shortStart;
            int j = longStart;
            while(i <= shortEnd && j <= longEnd){
                if(shortArr[i] <= longArr[j]){
                    mergedArr[index++] = shortArr[i++];
                } else {
                    mergedArr[index++] = longArr[j++];
                }
            }
            while(i <= shortEnd){
                mergedArr[index++] = shortArr[i++];
            }

            while(j <= longEnd){
                mergedArr[index++] = longArr[j++];
            }
        }

        /**
         * Finds the highest index i in shortArr[start..end] such that shortArr[i] <= val.
         * Used to find the split point for parallel merge.
         * 
         * @param start starting index (inclusive)
         * @param end ending index (inclusive)
         * @param val the value to compare against
         * @return highest i where shortArr[i] <= val, or start-1 if all elements > val
         * @requires shortArr is sorted in [start..end]
         */
        private int lowerBoundInShortArr(int start, int end, int val) {
            int ans = start - 1;

            while (start <= end) {
                int mid = start + (end - start) / 2;

                if (shortArr[mid] <= val) {
                    ans = mid;        
                    start = mid + 1;  
                } else {
                    end = mid - 1;
                }
            }
            return ans;
        }

        /**
         * Swaps the short and long array references and their bounds.
         * Called when shortArr is actually longer than longArr to maintain 
         * that longArr is always the longer array.
         */
        private void swapArr(){
            int[] tempArr = shortArr;
            shortArr = longArr;
            longArr = tempArr;

            int tempStart = shortStart;
            shortStart = longStart;
            longStart = tempStart;

            int tempEnd = shortEnd;
            shortEnd = longEnd;
            longEnd = tempEnd;
        }
    }
}

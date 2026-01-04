package parallel_computing.parallel_sorting;

import java.io.*;
import java.util.*;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveAction;

public class Parallel_merge extends Sorting{
    static ForkJoinPool POOL = new ForkJoinPool();
    static int CUTOFFMERGE;

    public Parallel_merge(int[] input){
        super(input);
    }

    public Parallel_merge(String fileName) throws FileNotFoundException{
        super(fileName);
    }

    public int[] sort(){
        int[] input = getList();
        ParallelDivide sortingTask = new ParallelDivide(input, 0, input.length - 1);
        POOL.invoke(sortingTask);
        return sortingTask.output;
    }

    public static class ParallelDivide extends RecursiveAction{
        private int[] input;
        private int[] output;
        private int lo;
        private int hi;

        public ParallelDivide(int[] input, int lo, int hi){
            this.input = input;
            this.lo = lo;
            this.hi = hi;
        }

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

    public static class ParallelMerge extends RecursiveAction{
        private int[] shortArr; 
        private int shortStart; // start to end of shortArr
        private int shortEnd;
        private int[] longArr;
        private int longStart;
        private int longEnd;
        private int[] mergedArr;
        private int offset; //num of element in front in output

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

        public void compute(){
            int shortLen = shortEnd - shortStart + 1;
            int longLen = longEnd - longStart + 1;
            if(longLen <= CUTOFFMERGE){
                mergeSortedArr();
            } else{
                if(shortLen > longLen){
                    swap();
                }

                //median of longer arr to make sure the total size to compute after 
                // each  falls somewhere between 1/4 and 3/4 
                int longMed = longStart + longLen/2; // the array is sorted so the mid point is also median

                //the index where shortStart -> j are smaller than longArr[medLong]
                int leftLower = lowerBound(shortArr, shortStart, shortEnd, longArr[longMed]);
                
                // Calculate output positions
                // Left side: long[Start..Med -1] + short[start..j]
                // Middle: Med 
                // Right side: long[Med+1..end] + B[j+1..end]
                int numBeforeMedInLong = longMed - longStart;
                int numBeforeMedInShort = leftLower - shortStart + 1;
                int medPositionAtMergedOutput = numBeforeMedInLong + numBeforeMedInShort + offset;
                mergedArr[medPositionAtMergedOutput] = longArr[longMed];
                //offset left = offste (or shortStart)
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

        // Find the highest index i such that shortArr[i] <= val
        // return start - 1 when all element > val
        private int lowerBound(int[] arr, int start, int end, int val) {
            int ans = start - 1;

            while (start <= end) {
                int mid = start + (end - start) / 2;

                if (arr[mid] <= val) {
                    ans = mid;        // mid is a valid candidate
                    start = mid + 1;  // try to find a higher index
                } else {
                    end = mid - 1;
                }
            }
            return ans;
        }

        //swap 2 array and their attributes
        private void swap(){
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

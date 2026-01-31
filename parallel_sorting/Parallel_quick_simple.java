package parallel_computing.parallel_sorting;

import java.io.*;
import java.util.*;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveAction;

public class Parallel_quick_simple extends Sorting{
    static int CUTOFF;
    static final ForkJoinPool POOL = new ForkJoinPool();

    public Parallel_quick_simple(int[] list){
        super(list);
    }

    public Parallel_quick_simple(String fileName) throws FileNotFoundException{
        super(fileName);
    }

    public int[] sort(){
        int[] input = getList();
        if (input.length <= 1){
            return input;
        } 
        int[] output = new int[input.length];
        POOL.invoke(new ParallelDivide(input, output, 0, input.length - 1));
        return output;
    }

    public class ParallelDivide extends RecursiveAction{
        private int[] input;
        private int[] output;
        private int lo;
        private int hi;

        public ParallelDivide(int[] input, int[] output, int lo, int hi){
            this.input = input;
            this.output = output;
            this.lo = lo;
            this.hi = hi;
        }

        public void compute(){
            if(hi - lo <= CUTOFF){
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

        //return the pivot position seperating the array 
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

        private void swap(int a, int b){
            int temp = input[a];
            input[a] = input[b];
            input[b] = temp;
        }

        private int medianOfThree(){
            // Median of input[lo], input[mid], input[hi]
            int mid = lo + (hi - lo) / 2;

            int a = input[lo];
            int b = input[mid];
            int c = input[hi];

            // Return index of median value
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

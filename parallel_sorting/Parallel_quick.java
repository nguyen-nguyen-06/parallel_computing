package parallel_computing.parallel_sorting;

import java.io.*;
import java.util.*;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveAction;
import java.util.concurrent.RecursiveTask;

public class Parallel_quick extends Sorting{
    static ForkJoinPool POOL = new ForkJoinPool();
    static int CUTOFF;
    static int CUTOFFQUICK;

    public Parallel_quick(String fileName) throws FileNotFoundException{
        super(fileName);
    }

    public Parallel_quick(int[] list){
        super(list);
    }

    public int[] sort(){
        int[] input = getList();
        if (input.length <= 1){
            return input;
        } 
        int[] output = new int[input.length];
        POOL.invoke(new ParallelQuickTask(input, output, 0));
        return output;
    }

    public static int[] parallelPrefixSum(int[] list){
        Node root = POOL.invoke(new BuildSumTree(list, 0, list.length - 1));
        int[] prefixSum = new int[list.length];
        POOL.invoke(new PopulatePrefixSum(list, prefixSum, root, false, null, null));
        return prefixSum;
    }

    //This not in-place quick sort algo have memory complexity of O(nlogn) or may be O(n^2)
    //Trade off with speed up of n/c.P < speed up < n/logn (with c is a large constant, P is the number of processors) 
    //Only good when we have large number of processor, very large memory and large input number
    public class ParallelQuickTask extends RecursiveAction{
        private int[] input;
        public int[] output;
        public int offset; //nums of element in front in output

        public ParallelQuickTask(int[] input, int[] output, int offset) {
            this.input = input;
            this.output = output;
            this.offset = offset;
        }

        public void compute(){
            if(input.length <= CUTOFFQUICK){
                Arrays.sort(input);
                System.arraycopy(input, 0, output, offset, input.length);
            } else{
                int pivotVal = input[input.length - 1];
                int[] lowerEqual = new int[input.length - 1]; //exclude the pivot
                int[] greater = new int[input.length - 1]; //exclude the pivot

                POOL.invoke(new LowerEqualGreaterMap(input, lowerEqual, greater, pivotVal, 0, input.length - 2)); //map everything except the pivot

                int[] indexLeftPrefix = parallelPrefixSum(lowerEqual);
                int[] indexRightPrefix = parallelPrefixSum(greater);
                int pivotNewPosition = indexLeftPrefix[indexLeftPrefix.length - 1];
                int[] leftLower = new int[pivotNewPosition];
                int[] rightGreater = new int[input.length - 1 - pivotNewPosition];
                
                POOL.invoke(new ParallelFilter(input, lowerEqual, indexLeftPrefix, leftLower, 0, indexLeftPrefix.length - 1));
                output[pivotNewPosition + offset] = pivotVal;
                POOL.invoke(new ParallelFilter(input, greater, indexRightPrefix, rightGreater, 0, indexRightPrefix.length - 1));

                ParallelQuickTask leftTask = new ParallelQuickTask(leftLower, output, offset);
                ParallelQuickTask rightTask = new ParallelQuickTask(rightGreater, output, offset + pivotNewPosition + 1);

                leftTask.fork();
                rightTask.compute();
                leftTask.join();
            }
        }
    }

    private static class Node{
        public int lo;
        public int hi; //inclusive
        public int sum;
        public int leftSum;
        public Node left;
        public Node right;

        public Node(int lo, int hi, int sum){
            this.lo = lo;
            this.hi = hi;
            this.sum = sum;
        }

    }

    private static class BuildSumTree extends RecursiveTask<Node>{
        private int[] input;
        private int lo;
        private int hi;

        public BuildSumTree(int[] input, int lo, int hi){
            this.input = input;
            this.lo = lo;
            this.hi = hi;
        }

        public Node compute(){
            if(hi - lo <= CUTOFF){
                int sum = 0;
                for(int i = lo; i <= hi; i++){
                    sum += input[i];
                }
                return new Node(lo, hi, sum);
            } else {
                int mid = lo + (hi - lo)/2;
                BuildSumTree leftTree = new BuildSumTree(input, lo, mid);
                BuildSumTree rightTree = new BuildSumTree(input, mid + 1, hi);

                leftTree.fork();
                Node rightNode = rightTree.compute();
                Node leftNode = leftTree.join();
                Node parent = new Node(lo, hi, leftNode.sum + rightNode.sum);
                parent.left = leftNode;
                parent.right = rightNode;
                return parent;
            }
        }
    }

    private static class PopulatePrefixSum extends RecursiveAction{
        private int[] input;
        private int[] output;
        private int lo;
        private int hi; // inclusive
        private Node root;
        private boolean isLeftChild;
        private Node parent;
        private Node sibling;

        public PopulatePrefixSum(int[] input, int[] output, Node root, boolean isLeftChild, Node parent, Node sibling){
            this.input = input;
            this.output = output;
            this.lo = root.lo;
            this.hi = root.hi;
            this.root = root;
            this.parent = parent;
            this.sibling = sibling;
            this.isLeftChild = isLeftChild;
        }

        public void compute(){
            // fill in the left sum for each node
            if(parent == null){
                root.leftSum = 0;
            }
            else if(isLeftChild){
                root.leftSum = parent.leftSum;
            } else{
                root.leftSum = parent.leftSum + sibling.sum;
            }
            if(root.left == null && root.right == null){
                int sum = root.leftSum;
                for(int i = lo; i <= hi; i++){
                    sum += input[i];
                    output[i] = sum;
                }
            }else{
                PopulatePrefixSum leftSubtree = new PopulatePrefixSum(input, output, root.left, true, root, root.right);
                leftSubtree.fork();
                PopulatePrefixSum rightSubtree = new PopulatePrefixSum(input, output, root.right, false, root, root.left);
                rightSubtree.compute();
                leftSubtree.join();
            }
        }
    }

    private static class LowerEqualGreaterMap extends RecursiveAction{
        private int[] input;
        private int[] lowerEqual;
        private int[] greater;
        private int val;
        private int lo;
        private int hi;

        public LowerEqualGreaterMap(int[] input, int[] lowerEqual, int[] greater, int val, int lo, int hi){
            this.input = input;
            this.lowerEqual = lowerEqual;
            this.greater = greater;
            this.val = val;
            this.lo = lo;
            this.hi = hi;
        }

        public void compute(){
            if(hi - lo <= CUTOFF){
                for(int i = lo; i <= hi; i++){
                    if(input[i] <= val){
                        lowerEqual[i] = 1;
                        greater[i] = 0;
                    } else {
                        lowerEqual[i] = 0;
                        greater[i] = 1;
                    }
                }
            } else {
                int mid = lo + (hi - lo)/2;
                LowerEqualGreaterMap leftTask = new LowerEqualGreaterMap(input, lowerEqual, greater, val, lo, mid);
                LowerEqualGreaterMap rightTask = new LowerEqualGreaterMap(input, lowerEqual, greater, val, mid + 1, hi);
                leftTask.fork();
                rightTask.compute();
                leftTask.join();
            }
        }
    }

    public static class ParallelFilter extends RecursiveAction{
        public int[] input;
        public int[] bitVector;
        public int[] indexPrefix;
        public int[] filter;
        public int lo;
        public int hi;

        public ParallelFilter(int[] input, int[] bitVector, int[] indexPrefix, int[] filter, int lo, int hi){
            this.input = input;
            this.bitVector = bitVector;
            this.indexPrefix = indexPrefix;
            this.filter = filter;
            this.lo = lo; 
            this.hi = hi; 
        }

        public void compute(){
            if(hi - lo <= CUTOFF){
                for(int i = lo; i <= hi; i++){
                    if(bitVector[i] == 1){
                        filter[indexPrefix[i] - 1] = input[i];
                    }
                }
            } else {
                int mid = lo + (hi - lo)/2;
                ParallelFilter leftTask = new ParallelFilter(input, bitVector, indexPrefix, filter, lo, mid);
                ParallelFilter rightTask = new ParallelFilter(input, bitVector, indexPrefix, filter, mid + 1, hi);
                leftTask.fork();
                rightTask.compute();
                leftTask.join();
            }
        }
    }
}


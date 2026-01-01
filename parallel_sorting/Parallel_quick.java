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
        int[] list = getList();
        if (list.length <= 1){
            return list;
        } 
        POOL.invoke(new ParallelQuickTask(list, 0, list.length - 1));
        return list;
    }

    public class ParallelQuickTask extends RecursiveAction{
        private int[] input;
        private int lo;
        private int hi;  // inclusive

        public ParallelQuickTask(int[] input, int lo, int hi) {
            this.input = input;
            this.lo = lo;
            this.hi = hi;
        }

        public void compute(){
            if(hi - lo <= CUTOFFQUICK){
                Arrays.sort(input, lo, hi + 1);
            } else{
                int pivotVal = input[hi];
                int[] lowerEqual = new int[hi - lo];//exclude the pivot 
                int[] greater = new int[hi - lo]; //exclude the pivot
                POOL.invoke(new LowerEqualGreaterMap(input, lowerEqual, greater, pivotVal, lo, hi - 1)); //exclude the pivot
                int[] indexLeftHalf = parallelPrefixSum(lowerEqual);
                int[] indexRightHalf = parallelPrefixSum(greater);
                int[] output = new int[input.length];
                int leftHalfSize = indexLeftHalf[indexLeftHalf.length - 1];
                int pivotPosition = leftHalfSize + 1;
                POOL.invoke(new LowerEqualFilter(input, lowerEqual, indexLeftHalf, output, lo, hi, lo));
                POOL.invoke(new GreaterFilter(input, greater, indexRightHalf, output, lo, hi, hi));
                //how about 
                //POOL.invoke(new LowerEqualFilter(input, lowerEqual, indexLeftHalf, output, lo, hi -1, lo));
                //POOL.invoke(new GreaterFilter(input, greater, indexRightHalf, output, lo, hi - 1, hi)); still hi because offset from the pivot
                //lo to hi - 1 because lowerEqual made from new int[hi - lo]
                output[pivotPosition] = pivotVal;
                input = output;
                //
                //1 bug here input is shared but output is not
                //
                ParallelQuickTask leftTask = new ParallelQuickTask(input, lo, pivotPosition - 1);
                ParallelQuickTask rightTask = new ParallelQuickTask(input, pivotPosition + 1, hi);
                //How about delete input = output and use 
                //ParallelQuickTask leftTask = new ParallelQuickTask(output, lo, pivotPosition - 1);
                //ParallelQuickTask rightTask = new ParallelQuickTask(outputput, pivotPosition + 1, hi);
                leftTask.fork();
                rightTask.compute();
                leftTask.join();
            }
        }
    }

    //correct so far
    public static int[] parallelPrefixSum(int[] list){
        Node root = POOL.invoke(new BuildSumTree(list, 0, list.length - 1));
        int[] prefixSum = new int[list.length];
        POOL.invoke(new PopulatePrefixSum(list, prefixSum, root, false, null, null));
        return prefixSum;
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

    // 0 0 1 0 0 0 0 1 0 1(pivot is left outside) ---PrefixSum---> 0 0 1 1 1 1 1 1 2 2 -------> map start + 0 start + 1 start + 2 in output
    // -1 -1 0 -1 -1 -1 -1 0 -1 ----PrefixSum----> -1 -2 -2 -3 -4 -5 -6 -6 -7 -----> map end - 0, end -1 ... in output
    //then move pivot 
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
                        greater[i] = -1;
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

    // 0 0 1 0 0 0 0 1 0 1(pivot is left outside) ---PrefixSum---> 0 0 1 1 1 1 1 1 2 2 -------> map start + 0 start + 1 start + 2 in output
    // -1 -1 0 -1 -1 -1 -1 0 -1 ----PrefixSum----> -1 -2 -2 -3 -4 -5 -6 -6 -7 -----> map end, end -1 ... in output
    //then move pivot 
    private static class LowerEqualFilter extends RecursiveAction{
        private int[] input;
        private int[] lowerEqual;
        private int[] indexLeftHalf;
        private int[] output;
        private int lo;
        private int hi;
        private int start;

        public LowerEqualFilter(int[] input, int[] lowerEqual, int[] indexLeftHalf, int[] output, int lo, int hi, int start){
            this.input = input;
            this.lowerEqual = lowerEqual;
            this.indexLeftHalf = indexLeftHalf;
            this.output = output;
            this.lo = lo; // lo and hi is range 
            this.hi = hi;
            this.start = start; //offset 
        }

        public void compute(){
            if(hi - lo <= CUTOFF){
                for(int i = lo; i <= hi; i++){
                    if(lowerEqual[i] != 0){
                        output[start + indexLeftHalf[i] - 1] = input[start + i];
                    }
                }
            } else {
                int mid = lo + (hi - lo)/2;
                LowerEqualFilter leftTask = new LowerEqualFilter(input, lowerEqual, indexLeftHalf, output, lo, mid, start);
                LowerEqualFilter rightTask = new LowerEqualFilter(input, lowerEqual, indexLeftHalf, output, mid, hi, start);
                leftTask.fork();
                rightTask.compute();
                leftTask.join();
            }
        }
    }

    private static class GreaterFilter extends RecursiveAction{
        private int[] input;
        private int[] greater;
        private int[] indexRightHalf;
        private int[] output;
        private int lo;
        private int hi;
        private int end;

        public GreaterFilter(int[] input, int[] greater, int[] indexRightHalf, int[] output, int lo, int hi, int end){
            this.input = input;
            this.greater = greater;
            this.indexRightHalf = indexRightHalf;
            this.output = output;
            this.lo = lo;
            this.hi = hi;
            this.end = end;
        }

        public void compute(){
            if(hi - lo <= CUTOFF){
                for(int i = lo; i <= hi; i++){
                    if(greater[i] != 0){
                        output[end + indexRightHalf[i] + 1] = input[end - i];
                    }
                }
            } else {
                int mid = lo + (hi - lo)/2;
                GreaterFilter leftTask = new GreaterFilter(input, greater, indexRightHalf, output, lo, mid, end);
                GreaterFilter rightTask = new GreaterFilter(input, greater, indexRightHalf, output, mid, hi, end);
                leftTask.fork();
                rightTask.compute();
                leftTask.join();
            }
        }
    }
}


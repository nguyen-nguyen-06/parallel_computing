package parallel_computing.parallel_sorting;

import java.io.*;
import java.util.*;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveAction;
import java.util.concurrent.RecursiveTask;

/**
 * Parallel quicksort implementation using parallel prefix sums for partitioning.
 * Then parallelizes partitioning the left partioned part and right partitioned part.
 * 
 * Compare to Parallel_simple_quick, this algorithm parallelizes both the partition step
 *  (using map, prefix sum, and filter) and the recursive sorting of subarrays. 
 * 
 * Time complexity: O(n log n) work 
 * Span: O(log^2 n) with parallel task
 * 
 * Trade-offs:
 * - Memory: non-in-place partitioning
 * - Speedup: Between n/(c*P) and n/log(n) where c is overhead constant, P is processor count
 * - Best for: Large processor counts, large memory, large input sizes
 * 
 * @specfield POOL : ForkJoinPool // shared thread pool
 * @specfield CUTOFFPARTITION : int // threshold for sequential prefix sum operations
 * @specfield CUTOFFQUICK : int // threshold for sequential sorting
 */

public class Parallel_quick extends Sorting{
    static ForkJoinPool POOL = new ForkJoinPool();
    static int CUTOFFPARTITION;
    static int CUTOFFQUICK;

    /**
     * Constructs a Parallel_quick sorter by reading from a file.
     * 
     * @param fileName path to file containing the array
     * @throws FileNotFoundException if file does not exist
     */
    public Parallel_quick(String fileName) throws FileNotFoundException{
        super(fileName);
    }

    /**
     * Constructs a Parallel_quick sorter with the given array.
     * 
     * @param list the array to sort
     * @requires list != null
     */
    public Parallel_quick(int[] list){
        super(list);
    }

    /**
     * Sorts the array using parallel quicksort with parallel prefix sum for partitioning.
     * 
     * @return a new sorted array
     * @requires CUTOFFPARTITION and CUTOFFQUICK are set appropriately
     * @effects spawns parallel tasks for partitioning and recursive sorting
     */
    public int[] sort(){
        int[] input = getList();
        if (input.length <= 1){
            return input;
        } 
        int[] output = new int[input.length];
        POOL.invoke(new ParallelQuickTask(input, output, 0));
        return output;
    }

    /**
     * Computes the inclusive prefix sum of the input array in parallel.
     * Builds a sum tree and populates prefix sums using parallel tasks
     * @param list the input array
     * @return a new array where result[i] = sum of list[0..i]
     * @requires list != null
     */
    private int[] parallelPrefixSum(int[] list){
        Node root = POOL.invoke(new BuildSumTree(list, 0, list.length - 1));
        int[] prefixSum = new int[list.length];
        POOL.invoke(new PopulatePrefixSum(list, prefixSum, root, false, null, null));
        return prefixSum;
    }

    /**
     * A RecursiveAction that performs parallel quicksort on a subarray.
     * Uses parallel prefix sum to partition elements around the pivot,
     * then forks left and right subtasks for recursive sorting.
     * 
     * @specfield input : int[] // the subarray to sort
     * @specfield output : int[] // the destination array for sorted results
     * @specfield offset : int // starting position in output array
     */
    public class ParallelQuickTask extends RecursiveAction{
        private int[] input;
        public int[] output;
        public int offset; //nums of element in front in output

        /**
         * Constructs a ParallelQuickTask for sorting input into output.
         * 
         * @param input the subarray to sort
         * @param output the destination array
         * @param offset starting position in output for this subarray's sorted result
         */
        public ParallelQuickTask(int[] input, int[] output, int offset) {
            this.input = input;
            this.output = output;
            this.offset = offset;
        }

        /**
         * Executes parallel quicksort partitioning and recursive sorting.
         * 
         * @requires input.length > 0 if called on non-empty partition
         * @effects if below cutoff, sorts sequentially; otherwise uses parallel prefix sum
         *          to partition and forks left/right subtasks
         */
        public void compute(){
            if(input.length <= CUTOFFQUICK){
                Arrays.sort(input);
                System.arraycopy(input, 0, output, offset, input.length);
            } else{

                int pivotPosition = medianOfThree(0, input.length -1);
                swap(pivotPosition, input.length - 1); //swap pivot to the end of input arr
                
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

        /**
         * Selects pivot index using median-of-three strategy.
         * 
         * @param start starting index
         * @param end ending index
         * @return index of median among input[start], input[mid], input[end]
         */
        private int medianOfThree(int start, int end){
            // Median of input[start], input[mid], input[end]
            int mid = start + (end - start) / 2;

            int a = input[start];
            int b = input[mid];
            int c = input[end];

            // Return index of median value
            if ((a <= b && b <= c) || (c <= b && b <= a)) {
                return mid;
            } else if ((b <= a && a <= c) || (c <= a && a <= b)) {
                return start;
            } else {
                return end;
            }
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
    }

    /**
     * ==================== PARALLEL PARTITION OVERVIEW ====================
     * Partitions an array around a pivot value using three parallel primitives.
     * Unlike sequential partitioning, this approach parallelizes the entire
     * partition step at the cost of additional memory.
     * 
     * Three-step algorithm:
     * 
     *   Step 1 - Bit Vector Mapping (LowerEqualGreaterMap):
     *     - Input: array, pivot value
     *     - Output: two bit vectors (lowerEqual, greater)
     *     - Each element is classified as <= pivot or > pivot
     * 
     *   Step 2 - Parallel Prefix Sum:
     *     - Input: bit vectors from Step 1
     *     - Output: prefix sum arrays (indexLeftPrefix, indexRightPrefix)
     *     - Computes destination index for each element in the partitioned output
     * 
     *   Step 3 - Parallel Filter:
     *     - Input: original array, bit vectors, prefix sums
     *     - Output: two partitioned arrays (leftLower, rightGreater)
     *     - Scatters elements to their final positions based on prefix sum indices
     * 
     * 
     * Trade-off: Sacrifices memory efficiency for parallelism in the partition step.
     */
    
    /**
     * ==================== BIT VECTOR MAPPING ====================
     * Creates two complementary bit vectors that classify elements relative to a pivot value.
     * This is the "map" step of the parallel partition operation.
     * 
     * Algorithm:
     *   For each element input[i] in parallel:
     *     - If input[i] <= pivot: set lowerEqual[i] = 1, greater[i] = 0
     *     - If input[i] > pivot:  set lowerEqual[i] = 0, greater[i] = 1
     * 
     * Purpose:
     *   These bit vectors serve as selection masks for the filter operation.
     *   When combined with prefix sums, they determine the final position of each element
     *   in the partitioned output array.
     * 
     * 
     * Complexity:
     *   Work: O(n)
     *   Span: O(log n) with parallel divide-and-conquer
     * 
     * @param input the source array to classify
     * @param lowerEqual output bit vector for elements <= pivot
     * @param greater output bit vector for elements > pivot
     * @param val the pivot value for comparison
     * ================================================================================
     */

    /**
     * A RecursiveAction that maps each element to a bit vector based on comparison with pivot.
     * Creates two parallel arrays: lowerEqual[i]=1 if input[i]<=val, greater[i]=1 otherwise.
     * Used to prepare for parallel filter operation in quicksort partitioning.
     * 
     * @specfield input : int[] // source array
     * @specfield lowerEqual : int[] // bit vector for elements <= pivot
     * @specfield greater : int[] // bit vector for elements > pivot
     * @specfield val : int // the pivot value
     * @specfield lo, hi : int // range to process
     */
    private static class LowerEqualGreaterMap extends RecursiveAction{
        private int[] input;
        private int[] lowerEqual;
        private int[] greater;
        private int val;
        private int lo;
        private int hi;

        /**
         * Constructs a LowerEqualGreaterMap task.
         * 
         * @param input the source array
         * @param lowerEqual destination for <= pivot indicators
         * @param greater destination for > pivot indicators
         * @param val the pivot value
         * @param lo starting index (inclusive)
         * @param hi ending index (inclusive)
         */
        public LowerEqualGreaterMap(int[] input, int[] lowerEqual, int[] greater, int val, int lo, int hi){
            this.input = input;
            this.lowerEqual = lowerEqual;
            this.greater = greater;
            this.val = val;
            this.lo = lo;
            this.hi = hi;
        }

        public void compute(){
            if(hi - lo <= CUTOFFPARTITION){
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

    /**
     * ==================== PARALLEL PREFIX SUM ====================
     * Computes the prefix sum of an array in parallel.
     * 
     * Algorithm:
     *   Phase 1 - Up-sweep (BuildSumTree):
     *     - Recursively build a binary segment tree
     *     - Each node stores the sum of elements in its range [lo, hi]
     *     - Leaves store partial sums; internal nodes store sum of children
     *   
     *   Phase 2 - Down-sweep (PopulatePrefixSum):
     *     - Traverse tree from root to leaves
     *     - Each node computes leftSum = sum of all elements before its range
     *     - Left child inherits parent's leftSum
     *     - Right child's leftSum = parent's leftSum + left sibling's sum
     *     - Leaves compute final prefix sums: output[i] = leftSum + sum(input[lo..i])
     * 
     * Complexity:
     *   Work: O(n)
     *   Span: O(log n)
     * 
     * @param list the input array
     * @return array where result[i] = sum of list[0..i] (inclusive)
     * 
     * 
     * ==================================================================
     */

    /**
     * Represents a node in the parallel prefix sum segment tree.
     * 
     * @specfield lo : int // starting index of range (inclusive)
     * @specfield hi : int // ending index of range (inclusive)
     * @specfield sum : int // sum of all elements in [lo, hi]
     * @specfield leftSum : int // sum of all elements to the left of this node's range
     * @specfield left : Node // left child (null for leaf nodes)
     * @specfield right : Node // right child (null for leaf nodes)
     */
    private static class Node{
        public int lo;
        public int hi; 
        public int sum;
        public int leftSum;
        public Node left;
        public Node right;

        /**
         * Constructs a Node for the given range with the specified sum.
         * 
         * @param lo starting index (inclusive)
         * @param hi ending index (inclusive)
         * @param sum the sum of elements in [lo, hi]
         */
        public Node(int lo, int hi, int sum){
            this.lo = lo;
            this.hi = hi;
            this.sum = sum;
        }

    }

    /**
     * A RecursiveTask that builds a segment tree where each node stores the sum of its range.
     * Used as the first phase of parallel prefix sum computation.
     * 
     * @specfield input : int[] // the array to build the tree from
     * @specfield lo, hi : int // the range this task is responsible for
     */
    private static class BuildSumTree extends RecursiveTask<Node>{
        private int[] input;
        private int lo;
        private int hi;

        /**
         * Constructs a BuildSumTree task for the given range.
         * 
         * @param input the source array
         * @param lo starting index (inclusive)
         * @param hi ending index (inclusive)
         */
        public BuildSumTree(int[] input, int lo, int hi){
            this.input = input;
            this.lo = lo;
            this.hi = hi;
        }

        /**
         * Builds a segment tree node representing the sum of input[lo..hi].
         * 
         * @return the root Node for this range
         * @effects if below cutoff, creates leaf node with sequential sum;
         *          otherwise forks children and combines their sums
         */
        public Node compute(){
            if(hi - lo <= CUTOFFPARTITION){
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

    /**
     * A RecursiveAction that computes prefix sums by traversing the segment tree.
     * Using the leftSum values propagated from parent nodes to compute final prefix sums.
     * 
     * @specfield input : int[] // source array
     * @specfield output : int[] // destination for prefix sums
     * @specfield root : Node // current node in the segment tree
     * @specfield isLeftChild : boolean // whether this node is a left child
     * @specfield parent, sibling : Node // parent and sibling nodes for leftSum computation
     */
    private static class PopulatePrefixSum extends RecursiveAction{
        private int[] input;
        private int[] output;
        private int lo;
        private int hi; 
        private Node root;
        private boolean isLeftChild;
        private Node parent;
        private Node sibling;

        /**
         * Constructs a PopulatePrefixSum task for traversing a subtree.
         * 
         * @param input the source array
         * @param output the destination array for prefix sums
         * @param root the current node
         * @param isLeftChild true if this node is the left child of parent
         * @param parent the parent node (null for root)
         * @param sibling the sibling node (null for root)
         */
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

        /**
         * Populates prefix sums by traversing the sum segmenttree.
         * 
         * @effects computes leftSum for this node based on parent and sibling;
         *          if leaf, fills output[lo..hi] with cumulative prefix sums;
         *          otherwise forks children tasks
         * @modifies output[lo..hi], root.leftSum
         */
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
                PopulatePrefixSum rightSubtree = new PopulatePrefixSum(input, output, root.right, false, root, root.left);
                leftSubtree.fork();
                rightSubtree.compute();
                leftSubtree.join();
            }
        }
    }

    /**
     * ==================== BIT VECTOR MAPPING ====================
     * Creates two complementary bit vectors that classify elements relative to a pivot value.
     * This is the "map" step of the parallel partition operation.
     * 
     * Algorithm:
     *   For each element input[i] in parallel:
     *     - If input[i] <= pivot: set lowerEqual[i] = 1, greater[i] = 0
     *     - If input[i] > pivot:  set lowerEqual[i] = 0, greater[i] = 1
     * 
     * Purpose:
     *   These bit vectors serve as selection masks for the filter operation.
     *   When combined with prefix sums, they determine the final position of each element
     *   in the partitioned output array.
     * 
     * 
     * Complexity:
     *   Work: O(n)
     *   Span: O(log n) with parallel divide-and-conquer
     * 
     * @param input the source array to classify
     * @param lowerEqual output bit vector for elements <= pivot
     * @param greater output bit vector for elements > pivot
     * @param val the pivot value for comparison
     * ================================================================================
     */


    /**
     * ==================== PARALLEL FILTER ====================
     * Performs parallel filtering on discontiguous selected elements to contiguous positions.
     * This is the final step of parallel partition, placing elements in their sorted positions.
     * 
     * Algorithm:
     *   For each index i in parallel:
     *     - If bitVector[i] == 1 (element is selected):
     *       - Destination index = indexPrefix[i] - 1 (prefix sum gives 1-based position)
     *       - filter[destination] = input[i]
     *     - If bitVector[i] == 0: skip (element belongs to other partition)
     * 
     * How it works with prefix sum:
     *   - bitVector marks which elements to include (1) or exclude (0)
     *   - indexPrefix[i] = count of 1s in bitVector[0..i] (inclusive prefix sum)
     *   - Therefore indexPrefix[i] - 1 gives the 0-based destination index
     *   - Elements are scattered to contiguous positions without gaps
     * 
     * Example:
     *   input:       [5, 2, 8, 1, 9]
     *   bitVector:   [1, 1, 0, 1, 0]  (selecting elements <= pivot=5)
     *   indexPrefix: [1, 2, 2, 3, 3]
     *   filter:      [5, 2, 1]        (elements at positions 0, 1, 2)
     * 
     * Complexity:
     *   Work: O(n)
     *   Span: O(log n) with parallel divide-and-conquer
     * 
     * @param input the source array
     * @param bitVector selection mask (1 = include, 0 = exclude)
     * @param indexPrefix inclusive prefix sum of bitVector
     * @param filter destination array for selected elements
     */

    /**
     * A RecursiveAction that filters elements to their final sorted positions.
     * Uses the bit vector and prefix sum to scatter elements: if bitVector[i]==1,
     * then input[i] is placed at filter[indexPrefix[i]-1].
     * 
     * @specfield input : int[] // source array
     * @specfield bitVector : int[] // selection indicator (1 = include)
     * @specfield indexPrefix : int[] // prefix sum of bitVector
     * @specfield filter : int[] // destination array
     * @specfield lo, hi : int // range to process
     */
    public static class ParallelFilter extends RecursiveAction{
        public int[] input;
        public int[] bitVector;
        public int[] indexPrefix;
        public int[] filter;
        public int lo;
        public int hi;

        /**
         * Constructs a ParallelFilter task.
         * 
         * @param input the source array
         * @param bitVector selection indicators
         * @param indexPrefix prefix sum of bitVector
         * @param filter destination array
         * @param lo starting index (inclusive)
         * @param hi ending index (inclusive)
         */
        public ParallelFilter(int[] input, int[] bitVector, int[] indexPrefix, int[] filter, int lo, int hi){
            this.input = input;
            this.bitVector = bitVector;
            this.indexPrefix = indexPrefix;
            this.filter = filter;
            this.lo = lo; 
            this.hi = hi; 
        }

        /**
         * Scatters selected elements to their final positions using prefix sum indices.
         * 
         * @effects for each i in [lo, hi] where bitVector[i]==1: filter[indexPrefix[i]-1] = input[i]
         * @modifies filter
         */
        public void compute(){
            if(hi - lo <= CUTOFFPARTITION){
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


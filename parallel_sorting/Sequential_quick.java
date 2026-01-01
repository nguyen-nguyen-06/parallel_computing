package parallel_computing.parallel_sorting;

import java.io.*;

public class Sequential_quick extends Sorting{

    public Sequential_quick(String fileName) throws FileNotFoundException{
        super(fileName);
    }

    public Sequential_quick(int[] list){
        super(list);
    }

    public int[] sort(){
        int[] list = getList();
        sort(getList(), 0, list.length - 1);
        return list;
    }

    //start and end are inclusive
    //in-place sorting
    private void sort(int[] list, int start, int end){
        if(start < end){
            int ptr1 = start - 1;
            int ptr2 = start;
            int pivot = medianOfThree(list, start, end);
            int pivotVal = list[pivot];
            swap(list, pivot, end);
            while(ptr2 <= end){
                if(list[ptr2] <= pivotVal){
                    swap(list, ptr1 + 1, ptr2);
                    ptr1++;
                } 
                ptr2++;
            }
            sort(list, start, ptr1 - 1);
            sort(list, ptr1 + 1, end);
        }
    }

    private void swap(int[] list, int a, int b){
        int temp = list[a];
        list[a] = list[b];
        list[b] = temp;
    }

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


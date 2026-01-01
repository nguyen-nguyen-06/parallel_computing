package parallel_computing.parallel_sorting;

import java.io.*;

public class Sequential_merge extends Sorting {
    public Sequential_merge(int[] list){
        super(list);
    }

    public Sequential_merge(String fileName) throws FileNotFoundException{
        super(fileName);
    }

    public int[] sort(){
        int[] list = getList();
        if (list.length <= 1){
            return list;
        } 
        int[] temp = new int[list.length];
        divide(list, 0, list.length - 1, temp);
        return list;
    }


    private void divide(int[] list, int start, int end, int[] temp){
        if (start < end){
            int mid = start + (end - start) / 2;

            divide(list, start, mid, temp);
            divide(list, mid + 1, end, temp);

            merge(list, start, mid, end, temp);
        }
    }

    private void merge(int[] list, int start, int mid, int end, int[] temp){
        // Copy to temp array
        for (int i = start; i <= end; i++) {
            temp[i] = list[i];
        }

        int i = start;      // left half pointer
        int j = mid + 1;    // right half pointer
        int k = start;      // write pointer

        while (i <= mid && j <= end) {
            if (temp[i] <= temp[j]) {
                list[k] = temp[i];
                k++;
                i++;
            } else {
                list[k] = temp[j];
                k++;
                j++;
            }
        }

        // Copy remaining left half (right half already in place)
        while (i <= mid) {
            list[k] = temp[i];
            k++;
            i++;
        }
    }
}

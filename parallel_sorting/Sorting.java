package parallel_computing.parallel_sorting;

import java.util.*;
import java.io.*;

public abstract class Sorting{
    private int[] list;

    public Sorting (String fileName) throws FileNotFoundException{
        Scanner in = new Scanner(new File(fileName));
        int size = Integer.parseInt(in.next());
        list = new int[size];
        int index = 0;
        while(in.hasNext()){
            list[index] = Integer.parseInt(in.next());
            index++;
        }

    }

    public Sorting(int[] list){
        this.list = list;
    }

    public int[] getList(){
        return list;
    }

    public abstract int[] sort();
}


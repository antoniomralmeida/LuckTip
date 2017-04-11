package br.com.opencare.lucktip;

import java.util.ArrayList;
import java.util.Random;

/**
 * Created by manoel.ribeiro on 23/03/2017.
 */

public class Predict {
    //private int[][] predict;
    private ArrayList<ArrayList<Integer>> predict = new ArrayList<ArrayList<Integer>>();
    private ArrayList<Integer> index = new ArrayList<Integer>();
    //private int[] index;
    private int column = 0;

    public Predict() {
    }

    public void reset() {
        Random r = new Random();
        column = r.nextInt(predict.size());

        //index = new int[predict.length];
        index = new ArrayList<Integer>();

        for (int i = 0; i < predict.size(); i++)
            index.add(0);
    }

    public void setPredict(ArrayList<ArrayList<Integer>> p) {
        predict = p;
    }

    public int next() {
        if (column >= predict.size())
            column = 0;
        if (index.get(column) >= predict.get(column).size())
            index.set(column, 0);

            int p = predict.get(column).get(index.get(column));
            index.set(column, index.get(column)+1);
            column++;
            return p;
    }
}
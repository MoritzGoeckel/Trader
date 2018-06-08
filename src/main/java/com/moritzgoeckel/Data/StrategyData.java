package com.moritzgoeckel.Data;

import java.util.HashMap;
import java.util.LinkedList;

public class StrategyData {

    private HashMap<String, LinkedList<Double>> data = new HashMap<>();
    private HashMap<String, Integer> axisGroups = new HashMap<>();

    public HashMap<String, LinkedList<Double>> getData() {
        return data;
    }

    public int getAxisGroup(String name){
        return axisGroups.get(name);
    }

    public StrategyData(String[] names, int[] axisGroups){
        for(int i = 0; i < names.length; i++) {
            data.put(names[i], new LinkedList<>());
            this.axisGroups.put(names[i], axisGroups[i]);
        }
    }

    public void addData(String[] names, double[] values){
        if(names.length != values.length)
            throw new RuntimeException("Names and values have different length!");

        for(int i = 0; i < names.length; i++)
            data.get(names[i]).addLast(values[i]);
    }
}

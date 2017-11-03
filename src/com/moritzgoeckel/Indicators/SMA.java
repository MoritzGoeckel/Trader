package com.moritzgoeckel.Indicators;

import java.util.LinkedList;
import java.util.List;

public class SMA implements RollingIndicator {

    private int timeframe;
    private List<Double> valueBuffer = new LinkedList<>();
    private double sum = 0;

    public SMA(int timeframe){
        this.timeframe = timeframe;
    }

    @Override
    public double getNext(double value) {
        sum += value;
        valueBuffer.add(value);

        while (valueBuffer.size() > timeframe)
            sum -= valueBuffer.remove(0);

        return get();
    }

    @Override
    public double get() {
        int size = valueBuffer.size();
        if(size == timeframe)
            return sum / size;
        else
            throw new RuntimeException("Not ready yet: " + size + " / " + timeframe);
    }
}

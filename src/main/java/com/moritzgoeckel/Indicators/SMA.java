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
    public void pushValue(double value) {
        sum += value;
        valueBuffer.add(value);

        while (valueBuffer.size() > timeframe)
            sum -= valueBuffer.remove(0);
    }

    @Override
    public boolean isReady() {
        return valueBuffer.size() == timeframe;
    }

    @Override
    public double getIndicatorValue() {
        if(isReady())
            return sum / valueBuffer.size();
        else
            throw new RuntimeException("Not ready yet: " + valueBuffer.size() + " / " + timeframe);
    }
}

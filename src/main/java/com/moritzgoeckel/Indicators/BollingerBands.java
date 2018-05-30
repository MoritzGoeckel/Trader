package com.moritzgoeckel.Indicators;

import org.tools4j.meanvar.MeanVarianceSlidingWindow;

public class BollingerBands implements RollingIndicator {


    public enum BollingerState{
        Higher,
        Lower,
        Inside
    }

    private final double stdFactor;
    private final int timeframe;
    private final SMA sma;
    private final MeanVarianceSlidingWindow meanVarianceSlidingWindow;

    private double lastValue;

    public BollingerBands(int timeframe, double stdFactor){
        this.timeframe = timeframe;
        this.meanVarianceSlidingWindow = new MeanVarianceSlidingWindow(timeframe);
        this.sma = new SMA(timeframe);
        this.stdFactor = stdFactor;
    }

    @Override
    public void pushValue(double value) {
        sma.pushValue(value);
        meanVarianceSlidingWindow.update(value);
        lastValue = value;
    }

    @Override
    public boolean isReady() {
        return sma.isReady() && meanVarianceSlidingWindow.getCount() == timeframe;
    }

    @Override
    public double getIndicatorValue() {
        if(isReady()) {
            double dist = meanVarianceSlidingWindow.getStdDev() * stdFactor;
            if(lastValue > sma.getIndicatorValue() + dist)
                return BollingerState.Higher.ordinal();

            if(lastValue < sma.getIndicatorValue() - dist)
                return BollingerState.Lower.ordinal();

            return BollingerState.Inside.ordinal();
        }
        else
            throw new RuntimeException("Not ready yet: " + meanVarianceSlidingWindow.getCount() + " / " + timeframe);
    }
}

package com.moritzgoeckel.Indicators;

public interface RollingIndicator {
    void pushValue(double value);
    boolean isReady();
    double getIndicatorValue();
}

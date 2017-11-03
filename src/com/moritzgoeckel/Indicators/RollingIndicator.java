package com.moritzgoeckel.Indicators;

public interface RollingIndicator {
    double getNext(double value);
    double get();
}

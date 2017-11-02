package com.moritzgoeckel.Market;

public interface Market {
    void openPosition(String instrument, double units, PositionType type);
    void closePosition(String instrument);
    PositionType isPositionOpen(String instrument);
}

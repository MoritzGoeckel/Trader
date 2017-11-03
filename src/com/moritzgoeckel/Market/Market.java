package com.moritzgoeckel.Market;

import com.moritzgoeckel.Data.PositionType;

public interface Market {
    void openPosition(String instrument, double units, PositionType type);
    void closePosition(String instrument);
    PositionType isPositionOpen(String instrument);
}

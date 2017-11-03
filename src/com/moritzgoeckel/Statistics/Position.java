package com.moritzgoeckel.Statistics;

import com.moritzgoeckel.Market.PositionType;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

public class Position{
    public double in, out;
    public LocalDateTime timeIn, timeOut;
    public String instrument;

    public PositionType getType() {
        return type;
    }

    public void setType(PositionType type) {
        if(type == PositionType.None)
            throw new RuntimeException("Type None is not allowed in this context");

        this.type = type;
    }

    private PositionType type;

    double getProfit(){
        if(type == PositionType.Buy)
            return out - in;

        if(type == PositionType.Sell)
            return in - out;

        throw new RuntimeException("Position type is invalid, None is in this context not allowed");
    }

    long getDurationSeconds(){
        return timeIn.until(timeOut, ChronoUnit.SECONDS);
    }
}
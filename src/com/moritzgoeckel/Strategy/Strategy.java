package com.moritzgoeckel.Strategy;

import com.moritzgoeckel.Data.Candle;
import com.moritzgoeckel.Market.Market;

import java.time.Duration;
import java.util.Map;

public interface Strategy {
    void setDna(Map<String, Double> dna);
    void candleCompleted(Candle candle, Market market, String instrument);
    int getPreperationTime();
    Map<String, Double> getDna();
    Map<String, Double> getOffspringDna();
    Map<String, Double> getRandomDna();
}

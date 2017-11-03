package com.moritzgoeckel.Strategy;

import com.moritzgoeckel.Data.Candle;
import com.moritzgoeckel.Market.Market;

import java.time.Duration;
import java.util.Map;

public interface Strategy {
    void setDna(StrategyDNA dna);
    void candleCompleted(Candle candle, Market market);
    int getPreperationTime();
    StrategyDNA getDna();
    StrategyDNA getOffspringDna();
    StrategyDNA getRandomDna();
}

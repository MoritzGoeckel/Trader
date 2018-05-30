package com.moritzgoeckel.Strategy;

import com.moritzgoeckel.Data.Candle;
import com.moritzgoeckel.Market.Market;

public interface Strategy {
    void setDna(StrategyDNA dna);
    void candleCompleted(Candle candle, Market market);
    int getPreparationTime();
    StrategyDNA getDna();
    StrategyDNA getOffspringDna(double exploration);

    default StrategyDNA getOffspringDna(){
        return getOffspringDna(1d);
    }

    StrategyDNA getRandomDna();
}

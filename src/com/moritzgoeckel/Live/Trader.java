package com.moritzgoeckel.Live;

import com.moritzgoeckel.Data.Candle;
import com.moritzgoeckel.Market.BacktestMarket;
import com.moritzgoeckel.Market.Market;
import com.moritzgoeckel.Strategy.Strategy;
import com.moritzgoeckel.Strategy.StrategyDNA;

import java.util.List;

public class Trader {

    private Strategy strategy;

    public Trader(StrategyDNA dna, List<Candle> preparationCandles) throws IllegalAccessException, InstantiationException {
        strategy = dna.getStrategyLogic().newInstance();
        strategy.setDna(dna);

        BacktestMarket backtestMarket = new BacktestMarket();

        for(Candle c : preparationCandles){
            backtestMarket.updateCandle(c);
            strategy.candleCompleted(c, backtestMarket);
        }
    }

    private String lastCandleDate = "";
    public void doCandle(Candle c, Market market){
        if(lastCandleDate.equals(c.getTime().toString()))
            throw new RuntimeException("Candle already processed");

        if(!c.getComplete())
            throw new RuntimeException("Candle not yet completed");

        lastCandleDate = c.getTime().toString();
        strategy.candleCompleted(c, market);
    }
}

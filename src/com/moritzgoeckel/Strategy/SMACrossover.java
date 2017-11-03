package com.moritzgoeckel.Strategy;

import com.moritzgoeckel.Data.Candle;
import com.moritzgoeckel.Indicators.SMA;
import com.moritzgoeckel.Market.Market;
import com.moritzgoeckel.Market.PositionType;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

public class SMACrossover implements Strategy {

    private SMA sma_1 = null;
    private SMA sma_2 = null;

    private Map<String, Double> dna = null;

    @Override
    public void setDna(Map<String, Double> dna) {
        this.dna = dna;
        sma_1 = new SMA(dna.get("sma1").intValue());
        sma_2 = new SMA(dna.get("sma2").intValue());
    }

    @Override
    public void candleCompleted(Candle candle, Market market, String instrument) {
        double value = candle.getMid().getC().doubleValue();
        double one = sma_1.getNext(value);
        double two = sma_2.getNext(value);

        PositionType openPosition = market.isPositionOpen(instrument);

        if(one > two && openPosition != PositionType.Buy){
            if(openPosition == PositionType.Sell)
                market.closePosition(instrument);

            market.openPosition(instrument, 1, PositionType.Buy);
        }

        if(one < two && openPosition != PositionType.Sell){
            if(openPosition == PositionType.Buy)
                market.closePosition(instrument);

            market.openPosition(instrument, 1, PositionType.Sell);
        }
    }

    @Override
    public int getPreperationTime() {
        return Math.max(dna.get("sma1").intValue(), dna.get("sma2").intValue());
    }

    @Override
    public Map<String, Double> getDna() {
        return this.dna;
    }

    @Override
    public Map<String, Double> getOffspringDna() {
        int exploration = 20;

        Map<String, Double> nDna = new HashMap<>();
        nDna.put("sma1", Math.random() * exploration - exploration / 2 + dna.get("sma1"));
        nDna.put("sma2", Math.random() * exploration - exploration / 2 + dna.get("sma1"));

        return nDna;
    }

    @Override
    public Map<String, Double> getRandomDna() {
        Map<String, Double> nDna = new HashMap<>();
        nDna.put("sma1", Math.random() * 70d + 1d);
        nDna.put("sma2", Math.random() * 70d + 1d);

        return nDna;
    }
}

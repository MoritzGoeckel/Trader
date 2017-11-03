package com.moritzgoeckel.Strategy;

import com.moritzgoeckel.Data.Candle;
import com.moritzgoeckel.Indicators.SMA;
import com.moritzgoeckel.Market.Market;
import com.moritzgoeckel.Data.PositionType;

public class SMACrossover implements Strategy {

    private SMA sma_1 = null;
    private SMA sma_2 = null;

    private StrategyDNA dna = null;

    @Override
    public void setDna(StrategyDNA dna) {
        this.dna = dna;
        sma_1 = new SMA((int)dna.get("sma1"));
        sma_2 = new SMA((int)dna.get("sma2"));
    }

    @Override
    public void candleCompleted(Candle candle, Market market) {

        double value = candle.getMid().getC().doubleValue();
        sma_1.pushValue(value);
        sma_2.pushValue(value);

        if(sma_1.isReady() && sma_2.isReady()) {
            PositionType openPosition = market.isPositionOpen(candle.getInstrument());

            double one = sma_1.getIndicatorValue();
            double two = sma_2.getIndicatorValue();

            if (one > two && openPosition != PositionType.Buy) {
                if (openPosition == PositionType.Sell)
                    market.closePosition(candle.getInstrument());

                market.openPosition(candle.getInstrument(), 1, PositionType.Buy);
            }

            if (one < two && openPosition != PositionType.Sell) {
                if (openPosition == PositionType.Buy)
                    market.closePosition(candle.getInstrument());

                market.openPosition(candle.getInstrument(), 1, PositionType.Sell);
            }
        }
    }

    @Override
    public int getPreperationTime() {
        return Math.max((int)dna.get("sma1"), (int)dna.get("sma2"));
    }

    @Override
    public StrategyDNA getDna() {
        return this.dna;
    }

    @Override
    public StrategyDNA getOffspringDna() {
        int exploration = 10;

        StrategyDNA nDna = new StrategyDNA(this.getClass());
        nDna.put("sma1", (int)(Math.random() * exploration - exploration / 2 + dna.get("sma1")));
        nDna.put("sma2", (int)(Math.random() * exploration - exploration / 2 + dna.get("sma1")));

        if(nDna.get("sma1") <= 0)
            nDna.put("sma1", 1);

        if(nDna.get("sma2") <= 0)
            nDna.put("sma2", 1);

        return nDna;
    }

    @Override
    public StrategyDNA getRandomDna() {
        StrategyDNA nDna = new StrategyDNA(this.getClass());
        nDna.put("sma1", (int)(Math.random() * 70d + 1d));
        nDna.put("sma2", (int)(Math.random() * 70d + 1d));

        return nDna;
    }
}

package com.moritzgoeckel.Strategy;

import com.moritzgoeckel.Data.Candle;
import com.moritzgoeckel.Data.PositionType;
import com.moritzgoeckel.Indicators.BollingerBands;
import com.moritzgoeckel.Indicators.SMA;
import com.moritzgoeckel.Market.Market;
import com.moritzgoeckel.Util.Formatting;

import java.text.Normalizer;
import java.time.DayOfWeek;

public class BollingerStrategy implements Strategy {

    private StrategyDNA dna = null;
    private BollingerBands bands;
    private boolean reversed;

    @Override
    public void setDna(StrategyDNA dna) {
        this.dna = dna;
        bands = new BollingerBands((int)dna.get("sma"), (int)dna.get("factor"));
        reversed = dna.get("reversed") < 0.5;
    }

    @Override
    public void candleCompleted(Candle candle, Market market) {
        double value = candle.getMid().getC().doubleValue();
        bands.pushValue(value);

        int tradedUnits = (int)(value / 0.001);

        PositionType openPosition = market.isPositionOpen(candle.getInstrument());

        DayOfWeek weekDay = candle.getLocalDateTime().getDayOfWeek();

        //Todo: Maybe somewhere else?
        boolean isWeekend = weekDay == DayOfWeek.SATURDAY || weekDay == DayOfWeek.SUNDAY;
        boolean dontOpenPositions = weekDay == DayOfWeek.FRIDAY || (weekDay == DayOfWeek.THURSDAY && candle.getLocalDateTime().getHour() >= 22);
        boolean stopTrading = (weekDay == DayOfWeek.FRIDAY && candle.getLocalDateTime().getHour() >= 15) || isWeekend;

        if(stopTrading){
            //Market is closing, get out
            if(openPosition != PositionType.None) {
                market.closePosition(candle.getInstrument());
            }
        }
        else {
            if (bands.isReady()) {
                int bandsValue = (int)bands.getIndicatorValue();
                if(reversed){
                    if(BollingerBands.BollingerState.Higher.ordinal() == bandsValue)
                        bandsValue = BollingerBands.BollingerState.Lower.ordinal();
                    else if(BollingerBands.BollingerState.Lower.ordinal() == bandsValue)
                        bandsValue = BollingerBands.BollingerState.Higher.ordinal();
                }

                if (bandsValue == BollingerBands.BollingerState.Lower.ordinal() && openPosition != PositionType.Buy) {
                    if (openPosition == PositionType.Sell)
                        market.closePosition(candle.getInstrument());

                    if(!dontOpenPositions)
                        market.openPosition(candle.getInstrument(), tradedUnits, PositionType.Buy); //Todo: Price/0.0001 or Price/0.001
                }

                if (bandsValue == BollingerBands.BollingerState.Higher.ordinal() && openPosition != PositionType.Sell) {
                    if (openPosition == PositionType.Buy)
                        market.closePosition(candle.getInstrument());

                    if(!dontOpenPositions)
                        market.openPosition(candle.getInstrument(), tradedUnits, PositionType.Sell);
                }
            }
        }
    }

    @Override
    public int getPreparationTime() {
        return (int)dna.get("sma");
    }

    @Override
    public StrategyDNA getDna() {
        return this.dna;
    }

    @Override
    public StrategyDNA getOffspringDna(double exploration) {
        double actualExploration = 10d * exploration;

        StrategyDNA nDna = new StrategyDNA(this.getClass());
        nDna.put("sma", (int)((Math.random() * actualExploration - actualExploration / 2d) + dna.get("sma")));

        if(nDna.get("sma") <= 0)
            nDna.put("sma", 1);

        actualExploration = 1d * exploration;
        nDna.put("factor", Formatting.round((Math.random() * actualExploration - actualExploration / 2d) + dna.get("factor"), 1));

        if(nDna.get("factor") <= 0)
            nDna.put("factor", 1);

        nDna.put("reversed", Math.random() > 0.5 ? 1 : 0);

        return nDna;
    }

    @Override
    public StrategyDNA getRandomDna() {
        StrategyDNA nDna = new StrategyDNA(this.getClass());
        nDna.put("sma", (int)(Math.random() * 100d + 1d));
        nDna.put("factor", Formatting.round(Math.random() * 5 + 1d, 2));
        nDna.put("reversed", Math.random() > 0.5 ? 1 : 0);

        return nDna;
    }
}

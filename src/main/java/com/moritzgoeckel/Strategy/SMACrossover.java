package com.moritzgoeckel.Strategy;

import com.moritzgoeckel.Charting.ChartAdapter;
import com.moritzgoeckel.Data.Candle;
import com.moritzgoeckel.Data.StrategyData;
import com.moritzgoeckel.Indicators.SMA;
import com.moritzgoeckel.Market.Market;
import com.moritzgoeckel.Data.PositionType;

import java.time.DayOfWeek;

public class SMACrossover implements Strategy {

    private SMA sma_1 = null;
    private SMA sma_2 = null;

    private StrategyDNA dna = null;

    private StrategyData data = new StrategyData(new String[]{"sma1", "sma2", "weekend"}, new int[]{ChartAdapter.AxisGroup.Price.ordinal(), ChartAdapter.AxisGroup.Price.ordinal(), ChartAdapter.AxisGroup.ZeroOneFilled.ordinal()});

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

        int tradedUnits = (int)(value / 0.001);

        PositionType openPosition = market.isPositionOpen(candle.getInstrument());

        DayOfWeek weekDay = candle.getLocalDateTime().getDayOfWeek();

        //Todo: Maybe somewhere else?
        boolean isWeekend = weekDay == DayOfWeek.SATURDAY || weekDay == DayOfWeek.SUNDAY;
        boolean dontOpenPositions = weekDay == DayOfWeek.FRIDAY || (weekDay == DayOfWeek.THURSDAY && candle.getLocalDateTime().getHour() >= 22);
        boolean stopTrading = (weekDay == DayOfWeek.FRIDAY && candle.getLocalDateTime().getHour() >= 15) || isWeekend;

        double one = 0;
        double two = 0;

        if(sma_1.isReady())
            one = sma_1.getIndicatorValue();

        if(sma_2.isReady())
            two = sma_2.getIndicatorValue();

        if(stopTrading){
            //Market is closing, get out
            if(openPosition != PositionType.None) {
                market.closePosition(candle.getInstrument());
            }
        }
        else {
            if (sma_1.isReady() && sma_2.isReady()) {
                if (one > two && openPosition != PositionType.Buy) {
                    if (openPosition == PositionType.Sell)
                        market.closePosition(candle.getInstrument());

                    if(!dontOpenPositions)
                        market.openPosition(candle.getInstrument(), tradedUnits, PositionType.Buy); //Todo: Price/0.0001 or Price/0.001
                }

                if (one < two && openPosition != PositionType.Sell) {
                    if (openPosition == PositionType.Buy)
                        market.closePosition(candle.getInstrument());

                    if(!dontOpenPositions)
                        market.openPosition(candle.getInstrument(), tradedUnits, PositionType.Sell);
                }
            }
        }

        data.addData(new String[]{"sma1", "sma2", "weekend"}, new double[]{one, two, stopTrading ? 1 : 0});
    }

    @Override
    public int getPreparationTime() {
        return Math.max((int)dna.get("sma1"), (int)dna.get("sma2"));
    }

    @Override
    public StrategyDNA getDna() {
        return this.dna;
    }

    @Override
    public StrategyDNA getOffspringDna(double exploration) {
        double actualExploration = 10d * exploration;

        StrategyDNA nDna = new StrategyDNA(this.getClass());
        nDna.put("sma1", (int)((Math.random() * actualExploration - actualExploration / 2d) + dna.get("sma1")));
        nDna.put("sma2", (int)((Math.random() * actualExploration - actualExploration / 2d) + dna.get("sma2")));

        if(nDna.get("sma1") <= 0)
            nDna.put("sma1", 1);

        if(nDna.get("sma2") <= 0)
            nDna.put("sma2", 1);

        return nDna;
    }

    @Override
    public StrategyData getStrategyData() {
        return data;
    }

    @Override
    public StrategyDNA getRandomDna() {
        StrategyDNA nDna = new StrategyDNA(this.getClass());
        nDna.put("sma1", (int)(Math.random() * 150d + 1d));
        nDna.put("sma2", (int)(Math.random() * 150d + 1d));

        return nDna;
    }
}

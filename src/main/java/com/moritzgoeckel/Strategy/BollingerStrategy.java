package com.moritzgoeckel.Strategy;

import com.moritzgoeckel.Charting.ChartAdapter;
import com.moritzgoeckel.Data.Candle;
import com.moritzgoeckel.Data.PositionType;
import com.moritzgoeckel.Data.StrategyData;
import com.moritzgoeckel.Indicators.BollingerBands;
import com.moritzgoeckel.Indicators.SMA;
import com.moritzgoeckel.Market.Market;
import com.moritzgoeckel.Util.Formatting;

import java.text.Normalizer;
import java.time.DayOfWeek;

public class BollingerStrategy implements Strategy {

    private StrategyDNA dna = null;
    private BollingerBands openingBands, closingBands;
    private boolean reversed;

    private StrategyData data = new StrategyData(new String[]{"openingBands", "closingBands", "weekend"}, new int[]{ChartAdapter.AxisGroup.ZeroOneTwo.ordinal(), ChartAdapter.AxisGroup.ZeroOneTwo.ordinal(), ChartAdapter.AxisGroup.ZeroOneFilled.ordinal()});

    @Override
    public void setDna(StrategyDNA dna) {
        this.dna = dna;
        openingBands = new BollingerBands((int)dna.get("sma"), (int)dna.get("factorOpening"));
        closingBands = new BollingerBands((int)dna.get("sma"), (int)dna.get("factorClosing"));

        reversed = dna.get("reversed") < 0.5;
    }

    @Override
    public void candleCompleted(Candle candle, Market market) {
        double value = candle.getMid().getC().doubleValue();
        openingBands.pushValue(value);
        closingBands.pushValue(value);

        int tradedUnits = (int)(value / 0.001);

        PositionType openPosition = market.isPositionOpen(candle.getInstrument());

        DayOfWeek weekDay = candle.getLocalDateTime().getDayOfWeek();

        //Todo: Maybe somewhere else?
        boolean isWeekend = weekDay == DayOfWeek.SATURDAY || weekDay == DayOfWeek.SUNDAY;
        boolean dontOpenPositions = weekDay == DayOfWeek.FRIDAY || (weekDay == DayOfWeek.THURSDAY && candle.getLocalDateTime().getHour() >= 22);
        boolean stopTrading = (weekDay == DayOfWeek.FRIDAY && candle.getLocalDateTime().getHour() >= 15) || isWeekend;

        int openingValue = (int)openingBands.getIndicatorValue();
        int closingValue = (int)closingBands.getIndicatorValue();

        if(openingBands.isReady())
            openingValue = (int)openingBands.getIndicatorValue();

        if(closingBands.isReady())
            closingValue = (int)closingBands.getIndicatorValue();

        if(stopTrading){
            //Market is closing, get out
            if(openPosition != PositionType.None) {
                market.closePosition(candle.getInstrument());
            }
        }
        else {
            if (openingBands.isReady() && closingBands.isReady()) {
                if(reversed){
                    if(BollingerBands.BollingerState.Higher.ordinal() == openingValue)
                        openingValue = BollingerBands.BollingerState.Lower.ordinal();
                    else if(BollingerBands.BollingerState.Lower.ordinal() == openingValue)
                        openingValue = BollingerBands.BollingerState.Higher.ordinal();
                }

                if(closingValue != BollingerBands.BollingerState.Inside.ordinal()) {
                    if (openingValue == BollingerBands.BollingerState.Lower.ordinal() && openPosition != PositionType.Buy) {
                        if (openPosition == PositionType.Sell)
                            market.closePosition(candle.getInstrument());

                        if (!dontOpenPositions)
                            market.openPosition(candle.getInstrument(), tradedUnits, PositionType.Buy); //Todo: Price/0.0001 or Price/0.001
                    }

                    if (openingValue == BollingerBands.BollingerState.Higher.ordinal() && openPosition != PositionType.Sell) {
                        if (openPosition == PositionType.Buy)
                            market.closePosition(candle.getInstrument());

                        if (!dontOpenPositions)
                            market.openPosition(candle.getInstrument(), tradedUnits, PositionType.Sell);
                    }
                }
                else if(openPosition != PositionType.None){ //Inside closing area
                    market.closePosition(candle.getInstrument());
                }
            }
        }

        data.addData(new String[]{"openingBands", "closingBands", "weekend"}, new double[]{openingValue, closingValue, stopTrading ? 1 : 0});
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
        nDna.put("sma", (int)(Math.abs((Math.random() * actualExploration - actualExploration / 2d) + dna.get("sma"))));

        actualExploration = 1d * exploration;
        nDna.put("factorOpening", Math.abs(Formatting.round((Math.random() * actualExploration - actualExploration / 2d) + dna.get("factorOpening"), 1)));
        nDna.put("factorClosing", Math.abs(Formatting.round((Math.random() * actualExploration - actualExploration / 2d) + dna.get("factorClosing"), 1)));

        //Todo: factorClosing has to be < factorOpening??

        nDna.put("reversed", Math.random() > 0.5 ? 1 : 0);

        return nDna;
    }

    @Override
    public StrategyData getStrategyData() {
        return data;
    }

    @Override
    public StrategyDNA getRandomDna() {
        StrategyDNA nDna = new StrategyDNA(this.getClass());
        nDna.put("sma", (int)(Math.random() * 100d + 1d));
        nDna.put("factorOpening", Formatting.round(Math.random() * 5, 1));
        nDna.put("factorClosing", Formatting.round(Math.random() * nDna.get("factorOpening") + 0.1d, 1));

        nDna.put("reversed", Math.random() > 0.5 ? 1 : 0);

        return nDna;
    }
}

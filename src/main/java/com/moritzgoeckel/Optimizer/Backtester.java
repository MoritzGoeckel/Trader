package com.moritzgoeckel.Optimizer;

import com.moritzgoeckel.Data.Candle;
import com.moritzgoeckel.Data.PositionType;
import com.moritzgoeckel.Market.BacktestMarket;
import com.moritzgoeckel.Statistics.PositionStatistics;
import com.moritzgoeckel.Strategy.Strategy;
import com.moritzgoeckel.Strategy.StrategyDNA;

import java.util.List;

public class Backtester {
    public static PositionStatistics backtest(List<Candle> candleList, StrategyDNA dna) throws IllegalAccessException, InstantiationException {
        BacktestMarket market = new BacktestMarket();

        Strategy strategy = dna.getStrategyLogic().newInstance();
        strategy.setDna(dna);

        for(Candle c : candleList) {
            market.updateCandle(c);
            strategy.candleCompleted(c, market);
        }

        if(market.isPositionOpen(candleList.get(0).getInstrument()) != PositionType.None)
            market.closePosition(candleList.get(0).getInstrument());

        return market.getStatistics();
    }
}

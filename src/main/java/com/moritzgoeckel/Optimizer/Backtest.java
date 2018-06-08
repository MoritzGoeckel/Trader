package com.moritzgoeckel.Optimizer;

import com.moritzgoeckel.Data.Candle;
import com.moritzgoeckel.Data.PositionType;
import com.moritzgoeckel.Data.StrategyData;
import com.moritzgoeckel.Market.BacktestMarket;
import com.moritzgoeckel.Statistics.PositionStatistics;
import com.moritzgoeckel.Strategy.Strategy;
import com.moritzgoeckel.Strategy.StrategyDNA;

import java.util.List;

public class Backtest {

    private BacktestMarket market = new BacktestMarket();
    private StrategyData strategyData = null;
    private PositionStatistics statistics = null;

    public Backtest(List<Candle> candleList, StrategyDNA dna) throws IllegalAccessException, InstantiationException {
        Strategy strategy = dna.getStrategyLogic().newInstance();
        strategy.setDna(dna);

        for(Candle c : candleList) {
            market.updateCandle(c);
            strategy.candleCompleted(c, market);
        }

        if(market.isPositionOpen(candleList.get(0).getInstrument()) != PositionType.None)
            market.closePosition(candleList.get(0).getInstrument());

        strategyData = strategy.getStrategyData();
    }

    public PositionStatistics getStatistics(){
        if(statistics == null)
            statistics = market.getStatistics();

        return statistics;
    }

    public StrategyData getStrategyData() {
        return strategyData;
    }
}

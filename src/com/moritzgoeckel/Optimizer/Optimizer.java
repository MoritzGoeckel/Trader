package com.moritzgoeckel.Optimizer;

import com.moritzgoeckel.Data.Candle;
import com.moritzgoeckel.Market.BacktestMarket;
import com.moritzgoeckel.Statistics.PositionStatistics;
import com.moritzgoeckel.Strategy.Strategy;
import com.moritzgoeckel.Strategy.StrategyDNA;

import java.lang.reflect.Type;
import java.util.*;
import java.util.function.Function;

public class Optimizer {
    List<Candle> candleList;

    List<StrategyDNA> queue = new LinkedList<>();
    SortedMap<Double, StrategyDNA> leaderboard = new TreeMap<>();
    Function<PositionStatistics, Double> scoringFunction;

    public Optimizer(List<Candle> candleList, Function<PositionStatistics, Double> scoringFunction){
        this.candleList = candleList;
        this.scoringFunction = scoringFunction;
    }

    public void doGeneration() throws IllegalAccessException, InstantiationException {
        for(StrategyDNA dna : queue){
            BacktestMarket market = new BacktestMarket();

            Strategy strategy = dna.getStrategyLogic().newInstance();
            strategy.setDna(dna);

            for(Candle c : candleList) {
                market.updateCandle(c);
                strategy.candleCompleted(c, market);
            }

            double score = scoringFunction.apply(market.getStatistics());
            leaderboard.put(score, dna);
        }
    }

    public Optimizer addRandomToQueue(Class <? extends Strategy> strategyType, int amount) throws IllegalAccessException, InstantiationException {
        Strategy strategy = strategyType.newInstance();
        for(int i = 0; i < amount; i++)
            queue.add(strategy.getRandomDna()); //Todo: no double

        return this;
    }

    public Optimizer addOffspringToQueue(int untilLeaderboardIndex){
        Iterator<StrategyDNA> iterator = leaderboard.values().iterator();

        for(int i = 0; i < untilLeaderboardIndex && iterator.hasNext(); i++)
            queue.add(iterator.next());

        return this;
    }
}

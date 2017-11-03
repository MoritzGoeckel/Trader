package com.moritzgoeckel.Optimizer;

import com.moritzgoeckel.Data.Candle;
import com.moritzgoeckel.Data.PositionType;
import com.moritzgoeckel.Market.BacktestMarket;
import com.moritzgoeckel.Statistics.PositionStatistics;
import com.moritzgoeckel.Strategy.Strategy;
import com.moritzgoeckel.Strategy.StrategyDNA;

import java.util.*;
import java.util.function.Function;

public class Optimizer {
    private List<Candle> candleList;

    private List<StrategyDNA> queue = new LinkedList<>();
    private SortedMap<Double, StrategyDNA> leaderboard = new TreeMap<>(Collections.reverseOrder());
    private Function<PositionStatistics, Double> scoringFunction;
    private Set<String> doneDna = new HashSet<>();

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

            if(market.isPositionOpen(candleList.get(0).getInstrument()) != PositionType.None)
                market.closePosition(candleList.get(0).getInstrument());

            double score = scoringFunction.apply(market.getStatistics());
            leaderboard.put(score, dna);
        }
    }

    public Optimizer addRandomToQueue(Class <? extends Strategy> strategyType, int amount) throws IllegalAccessException, InstantiationException {
        Strategy strategy = strategyType.newInstance();
        for(int i = 0; i < amount; i++) {
            StrategyDNA dna = strategy.getRandomDna();
            if(!doneDna.contains(dna.getHash())) {
                queue.add(dna);
                doneDna.add(dna.getHash());
            }
        }

        return this;
    }

    public Optimizer addOffspringToQueue(int untilLeaderboardIndex){
        Iterator<StrategyDNA> iterator = leaderboard.values().iterator();

        for(int i = 0; i < untilLeaderboardIndex && iterator.hasNext(); i++) {
            StrategyDNA dna = iterator.next();
            if(!doneDna.contains(dna.getHash())) {
                queue.add(dna);
                doneDna.add(dna.getHash());
            }
        }

        return this;
    }

    public List<StrategyDNA> getLeaderboard(int amount){
        List<StrategyDNA> output = new LinkedList<>();
        Iterator<StrategyDNA> iterator = leaderboard.values().iterator();

        for(int i = 0; i < amount && iterator.hasNext(); i++)
            output.add(iterator.next());

        return output;
    }
}

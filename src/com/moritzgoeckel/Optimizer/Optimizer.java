package com.moritzgoeckel.Optimizer;

import com.moritzgoeckel.Data.Candle;
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

    public void processQueue() throws IllegalAccessException, InstantiationException {
        for(StrategyDNA dna : queue){
            PositionStatistics stats = Backtester.backtest(candleList, dna);
            double score = scoringFunction.apply(stats);
            leaderboard.put(score, dna);
        }
        queue.clear();
    }

    public Optimizer addRandomToQueue(Class <? extends Strategy> strategyType) throws IllegalAccessException, InstantiationException {
        return addRandomToQueue(strategyType, 1);
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

    public Optimizer addOffspringToQueue(int untilLeaderboardIndex) throws IllegalAccessException, InstantiationException{
        return addOffspringToQueue(untilLeaderboardIndex, 1);
    }

    public Optimizer addOffspringToQueue(int untilLeaderboardIndex, int amount) throws IllegalAccessException, InstantiationException {
        Iterator<StrategyDNA> iterator = leaderboard.values().iterator();

        for(int i = 0; i < untilLeaderboardIndex && iterator.hasNext(); i++) {
            StrategyDNA parentDna = iterator.next();

            for(int done = 0; done < amount; done++) {
                Strategy s = parentDna.getStrategyLogic().newInstance();
                s.setDna(parentDna);
                StrategyDNA childDna = s.getOffspringDna();

                if (!doneDna.contains(childDna.getHash())) {
                    queue.add(childDna);
                    doneDna.add(childDna.getHash());
                }
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

    public int getQueueLength() {
        return this.queue.size();
    }

    public int getDoneStrategiesCount() {
        return this.doneDna.size() - this.queue.size();
    }
}

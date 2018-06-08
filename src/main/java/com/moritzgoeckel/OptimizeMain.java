package com.moritzgoeckel;

import com.moritzgoeckel.Charting.ChartAdapter;
import com.moritzgoeckel.Data.Candle;
import com.moritzgoeckel.Data.CandleDownloader;
import com.moritzgoeckel.Optimizer.Backtest;
import com.moritzgoeckel.Optimizer.Optimizer;
import com.moritzgoeckel.Statistics.PositionStatistics;
import com.moritzgoeckel.Strategy.BollingerStrategy;
import com.moritzgoeckel.Strategy.SMACrossover;
import com.moritzgoeckel.Strategy.StrategyDNA;
import com.moritzgoeckel.Util.Formatting;
import com.oanda.v20.Context;
import com.oanda.v20.instrument.CandlestickGranularity;
import com.oanda.v20.primitives.InstrumentName;
import javafx.util.Pair;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class OptimizeMain {

    public static void main(String[] args) throws Exception {
        Context ctx = new Context("https://api-fxpractice.oanda.com", "3a9a4f4ce5b4c5838dbfc24a7706151b-dd15e44936e5c3a48fbae5277d024e07");
        CandleDownloader downloader = new CandleDownloader(ctx);

        String insts = "SUGAR_USD|WHEAT_USD|XPD_USD|DE10YB_EUR|NL25_EUR|DE30_EUR|NAS100_USD";
        List<String> instruments = Arrays.asList(insts.split("\\|"));

        int goodValidations = 0, doneValidations = 0;

        for(String instrument : instruments){
            System.out.println("############## " + instrument + " ##############");
            List<Candle> optimizationCandles = downloader.downloadCandles(new InstrumentName(instrument), CandlestickGranularity.H1, LocalDateTime.now().minusDays(100), LocalDateTime.now().minusDays(30));
            List<Candle> validationCandles = downloader.downloadCandles(new InstrumentName(instrument), CandlestickGranularity.H1, LocalDateTime.now().minusDays(30), LocalDateTime.now());

            ChartAdapter c = new ChartAdapter(400, 300, instrument);
            c.addPrices(validationCandles);

            Pair<StrategyDNA, Backtest> pair = optimize(optimizationCandles, validationCandles);

            doneValidations++;
            if (pair.getValue().getStatistics().getProfit()> 0d)
                goodValidations++;

            c.addPositions(pair.getValue().getStatistics());
            c.addStrategyData(pair.getValue().getStrategyData());
            c.show(pair.getKey().toString() + "\r\n" + "Sharpe=" + pair.getValue().getStatistics().getSharpe());

            System.out.println("Good validations: " + ((double) goodValidations / (double) doneValidations));
            break;//Todo remove
        }

        System.out.println("DONE");
    }

    private static Pair<StrategyDNA, Backtest> optimize(List<Candle> optimizingCandles, List<Candle> validationCandles) throws InstantiationException, IllegalAccessException, InterruptedException {
        Optimizer optimizer = new Optimizer(optimizingCandles,
                stats -> stats.getSemiSharpe() * (stats.getNumberTrades() > 5 ? 1 : 0)
        );

        //Todo: Somehow only SMA CROSSOVERS win
        optimizer.addRandomToQueue(SMACrossover.class, 100);
        optimizer.addRandomToQueue(BollingerStrategy.class, 100); //Todo: Other strategy logic

        optimizer.processQueue();

        double lastScore = 0;
        int roundsTodo = 1;
        for(int i = 0; i <= roundsTodo; i++) {
            double exploration = (5d - i) / 5d;
            if(exploration <= 0)
                exploration = 0.1d;

            optimizer.addOffspringToQueue(3, 10, exploration);
            optimizer.addOffspringToQueue(5, 10, exploration);
            optimizer.addOffspringToQueue(10, 50, exploration);
            optimizer.addOffspringToQueue(20, 5, exploration);
            optimizer.addOffspringToQueue(50, 5, exploration);
            optimizer.addOffspringToQueue(200, 1, exploration);

            int fillSeeds = 500 - optimizer.getQueueLength();
            if(fillSeeds > 0) {
                optimizer.addRandomToQueue(SMACrossover.class, fillSeeds / 2);
                optimizer.addRandomToQueue(BollingerStrategy.class, fillSeeds / 2);
            }

            optimizer.processQueue();

            Map.Entry<Double, StrategyDNA> best = optimizer.getLeaderboard(1).get(0);
            double score = best.getKey();
            if(score != lastScore) {
                lastScore = score;
                roundsTodo += 2;
            }

            System.out.print("\rRound: " + i + "/" + roundsTodo + " expl=" + exploration + " \t" + score + "\t\t" + best.getValue().getHash());
        }

        System.out.println("\nProcessed: " + optimizer.getDoneStrategiesCount());

        List<Map.Entry<Double, StrategyDNA>> best = optimizer.getLeaderboard(10);

        Backtest optimizationBacktest = new Backtest(optimizingCandles, best.get(0).getValue());
        PositionStatistics optStats = optimizationBacktest.getStatistics();

        Backtest validationBacktest = new Backtest(validationCandles, best.get(0).getValue());
        PositionStatistics stats = validationBacktest.getStatistics();

        //Verbose
        if(true) {
            System.out.println("# WINNER #");
            System.out.println(best.get(0).getValue().getHash());

            System.out.println("# OPTIMIZATION BACKTEST #");
            optStats.printSummary();

            System.out.println("# VALIDATION BACKTEST #");
            stats.printSummary();
            stats.printProfitsPerWeek();
        }
        else {
            System.out.println(Formatting.round(stats.getSharpe(), 2) + "\t\t" + best.get(0).getValue().getHash());
        }

        return new Pair<>(best.get(0).getValue(), validationBacktest);
    }
}

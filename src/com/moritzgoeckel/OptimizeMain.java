package com.moritzgoeckel;

import com.moritzgoeckel.Data.Candle;
import com.moritzgoeckel.Data.CandleDownloader;
import com.moritzgoeckel.Optimizer.Backtester;
import com.moritzgoeckel.Optimizer.Optimizer;
import com.moritzgoeckel.Statistics.PositionStatistics;
import com.moritzgoeckel.Strategy.SMACrossover;
import com.moritzgoeckel.Strategy.StrategyDNA;
import com.oanda.v20.Context;
import com.oanda.v20.instrument.CandlestickGranularity;
import com.oanda.v20.primitives.InstrumentName;
import javafx.util.Pair;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

public class OptimizeMain {

    public static void main(String[] args) throws Exception {
        Context ctx = new Context("https://api-fxpractice.oanda.com", "3a9a4f4ce5b4c5838dbfc24a7706151b-dd15e44936e5c3a48fbae5277d024e07");
        CandleDownloader downloader = new CandleDownloader(ctx);

        String insts = "SUGAR_USD|WHEAT_USD|XPD_USD|DE10YB_EUR|NL25_EUR|DE30_EUR|NAS100_USD";
        List<String> instruments = Arrays.asList(insts.split("\\|"));

        int goodValidations = 0, doneValidations = 0;

        for(String instrument : instruments){
            System.out.println("############## " + instrument + " ##############");
            List<Candle> optimizationCandles = downloader.downloadCandles(new InstrumentName(instrument), CandlestickGranularity.M30, LocalDateTime.now().minusDays(200), LocalDateTime.now().minusDays(60));
            List<Candle> validationCandles = downloader.downloadCandles(new InstrumentName(instrument), CandlestickGranularity.M30, LocalDateTime.now().minusDays(60), LocalDateTime.now());

            Pair<StrategyDNA, PositionStatistics> pair = optimize(optimizationCandles, validationCandles);

            doneValidations++;
            if (pair.getValue().getProfit() > 0d) {
                System.out.println(pair.getKey().getHash());
                goodValidations++;
            }
            System.out.println("Good validations: " + ((double) goodValidations / (double) doneValidations));
        }

        System.out.println("DONE");
    }

    private static Pair<StrategyDNA, PositionStatistics> optimize(List<Candle> optimizingCandles, List<Candle> validationCandles) throws InstantiationException, IllegalAccessException, InterruptedException {
        Optimizer optimizer = new Optimizer(optimizingCandles, stats -> stats.getSharpe());
        optimizer.addRandomToQueue(SMACrossover.class, 100); //Todo: Other strategy logic
        optimizer.processQueue();

        for(int i = 0; i < 100; i++) {
            double exploration = ((100 - i) + 1) / 100d;

            optimizer.addOffspringToQueue(3, 10, exploration);
            optimizer.addOffspringToQueue(5, 10, exploration);
            optimizer.addOffspringToQueue(10, 50, exploration);
            optimizer.addOffspringToQueue(20, 5, exploration);
            optimizer.addOffspringToQueue(50, 5, exploration);
            optimizer.addOffspringToQueue(200, 1, exploration);

            int fillSeeds = 50 - optimizer.getQueueLength();
            if(fillSeeds > 0)
                optimizer.addRandomToQueue(SMACrossover.class, fillSeeds);

            System.out.print("\rRound: " + i + "/100");

            optimizer.processQueue();
        }

        System.out.println("\nProcessed: " + optimizer.getDoneStrategiesCount());

        List<StrategyDNA> best = optimizer.getLeaderboard(10);

        System.out.println("# WINNER #");
        System.out.println(best.get(0).getHash());

        System.out.println("# OPTIMIZATION BACKTEST #");
        PositionStatistics optStats = Backtester.backtest(optimizingCandles, best.get(0));
        optStats.printSummary();

        System.out.println("# VALIDATION BACKTEST #");
        PositionStatistics stats = Backtester.backtest(validationCandles, best.get(0));
        stats.printSummary();
        stats.printProfitsPerWeek();

        return new Pair<>(best.get(0), stats);
    }
}

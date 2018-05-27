package com.moritzgoeckel;

import com.moritzgoeckel.Data.Candle;
import com.moritzgoeckel.Data.CandleDownloader;
import com.moritzgoeckel.Optimizer.Backtester;
import com.moritzgoeckel.Optimizer.Optimizer;
import com.moritzgoeckel.Statistics.PositionStatistics;
import com.moritzgoeckel.Strategy.SMACrossover;
import com.moritzgoeckel.Strategy.StrategyDNA;
import com.oanda.v20.Context;
import com.oanda.v20.account.AccountID;
import com.oanda.v20.instrument.CandlestickGranularity;
import com.oanda.v20.primitives.Instrument;
import javafx.util.Pair;

import java.time.LocalDateTime;
import java.util.List;

public class OptimizeMain {

    public static void main(String[] args) throws Exception {
        Context ctx = new Context("https://api-fxpractice.oanda.com", "3a9a4f4ce5b4c5838dbfc24a7706151b-dd15e44936e5c3a48fbae5277d024e07");
        CandleDownloader downloader = new CandleDownloader(ctx);

        List<Instrument> instruments = ctx.account.instruments(new AccountID("101-004-2357917-002")).getInstruments();

        int goodValidations = 0, doneValidations = 0;

        for(Instrument i : instruments){
            if(i.getName().toString().contains("USD") || i.getName().toString().contains("EUR")) {
                System.out.println("############## " + i.getName() + " ##############");
                List<Candle> optimizationCandles = downloader.downloadCandles(i.getName(), CandlestickGranularity.M30, LocalDateTime.now().minusDays(365), LocalDateTime.now().minusDays(65));
                List<Candle> validationCandles = downloader.downloadCandles(i.getName(), CandlestickGranularity.M30, LocalDateTime.now().minusDays(60), LocalDateTime.now());

                Pair<StrategyDNA, PositionStatistics> pair = optimize(optimizationCandles, validationCandles);

                doneValidations++;
                if (pair.getValue().getSharpe() > 3) {
                    System.out.println(pair.getKey().getHash());
                    goodValidations++;
                }
                System.out.println("Good validations: " + ((double) goodValidations / (double) doneValidations));
            }
        }

        //Todo: Positive Weeks statistics

        System.out.println("DONE");
    }

    private static Pair<StrategyDNA, PositionStatistics> optimize(List<Candle> optimizingCandles, List<Candle> validationCandles) throws InstantiationException, IllegalAccessException {
        Optimizer optimizer = new Optimizer(optimizingCandles, stats -> stats.getSharpe() );
        optimizer.addRandomToQueue(SMACrossover.class, 100);
        optimizer.processQueue();

        for(int i = 0; i < 100; i++) {
            double exploration = ((100 - i) + 1) / 100d;

            optimizer.addOffspringToQueue(3, 10, exploration);
            optimizer.addOffspringToQueue(5, 10, exploration);
            optimizer.addOffspringToQueue(10, 50, exploration);
            optimizer.addOffspringToQueue(20, 5, exploration);
            optimizer.addOffspringToQueue(50, 5, exploration);

            int fillSeeds = 50 - optimizer.getQueueLength();
            if(fillSeeds > 0)
                optimizer.addRandomToQueue(SMACrossover.class, fillSeeds);

            System.out.print("\rRound: " + i + "/100" + " expl=" + exploration);

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

        return new Pair<>(best.get(0), stats);
    }
}

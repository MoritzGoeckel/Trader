package com.moritzgoeckel;

import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.moritzgoeckel.Data.Candle;
import com.moritzgoeckel.Data.CandleDownloader;
import com.moritzgoeckel.Data.CandleStore;
import com.moritzgoeckel.Indicators.RollingIndicator;
import com.moritzgoeckel.Indicators.SMA;
import com.moritzgoeckel.Live.Trader;
import com.moritzgoeckel.Market.Market;
import com.moritzgoeckel.Market.OandaMarket;
import com.moritzgoeckel.Optimizer.Backtester;
import com.moritzgoeckel.Optimizer.Optimizer;
import com.moritzgoeckel.Statistics.PositionStatistics;
import com.moritzgoeckel.Strategy.SMACrossover;
import com.moritzgoeckel.Strategy.Strategy;
import com.moritzgoeckel.Strategy.StrategyDNA;
import com.moritzgoeckel.Util.Profiler;
import com.oanda.v20.Context;
import com.oanda.v20.account.AccountID;
import com.oanda.v20.instrument.CandlestickGranularity;
import com.oanda.v20.primitives.InstrumentName;

import java.time.LocalDateTime;
import java.util.List;
import java.util.function.Function;

public class Main {

    public static void main(String[] args) throws Exception {

        /*
        AmazonDynamoDB client = AmazonDynamoDBClientBuilder.standard()
            .withRegion(Regions.US_WEST_2)
            .build();
        */

        CandleStore store = new CandleStore(
            AmazonDynamoDBClientBuilder.standard().withEndpointConfiguration(
                    new AwsClientBuilder.EndpointConfiguration("http://localhost:8000", "eu-west-1"))
                    .build()
        );

        Context ctx = new Context("https://api-fxpractice.oanda.com", "3a9a4f4ce5b4c5838dbfc24a7706151b-dd15e44936e5c3a48fbae5277d024e07");

        CandleDownloader downloader = new CandleDownloader(ctx);
        /*
        //Table
        System.out.println("Creating table ...");
        store.createCandleTable();

        //Mass download
        System.out.println("Downloading candles ...");
        List<Candle> candles = downloader.downloadCandles(new InstrumentName("SUGAR_USD"), CandlestickGranularity.M30, LocalDateTime.now().minusDays(365), LocalDateTime.now());

        //Put into table
        System.out.println("Saving candles ...");
        store.saveCandle(candles);
        */
        //Read from table
        /*
        List<Candle> optimizingCandles = store.loadCandles("SUGAR_USD", "M30", LocalDateTime.now().minusDays(365), LocalDateTime.now().minusDays(65));
        List<Candle> validationCandles = store.loadCandles("SUGAR_USD", "M30", LocalDateTime.now().minusDays(60), LocalDateTime.now().minusDays(0));

        System.out.println("Optimizing ...");
        optimize(optimizingCandles, validationCandles);
        */

        /*List<Candle> preperation = store.loadCandles("SUGAR_USD", "M30", LocalDateTime.now().minusDays(30), LocalDateTime.now());

        //{sma2=28.0, sma1=31.0}
        StrategyDNA dna = new StrategyDNA(SMACrossover.class);
        dna.put("sma2", 28.0);
        dna.put("sma1", 31.0);

        Market oandaMarket = new OandaMarket(ctx, new AccountID("101-004-2357917-002"));

        Trader t = new Trader(dna, preperation);
        t.doCandle(downloader.getNewestCompleteCandle(new InstrumentName("SUGAR_USD"), CandlestickGranularity.M30), oandaMarket);*/

        System.out.println(downloader.getNewestCompleteCandle(new InstrumentName("SUGAR_USD"), CandlestickGranularity.M30));

        System.out.println("DONE");

        //Todo: Get out before the weekend -> Strategy
    }

    public static void optimize(List<Candle> optimizingCandles, List<Candle> validationCandles) throws InstantiationException, IllegalAccessException {
        Optimizer optimizer = new Optimizer(optimizingCandles, stats -> stats.getSharpe());
        optimizer.addRandomToQueue(SMACrossover.class, 100);
        optimizer.processQueue();

        for(int i = 0; i < 100; i++) {
            optimizer.addOffspringToQueue(3, 10);
            optimizer.addOffspringToQueue(5, 10);
            optimizer.addOffspringToQueue(10, 50);
            optimizer.addOffspringToQueue(20, 5);

            int fillSeeds = 50 - optimizer.getQueueLength();
            if(fillSeeds > 0)
                optimizer.addRandomToQueue(SMACrossover.class, fillSeeds);

            System.out.print("\rRound: " + i);

            optimizer.processQueue();
        }

        System.out.println("\nProcessed: " + optimizer.getDoneStrategiesCount());

        List<StrategyDNA> best = optimizer.getLeaderboard(10);

        System.out.println("# WINNER #");
        System.out.println(best.get(0).getHash());

        System.out.println("# OPTIMIZATION BACKTEST #");
        Backtester.backtest(optimizingCandles, best.get(0)).printSummary();

        System.out.println("# VALIDATION BACKTEST #");
        Backtester.backtest(validationCandles, best.get(0)).printSummary();
    }
}

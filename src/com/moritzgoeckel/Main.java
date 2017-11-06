package com.moritzgoeckel;

import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.moritzgoeckel.Data.Candle;
import com.moritzgoeckel.Data.CandleDownloader;
import com.moritzgoeckel.Data.CandleStore;
import com.moritzgoeckel.Live.Trader;
import com.moritzgoeckel.Market.Market;
import com.moritzgoeckel.Market.OandaMarket;
import com.moritzgoeckel.Optimizer.Backtester;
import com.moritzgoeckel.Optimizer.Optimizer;
import com.moritzgoeckel.Strategy.SMACrossover;
import com.moritzgoeckel.Strategy.StrategyDNA;
import com.oanda.v20.Context;
import com.oanda.v20.account.AccountID;
import com.oanda.v20.instrument.CandlestickGranularity;
import com.oanda.v20.primitives.InstrumentName;

import java.time.LocalDateTime;
import java.util.List;

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
        List<Candle> candles = downloader.downloadCandles(new InstrumentName("USD_JPY"), CandlestickGranularity.M30, LocalDateTime.now().minusDays(365), LocalDateTime.now());

        //Put into table
        System.out.println("Saving candles ...");
        store.saveCandle(candles);

        */
        /*

        //Read from table

        List<Candle> optimizingCandles = store.loadCandles("USD_JPY", "M30", LocalDateTime.now().minusDays(365), LocalDateTime.now().minusDays(65));
        List<Candle> validationCandles = store.loadCandles("USD_JPY", "M30", LocalDateTime.now().minusDays(60), LocalDateTime.now().minusDays(0));

        System.out.println("Opti");
        System.out.println(optimizingCandles.get(0));
        System.out.println(optimizingCandles.get(optimizingCandles.size() - 1));

        System.out.println("Veri");
        System.out.println(validationCandles.get(0));
        System.out.println(validationCandles.get(validationCandles.size() - 1));

        System.out.println("Optimizing ...");
        optimize(optimizingCandles, validationCandles);

        */

        //com.moritzgoeckel.Strategy.SMACrossover_{sma2=72.0, sma1=73.0}

        System.out.println("Downloading preperation candles ...");

        List<Candle> preperation = downloader.downloadCandles(new InstrumentName("USD_JPY"), CandlestickGranularity.M30, LocalDateTime.now().minusDays(30), LocalDateTime.now());

        System.out.println("Done downloading");
        System.out.println("First: " + preperation.get(0));
        System.out.println("Last: " + preperation.get(preperation.size() - 1));

        StrategyDNA dna = new StrategyDNA(SMACrossover.class);
        dna.put("sma2", 72.0);
        dna.put("sma1", 73.0);

        Market oandaMarket = new OandaMarket(ctx, new AccountID("101-004-2357917-002"));

        System.out.println("Preparing strategy ...");
        Trader t = new Trader(dna, preperation);
        System.out.println("Done preparing");

        LocalDateTime lastDoneDateTime = null;
        while (true) {
            Candle c = downloader.getNewestCompleteCandle(new InstrumentName("USD_JPY"), CandlestickGranularity.M30);

            if(lastDoneDateTime == null || lastDoneDateTime.isBefore(c.getLocalDateTime())) {
                t.doCandle(c, oandaMarket);
                lastDoneDateTime = c.getLocalDateTime();
                System.out.println("Submitting Candle: " + c.getLocalDateTime());
            }

            Thread.sleep(1000 * 60); //Every minute
        }

        //System.out.println("DONE");
    }

    /*public static void optimize(List<Candle> optimizingCandles, List<Candle> validationCandles) throws InstantiationException, IllegalAccessException {
        Optimizer optimizer = new Optimizer(optimizingCandles, stats -> stats.getSharpe());
        optimizer.addRandomToQueue(SMACrossover.class, 100);
        optimizer.processQueue();

        for(int i = 0; i < 100; i++) {
            optimizer.addOffspringToQueue(3, 10);
            optimizer.addOffspringToQueue(5, 10);
            optimizer.addOffspringToQueue(10, 50);
            optimizer.addOffspringToQueue(20, 5);
            optimizer.addOffspringToQueue(50, 5);

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
        Backtester.backtest(validationCandles, best.get(0)).printPositionHistory();
    }*/
}

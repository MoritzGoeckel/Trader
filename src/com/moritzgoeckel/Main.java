package com.moritzgoeckel;

import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.moritzgoeckel.Data.Candle;
import com.moritzgoeckel.Data.CandleDownloader;
import com.moritzgoeckel.Data.CandleStore;
import com.moritzgoeckel.Indicators.RollingIndicator;
import com.moritzgoeckel.Indicators.SMA;
import com.moritzgoeckel.Optimizer.Backtester;
import com.moritzgoeckel.Optimizer.Optimizer;
import com.moritzgoeckel.Statistics.PositionStatistics;
import com.moritzgoeckel.Strategy.SMACrossover;
import com.moritzgoeckel.Strategy.Strategy;
import com.moritzgoeckel.Strategy.StrategyDNA;
import com.moritzgoeckel.Util.Profiler;
import com.oanda.v20.Context;
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

        //Live API

        /*
        Market m = new OandaMarket(ctx, new AccountID("101-004-2357917-002"));

        PositionType type = m.isPositionOpen("EUR_USD");

        System.out.println("Open position on EUR_USD -> " + type);

        if(type != PositionType.None) {
            System.out.println("Closing on EUR_USD -> " + type + " ...");
            m.closePosition("EUR_USD");
        }

        m.openPosition("EUR_USD", 1, PositionType.Sell);

        */

        /*

         /*

        //Table
        System.out.println("Creating table ...");
        store.createCandleTable();

        */

        /*

        //Mass download
        System.out.println("Downloading candles ...");
        List<Candle> candles = downloader.downloadCandles(new InstrumentName("EUR_USD"), CandlestickGranularity.M30, LocalDateTime.now().minusDays(365), LocalDateTime.now());

        System.out.println("Len: " + candles.size());
        System.out.println(candles.get(0).getTime());
        System.out.println(candles.get(candles.size() - 1).getTime());

        //Put into table
        System.out.println("Saving candles ...");
        store.saveCandle(candles);

        */

        //Read from table

        Profiler p = new Profiler();

        System.out.println("Reading candles ...");
        List<Candle> optimizingCandles = store.loadCandles("EUR_USD", "M30", LocalDateTime.now().minusDays(365), LocalDateTime.now().minusDays(65));
        List<Candle> validationCandles = store.loadCandles("EUR_USD", "M30", LocalDateTime.now().minusDays(60), LocalDateTime.now().minusDays(0));

        System.out.println("OPTIMIZATION: " + optimizingCandles.size());
        System.out.println(optimizingCandles.get(0).getTime());
        System.out.println(optimizingCandles.get(optimizingCandles.size() - 1).getTime());

        System.out.println("VALIDATION: " + validationCandles.size());
        System.out.println(validationCandles.get(0).getTime());
        System.out.println(validationCandles.get(validationCandles.size() - 1).getTime());

        p.print();



        //Indicator

        /*

        RollingIndicator sma = new SMA(10);
        for(int i = 0; i < 30; i++){
            String s = "NULL";
            try{
                s = Double.toString(sma.getNext((double) i));
            }catch (Exception e){}
            System.out.println(s);
        }

        //Strategy
        Strategy s = new SMACrossover();
        s.setDna(s.getRandomDna());

        */

        //Optimize

        Profiler p2 = new Profiler();

        Optimizer optimizer = new Optimizer(optimizingCandles, stats -> stats.getPositiveTradesRatio());
        optimizer.addRandomToQueue(SMACrossover.class, 100);
        optimizer.processQueue();

        for(int i = 0; i < 100; i++) {
            optimizer.addOffspringToQueue(3, 10);
            optimizer.addOffspringToQueue(5, 10);
            optimizer.addOffspringToQueue(10, 50);
            optimizer.addOffspringToQueue(20, 5);

            optimizer.addRandomToQueue(SMACrossover.class, 50);

            optimizer.processQueue();
        }

        System.out.println("Processed: " + optimizer.getDoneStrategiesCount());

        List<StrategyDNA> best = optimizer.getLeaderboard(10);

        System.out.println("# WINNER #");
        System.out.println(best.get(0).getHash());

        System.out.println("# OPTIMIZATION BACKTEST #");
        Backtester.backtest(optimizingCandles, best.get(0)).printSummary();

        System.out.println("# VALIDATION BACKTEST #");
        Backtester.backtest(validationCandles, best.get(0)).printSummary();

        p2.print();

        System.out.println("DONE");
    }
}

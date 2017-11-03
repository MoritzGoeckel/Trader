package com.moritzgoeckel;

import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.moritzgoeckel.Data.Candle;
import com.moritzgoeckel.Data.CandleStore;
import com.moritzgoeckel.Indicators.RollingIndicator;
import com.moritzgoeckel.Indicators.SMA;
import com.moritzgoeckel.Market.Market;
import com.moritzgoeckel.Market.OandaMarket;
import com.moritzgoeckel.Market.PositionType;
import com.moritzgoeckel.Strategy.SMACrossover;
import com.moritzgoeckel.Strategy.Strategy;
import com.oanda.v20.Context;
import com.oanda.v20.account.AccountID;

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

        //Table
        System.out.println("Creating table ...");
        createCandleTable(client);

        //Mass download
        System.out.println("Downloading candles ...");
        List<Candle> candles = downloadCandles(new InstrumentName("EUR_USD"), CandlestickGranularity.M30, LocalDateTime.now().minusDays(365), LocalDateTime.now());

        System.out.println("Len: " + candles.size());
        System.out.println(candles.get(0).getTime());
        System.out.println(candles.get(candles.size() - 1).getTime());

        //Put into table
        System.out.println("Saving candles ...");
        saveCandle(client, candles, "EUR_USD", "M30");

        */

        //Read from table

        /*

        Profiler p = new Profiler();

        System.out.println("Reading candles ...");
        List<Candle> candles = store.loadCandles("EUR_USD", "M30", LocalDateTime.now().minusDays(365), LocalDateTime.now());

        System.out.println("Len: " + candles.size());
        System.out.println(candles.get(0).getTime());
        System.out.println(candles.get(candles.size() - 1).getTime());

        p.print();

        */

        //Indicator

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

        //Backtest API



        //Optimize

        System.out.println("DONE");
    }
}

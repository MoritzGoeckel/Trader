package com.moritzgoeckel;

import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.moritzgoeckel.Data.Candle;
import com.moritzgoeckel.Data.CandleStore;
import com.moritzgoeckel.Util.Profiler;

import java.time.LocalDateTime;
import java.util.*;

public class Main {

    public static void main(String[] args) throws Exception {

        CandleStore store = new CandleStore(
            AmazonDynamoDBClientBuilder.standard().withEndpointConfiguration(
                    new AwsClientBuilder.EndpointConfiguration("http://localhost:8000", "eu-west-1"))
                    .build()
        );

        /*
            AmazonDynamoDB client = AmazonDynamoDBClientBuilder.standard()
                .withRegion(Regions.US_WEST_2)
                .build();
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
        Profiler p = new Profiler();

        System.out.println("Reading candles ...");
        List<Candle> candles = store.loadCandles("EUR_USD", "M30", LocalDateTime.now().minusDays(365), LocalDateTime.now());

        System.out.println("Len: " + candles.size());
        System.out.println(candles.get(0).getTime());
        System.out.println(candles.get(candles.size() - 1).getTime());

        p.print();

        //Strategy

        //Backtest API

        //Optimize

        //Live API

        System.out.println("DONE");
    }

    public static void smthing(){

        /*AccountID accountId = new AccountID("101-004-2357917-002"); //AutoTest -> 1000€ 10/1
        List<String> instruments = new ArrayList<>(
                Arrays.asList("EUR_USD", "USD_JPY", "GBP_USD", "USD_CHF"));*/

        /*AccountListResponse list = ctx.account.list();
        for(AccountProperties p : list.getAccounts())
            System.out.println(p.getId());*/
    }
}

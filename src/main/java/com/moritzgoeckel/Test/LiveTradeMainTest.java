package com.moritzgoeckel.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.moritzgoeckel.Data.PositionType;
import com.moritzgoeckel.Indicators.RollingIndicator;
import com.moritzgoeckel.Indicators.SMA;
import com.moritzgoeckel.Market.Market;
import com.moritzgoeckel.Market.OandaMarket;
import com.oanda.v20.Context;
import com.oanda.v20.account.AccountID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Test collection")
class LiveTradeMainTest {

    @Test
    @DisplayName("Testtest")
    void myFirstTest() {
        assertEquals(2, 1 + 1);

        Context ctx = new Context("https://api-fxpractice.oanda.com", "3a9a4f4ce5b4c5838dbfc24a7706151b-dd15e44936e5c3a48fbae5277d024e07");

        Market m = new OandaMarket(ctx, new AccountID("101-004-2357917-002"));

        PositionType type = m.isPositionOpen("EUR_USD");

        System.out.println("Open position on EUR_USD -> " + type);

        if(type != PositionType.None) {
            System.out.println("Closing on EUR_USD -> " + type + " ...");
            m.closePosition("EUR_USD");
        }

        m.openPosition("EUR_USD", 1, PositionType.Sell);
    }

    void indicatorTest(){
        RollingIndicator sma = new SMA(10);
        for(int i = 0; i < 30; i++){
            String s = "NULL";
            try{
                sma.pushValue((double) i);
                s = Double.toString(sma.getIndicatorValue());
            }catch (Exception e){ e.printStackTrace(); }
            System.out.println(s);
        }
    }

    public static void createTableAndDownload(){
        //Table
        /*System.out.println("Creating table ...");
        store.createCandleTable();

        //Mass download
        System.out.println("Downloading candles ...");
        List<Candle> candles = downloader.downloadCandles(new InstrumentName("USD_JPY"), CandlestickGranularity.M30, LocalDateTime.now().minusDays(365), LocalDateTime.now());

        //Put into table
        System.out.println("Saving candles ...");
        store.saveCandle(candles);*/
    }

        /*
    AmazonDynamoDB client = AmazonDynamoDBClientBuilder.standard()
        .withRegion(Regions.US_WEST_2)
        .build();
    */

    /*CandleStore store = new CandleStore(
        AmazonDynamoDBClientBuilder.standard().withEndpointConfiguration(
                new AwsClientBuilder.EndpointConfiguration("http://localhost:8000", "eu-west-1"))
                .build()
    );*/

    public static void readFromTable(){
        /*

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
    }

}

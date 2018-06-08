package com.moritzgoeckel;

import com.moritzgoeckel.Data.Candle;
import com.moritzgoeckel.Data.CandleDownloader;
import com.moritzgoeckel.Live.Trader;
import com.moritzgoeckel.Market.Market;
import com.moritzgoeckel.Market.OandaMarket;
import com.moritzgoeckel.Strategy.SMACrossover;
import com.moritzgoeckel.Strategy.StrategyDNA;
import com.oanda.v20.Context;
import com.oanda.v20.ExecuteException;
import com.oanda.v20.RequestException;
import com.oanda.v20.account.AccountID;
import com.oanda.v20.instrument.CandlestickGranularity;
import com.oanda.v20.primitives.InstrumentName;

import java.time.LocalDateTime;
import java.util.List;

public class LiveTradeMain {

    public static void main(String[] args) throws Exception {

        Context ctx = new Context("https://api-fxpractice.oanda.com", "3a9a4f4ce5b4c5838dbfc24a7706151b-dd15e44936e5c3a48fbae5277d024e07");

        CandleDownloader downloader = new CandleDownloader(ctx);

        //### USD_JPY ###
        //com.moritzgoeckel.Strategy.SMACrossover_{sma2=72.0, sma1=73.0} "USD_JPY"

        StrategyDNA dna = new StrategyDNA(SMACrossover.class);
        dna.put("sma2", 72.0);
        dna.put("sma1", 73.0);

        liveTrade(downloader, "USD_JPY", ctx, dna, new AccountID("101-004-2357917-002"));

        System.out.println("DONE");
    }

    public static void liveTrade(CandleDownloader downloader, String instrument, Context oandaContext, StrategyDNA dna, AccountID accountID) throws InstantiationException, IllegalAccessException, RequestException, ExecuteException, InterruptedException {
        System.out.println("Downloading preparation candles ...");

        List<Candle> preparation = downloader.downloadCandles(new InstrumentName(instrument), CandlestickGranularity.M30, LocalDateTime.now().minusDays(30), LocalDateTime.now());

        while (!preparation.get(preparation.size() - 1).getComplete())
            preparation.remove(preparation.size() - 1);

        System.out.println("Done downloading");
        System.out.println("First: " + preparation.get(0).getLocalDateTime() + " Completed: " + preparation.get(0).getComplete());
        System.out.println("Last: " + preparation.get(preparation.size() - 1).getLocalDateTime() + " Completed: " + preparation.get(preparation.size() - 1).getComplete());

        Market oandaMarket = new OandaMarket(oandaContext, accountID);

        System.out.println("Preparing strategy ...");
        Trader t = new Trader(dna, preparation);
        System.out.println("Done preparing");

        System.out.println("Going live ...");
        LocalDateTime lastDoneDateTime = preparation.get(preparation.size() - 1).getLocalDateTime();

        while (true) {
            Candle c = downloader.getNewestCompleteCandle(new InstrumentName(instrument), CandlestickGranularity.M30);

            if(lastDoneDateTime == null || lastDoneDateTime.isBefore(c.getLocalDateTime())) {
                t.doCandle(c, oandaMarket);
                lastDoneDateTime = c.getLocalDateTime();
                System.out.println("Submitting Candle: " + c.getLocalDateTime() + " Completed: " + c.getComplete());
            }

            Thread.sleep(1000 * 60); //Every minute
        }
    }
}

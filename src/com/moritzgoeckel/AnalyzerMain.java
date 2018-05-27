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

public class AnalyzerMain {

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

                //Todo: Do stuff
            }
        }

        System.out.println("DONE");
    }
}

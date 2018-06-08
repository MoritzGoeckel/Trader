package com.moritzgoeckel;

import com.moritzgoeckel.Charting.ChartAdapter;
import com.moritzgoeckel.Data.Candle;
import com.moritzgoeckel.Data.CandleDownloader;
import com.moritzgoeckel.Optimizer.Backtest;
import com.moritzgoeckel.Optimizer.Optimizer;
import com.moritzgoeckel.Statistics.PositionStatistics;
import com.moritzgoeckel.Strategy.BollingerStrategy;
import com.moritzgoeckel.Strategy.SMACrossover;
import com.moritzgoeckel.Strategy.StrategyDNA;
import com.moritzgoeckel.Util.Formatting;
import com.oanda.v20.Context;
import com.oanda.v20.instrument.CandlestickGranularity;
import com.oanda.v20.primitives.InstrumentName;
import javafx.util.Pair;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class TestStrategyMain {

    public static void main(String[] args) throws Exception {
        Context ctx = new Context("https://api-fxpractice.oanda.com", "3a9a4f4ce5b4c5838dbfc24a7706151b-dd15e44936e5c3a48fbae5277d024e07");
        CandleDownloader downloader = new CandleDownloader(ctx);

        //String insts = "SUGAR_USD|WHEAT_USD|XPD_USD|DE10YB_EUR|NL25_EUR|DE30_EUR|NAS100_USD";
        //List<String> instruments = Arrays.asList(insts.split("\\|"));

        String instrument = "DE10YB_EUR";
        StrategyDNA dna = new StrategyDNA(SMACrossover.class).put("sma1", 20).put("sma2", 80);

        List<Candle> candles = downloader.downloadCandles(new InstrumentName(instrument), CandlestickGranularity.H1, LocalDateTime.now().minusDays(300), LocalDateTime.now());
        //List<Candle> validationCandles = downloader.downloadCandles(new InstrumentName(instrument), CandlestickGranularity.H1, LocalDateTime.now().minusDays(30), LocalDateTime.now());

        Backtest backtest = new Backtest(candles, dna);

        ChartAdapter c = new ChartAdapter(400, 300, instrument);
        c.addPrices(candles);
        c.addPositions(backtest.getStatistics());
        c.addStrategyData(backtest.getStrategyData());
        c.show(dna.toString() + " Sharpe=" + backtest.getStatistics().getSharpe() + " SemiSharpe=" + backtest.getStatistics().getSemiSharpe());

        backtest.getStatistics().printSummary();
        backtest.getStatistics().printPositionHistory();

    }
}

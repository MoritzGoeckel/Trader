package com.moritzgoeckel.Data;

import com.oanda.v20.Context;
import com.oanda.v20.ExecuteException;
import com.oanda.v20.RequestException;
import com.oanda.v20.instrument.Candlestick;
import com.oanda.v20.instrument.CandlestickGranularity;
import com.oanda.v20.instrument.InstrumentCandlesRequest;
import com.oanda.v20.instrument.InstrumentCandlesResponse;
import com.oanda.v20.primitives.InstrumentName;

import java.time.LocalDateTime;
import java.util.LinkedList;
import java.util.List;

public class CandleDownloader {

    private Context ctx;

    public CandleDownloader(String url, String token){
        this.ctx = new Context(url, token);
    }

    public CandleDownloader(Context ctx){
        this.ctx = ctx;
    }

    public Candle getNewestCompleteCandle(InstrumentName instrumentName, CandlestickGranularity granularity) throws ExecuteException, RequestException {
        List<Candle> candleList = downloadCandles(instrumentName, granularity, LocalDateTime.now().minusHours(8), LocalDateTime.now().plusHours(30));
        Candle[] allCandles = candleList.toArray(new Candle[candleList.size()]);
        for(int i = allCandles.length - 1; i > 0; i--){
            if(allCandles[i].getComplete())
                return allCandles[i];
        }

        throw new RuntimeException("Cant find candle!");
    }

    public List<Candle> downloadCandles(InstrumentName instrument, CandlestickGranularity granularity, LocalDateTime from, LocalDateTime to) throws RequestException, ExecuteException {
        List<Candle> allCandles = new LinkedList<>();
        LocalDateTime lastAddedDateTime = null;

        GET_NEW_CANDLES: while (lastAddedDateTime == null || lastAddedDateTime.isBefore(to)) {

            InstrumentCandlesRequest request = new InstrumentCandlesRequest(instrument)
                    .setPrice("BA")
                    .setFrom(from.toString() + "Z")
                    //.setTo(to.toString() + "Z")
                    .setGranularity(granularity)
                    .setCount(1000)
                    .setIncludeFirst(false)
                    .setAlignmentTimezone("UTC");

            InstrumentCandlesResponse resp = ctx.instrument.candles(request);
            List<Candlestick> newCandles = resp.getCandles();

            if(newCandles.size() == 0)
                break GET_NEW_CANDLES;

            for(Candlestick rawCandle : newCandles){
                Candle candle = new Candle(rawCandle, instrument.toString(), granularity.toString());
                if(candle.getLocalDateTime().isBefore(to) && (lastAddedDateTime == null || candle.getLocalDateTime().isAfter(lastAddedDateTime))){
                    allCandles.add(candle);
                    lastAddedDateTime = candle.getLocalDateTime();
                }
                else
                    break GET_NEW_CANDLES;
            }

            from = lastAddedDateTime.plusNanos(1);
        }

        return allCandles;
    }
}

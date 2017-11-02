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

    public static List<Candle> downloadCandles(InstrumentName instrument, CandlestickGranularity granularity, LocalDateTime from, LocalDateTime to) throws RequestException, ExecuteException {
        Context ctx = new Context("https://api-fxpractice.oanda.com", "1fe66ad3be0bf2d4fa579667945faa15-656562a41bc087ed9c6e91f3b3947f99");

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
                Candle candle = new Candle(rawCandle);
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

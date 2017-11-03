package com.moritzgoeckel.Data;

import com.oanda.v20.instrument.Candlestick;
import com.oanda.v20.primitives.DateTime;

import java.time.LocalDateTime;

public class Candle extends Candlestick {

    private LocalDateTime time = null;
    private String instrument, granularity;

    public Candle(){

    }

    public Candle(Candlestick candle, String instrument, String granularity){
        this.setTime(candle.getTime());
        this.setAsk(candle.getAsk());
        this.setBid(candle.getBid());
        this.setMid(candle.getMid());
        this.setVolume(candle.getVolume());
        this.setComplete(candle.getComplete());

        this.instrument = instrument;
        this.granularity = granularity;
    }

    private static LocalDateTime stringToDateTime(String s){
        return LocalDateTime.parse(s.substring(0, s.length() - 1));
    }

    @Override
    public Candlestick setTime(String time) {
        this.time = stringToDateTime(time);
        return super.setTime(time);
    }

    @Override
    public Candlestick setTime(DateTime time) {
        this.time = stringToDateTime(time.toString());
        return super.setTime(time);
    }

    public LocalDateTime getLocalDateTime() {
        return time;
    }

    public String getInstrument() {
        return instrument;
    }
}

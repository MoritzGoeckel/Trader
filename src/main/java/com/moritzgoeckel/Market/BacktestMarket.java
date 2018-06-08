package com.moritzgoeckel.Market;

import com.moritzgoeckel.Data.Candle;
import com.moritzgoeckel.Data.Position;
import com.moritzgoeckel.Data.PositionType;
import com.moritzgoeckel.Statistics.PositionStatistics;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class BacktestMarket implements Market {

    private List<Position> historicPositions = new LinkedList<>();
    private Map<String, Position> openPositions = new HashMap<>();
    private Candle currentCandle = null;

    public void updateCandle(Candle candle){
        this.currentCandle = candle;
    }

    @Override
    public void openPosition(String instrument, double units, PositionType type) {
        if(currentCandle == null)
            throw new RuntimeException("Current candle is not set, use updateCandle");

        if(type == PositionType.None)
            throw new RuntimeException("Position type cant be None");

        if(isPositionOpen(instrument) != PositionType.None)
            throw new RuntimeException("Existing position on instrument " + instrument);

        if(!historicPositions.isEmpty())
            for(Position p : historicPositions){
                if(!p.timeOut.equals(currentCandle.getLocalDateTime()))
                    break;

                if(p.instrument.equals(instrument) && type.equals(p.getType()))
                    throw new RuntimeException("Trying to open a position just after closing on same instrument: Closed=" + p.toString() + " Opening=" + instrument + " " + type);
            }

        Position p = new Position();
        p.setType(type);
        p.instrument = instrument;
        p.timeIn = currentCandle.getLocalDateTime();

        if(p.getType() == PositionType.Buy)
            p.in = currentCandle.getAsk().getC().doubleValue();

        if(p.getType() == PositionType.Sell)
            p.in = currentCandle.getBid().getC().doubleValue();

        openPositions.put(instrument, p);
    }

    @Override
    public void closePosition(String instrument) {
        if(currentCandle == null)
            throw new RuntimeException("Current candle is not set, use updateCandle");

        if(isPositionOpen(instrument) == PositionType.None)
            throw new RuntimeException("No existing position on instrument " + instrument);

        Position p = openPositions.remove(instrument);
        p.timeOut = currentCandle.getLocalDateTime();

        if(p.getType() == PositionType.Buy)
            p.out = currentCandle.getBid().getC().doubleValue();

        if(p.getType() == PositionType.Sell)
            p.out = currentCandle.getAsk().getC().doubleValue();

        historicPositions.add(p);
    }

    @Override
    public PositionType isPositionOpen(String instrument) {
        if(openPositions.containsKey(instrument))
            return openPositions.get(instrument).getType();
        else
            return PositionType.None;
    }

    public PositionStatistics getStatistics(){
        return new PositionStatistics(this.historicPositions);
    }
}
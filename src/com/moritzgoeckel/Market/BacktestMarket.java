package com.moritzgoeckel.Market;

public class BacktestMarket implements Market {
    @Override
    public void openPosition(String instrument, double units, PositionType type) {

    }

    @Override
    public void closePosition(String instrument) {

    }

    @Override
    public PositionType isPositionOpen(String instrument) {
        return null;
    }

    public double getSharpe(){
        return 0d; //Todo
    }

    public double getProfit(){
        return 0d;
    }

    public double getNumberTrades(){
        return 0d;
    }
}

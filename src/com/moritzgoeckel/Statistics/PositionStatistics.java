package com.moritzgoeckel.Statistics;

import com.moritzgoeckel.Data.Position;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class PositionStatistics {

    private List<Position> positionList;
    private double profit = 0, median, sd = 0, semiSd = 0;
    private int positiveTrades = 0;

    public PositionStatistics(List<Position> positionList){
        this.positionList = positionList;

        List<Double> profits = new LinkedList<>();

        for(Position p : positionList) {
            profit += p.getProfit();
            profits.add(p.getProfit());
        }

        Collections.sort(profits);

        median = profits.get(profits.size() / 2);

        double mean = getMean();
        for(double p : profits) {
            sd += Math.pow(p - mean, 2);

            if(p <= 0)
                semiSd += Math.pow(p - mean, 2);
            else
                positiveTrades++;
        }

        sd = Math.sqrt(sd / getNumberTrades());
        semiSd = Math.sqrt(semiSd/(getNumberTrades() - positiveTrades));
    }

    public double getSd(){
        return sd;
    }

    public double getSemiSd(){
        return semiSd;
    }

    public double getMedian(){
        return median;
    }

    public double getMean(){
        return profit / getNumberTrades();
    }

    public double getSharpe(){
        return getProfit() / getSd();
    }

    public double getSemiSharpe(){
        return getProfit() / getSemiSd();
    }

    public double getProfit(){
        return profit;
    }

    public double getPositiveTradesRatio(){
        return positiveTrades / getNumberTrades();
    }

    public double getNumberTrades(){
        return positionList.size();
    }

    public List<Position> getPositionList(){ return new LinkedList<Position>(positionList); }
}

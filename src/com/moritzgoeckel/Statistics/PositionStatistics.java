package com.moritzgoeckel.Statistics;

import com.moritzgoeckel.Data.Position;

import java.text.DecimalFormat;
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

        if(positionList.size() != 0)
            median = profits.get(profits.size() / 2);
        else
            median = 0;

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
        return profit / (double) getNumberTrades();
    }

    public double getSharpe(){
        if(getSd() == 0 || getProfit() == 0)
            return 0;

        return getProfit() / getSd();
    }

    public double getSemiSharpe(){
        if(getSemiSd() == 0 || getProfit() == 0)
            return 0;

        return getProfit() / getSemiSd();
    }

    public double getProfit(){
        return profit;
    }

    public double getPositiveTradesRatio(){
        if(getNumberTrades() == 0)
            return 0;

        return (double) positiveTrades / (double) getNumberTrades();
    }

    public int getNumberTrades(){
        return positionList.size();
    }

    public List<Position> getPositionList(){ return new LinkedList<>(positionList); }

    public void printSummary(){
        System.out.println(
                "Mean:\t\t" + formatDouble(getMean()) + "\r\n" +
                "Median:\t\t" + formatDouble(getMedian()) + "\r\n" +
                "Profitable:\t" + formatDouble(getPositiveTradesRatio()*100d) + "%" + "\r\n" +
                "Profit:\t\t" + formatDouble(getProfit()) + "\r\n" +
                "Sharpe:\t\t" + formatDouble(getSharpe()) + "\r\n" +
                "SemiSharpe:\t" + formatDouble(getSemiSharpe()) + "\r\n" +
                "SD:\t" + formatDouble(getSd()) + "\r\n" +
                "Trades:\t\t" + getNumberTrades()
        );
    }

    private static String formatDouble(Double d){
        DecimalFormat df = new DecimalFormat("#.#");
        df.setMaximumFractionDigits(7);

        return df.format(d);

        /*if(s.indexOf('.') >= desiredLen - 1)
            return s.substring(0, s.indexOf('.'));

        if (s.length() > desiredLen)
            s = s.substring(0, desiredLen);

        StringBuilder b = new StringBuilder(s);

        if (b.length() < desiredLen && !s.contains("."))
            b.append(".");

        while (b.length() < desiredLen)
            b.append(0);

        return b.toString();*/
    }
}

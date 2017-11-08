package com.moritzgoeckel.Statistics;

import com.moritzgoeckel.Data.Position;
import com.moritzgoeckel.Data.PositionType;
import javafx.util.Pair;

import java.text.DecimalFormat;
import java.time.temporal.TemporalField;
import java.time.temporal.WeekFields;
import java.util.*;

import static java.time.temporal.ChronoUnit.MINUTES;

public class PositionStatistics {

    private List<Position> positionList;
    private double profit = 0, median, sd = 0, semiSd = 0;
    private int positiveTrades = 0;
    private double medianDurationMinutes, durationMinutesSD = 0;
    private Map<String, Double> weekProfits = new HashMap<>();
    private double positiveWeeksRatio;

    public PositionStatistics(List<Position> positionList){
        this.positionList = positionList;

        List<Double> profits = new LinkedList<>();
        List<Double> durations = new LinkedList<>();
        double durationTotal = 0d;

        for(Position p : positionList) {
            profit += p.getProfit();
            profits.add(p.getProfit());

            durations.add(p.getDurationSeconds() / 60d);
            durationTotal += p.getDurationSeconds() / 60d;

            TemporalField woy = WeekFields.of(Locale.getDefault()).weekOfWeekBasedYear();
            String weekId = p.timeIn.get(woy) + "_" + p.timeIn.getYear();

            if(!weekProfits.containsKey(weekId))
                weekProfits.put(weekId, 0d);

            weekProfits.put(weekId, weekProfits.get(weekId) + p.getProfit());
        }

        int positiveWeeks = 0;
        for(String weekKey : weekProfits.keySet())
            positiveWeeks += (weekProfits.get(weekKey) > 0 ? 1 : 0);

        if(weekProfits.size() > 0)
            positiveWeeksRatio = (double) positiveWeeks / (double) weekProfits.size();
        else
            positiveWeeksRatio = 0;

        medianDurationMinutes = (double)durationTotal / (double) durations.size();

        //Collections.sort(minutes);
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

        for(Double d : durations){
            durationMinutesSD += Math.pow(d - medianDurationMinutes, 2);
        }

        sd = Math.sqrt(sd / (double) getNumberTrades());
        semiSd = Math.sqrt(semiSd / (double) (getNumberTrades() - positiveTrades));
        durationMinutesSD = Math.sqrt(durationMinutesSD / (double) durations.size());
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

    public void printPositionHistory(){
        for(Position p : positionList){
            System.out.printf("%-5s %-7s %-15s %-7s \r\n",
                    p.getType(),
                    formatDouble(p.getDurationSeconds() / 60d / 60d) + "h",
                    "PnL:" + (p.getProfit() < 0 ? "" : " ") + formatDouble(p.getProfit()),
                    "("+formatDouble(p.getProfit() / profit * 100) + "%)"
            );
        }
    }

    public double getMedianDurationMinutes(){
        return medianDurationMinutes;
    }

    public double getDurationMinutesSD(){
        return durationMinutesSD;
    }

    public void printSummary(){
        System.out.println(
                "Mean:\t\t\t" + formatDouble(getMean()) + "\r\n" +
                "Median:\t\t\t" + formatDouble(getMedian()) + "\r\n" +
                "Profitable:\t\t" + formatDouble(getPositiveTradesRatio()*100d) + "%" + "\r\n" +
                "Profit:\t\t\t" + formatDouble(getProfit()) + "\r\n" +
                "Sharpe:\t\t\t" + formatDouble(getSharpe()) + "\r\n" +
                "SemiSharpe:\t\t" + formatDouble(getSemiSharpe()) + "\r\n" +
                "SD:\t\t\t\t" + formatDouble(getSd()) + "\r\n" +
                "Trades:\t\t\t" + getNumberTrades() + "\r\n" +
                "avgDuration:\t" + formatDouble(getMedianDurationMinutes() / 60) + "h" + "\r\n" +
                "durationSD:\t\t" + formatDouble(getDurationMinutesSD() / 60) + "h" + "\r\n" +
                "pWeekRatio:\t\t" + formatDouble(getPositiveTradesRatio())
        );
    }

    private static String formatDouble(Double d){
        DecimalFormat df = new DecimalFormat("#.#");
        df.setMaximumFractionDigits(4);

        return df.format(d);
    }

    public Double getPositiveWeeksRatio() {
        return positiveWeeksRatio;
    }
}

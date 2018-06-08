package com.moritzgoeckel.Charting;

import com.moritzgoeckel.Data.Candle;
import com.moritzgoeckel.Data.Position;
import com.moritzgoeckel.Data.PositionType;
import com.moritzgoeckel.Data.StrategyData;
import com.moritzgoeckel.Statistics.PositionStatistics;
import org.knowm.xchart.XChartPanel;
import org.knowm.xchart.XYChart;
import org.knowm.xchart.XYChartBuilder;
import org.knowm.xchart.XYSeries;
import org.knowm.xchart.style.Styler;
import org.knowm.xchart.style.markers.SeriesMarkers;

import javax.swing.*;
import java.awt.*;
import java.time.LocalDateTime;
import java.util.*;
import java.util.List;
import java.util.stream.*;

public class ChartAdapter {

    public enum AxisGroup{
        Price,
        ZeroOne,
        PlusMinusOne,
        Profits,
        ZeroOneTwo,
        ZeroOneFilled
    }

    private XYChart chart;
    private int length = -1;

    private String title;

    private List<LocalDateTime> times;

    public ChartAdapter(int w, int h, String title){
        this.title = title;
        chart = new XYChartBuilder()
                .width(w)
                .height(h)
                .title(this.title)
                .xAxisTitle("Time")
                .yAxisTitle("Value")
                .theme(Styler.ChartTheme.Matlab).build();

        chart.getStyler().setMarkerSize(0);
    }

    public void addPrices(List<Candle> candles){
        double[] mids = candles.stream().flatMapToDouble(c -> DoubleStream.of(c.getMid().getC().doubleValue())).toArray();
        checkLength(mids);
        chart.addSeries("Mid", mids).setYAxisGroup(AxisGroup.Price.ordinal());

        /*double[] bids = candles.stream().flatMapToDouble(c -> DoubleStream.of(c.getBid().getC().doubleValue())).toArray();
        checkLength(bids);
        chart.addSeries("Bid", bids).setYAxisGroup(AxisGroup.Price.ordinal());

        double[] asks = candles.stream().flatMapToDouble(c -> DoubleStream.of(c.getAsk().getC().doubleValue())).toArray();
        checkLength(asks);
        chart.addSeries("Ask", asks).setYAxisGroup(AxisGroup.Price.ordinal());*/

        times = candles.stream().map(Candle::getLocalDateTime).collect(Collectors.toList());
    }

    public void addPositions(PositionStatistics statistics){
        LinkedList<Position> positionStack = new LinkedList<>();

        for(Position p : statistics.getPositionList())
            positionStack.addLast(p);

        double[] positions = new double[times.size()];
        double[] positionValue = new double[times.size()];

        for(int i = 0; i < times.size(); i++){
            if(positionStack.isEmpty())
                continue;

            if(positionStack.getFirst().getType() == PositionType.None)
                throw new RuntimeException("Positiontype is None");

            LocalDateTime now = times.get(i);

            if(i > 0) {
                positions[i] = positions[i - 1];
                positionValue[i] = positionValue[i - 1];
            }
            else {
                positions[0] = 0;
                positionValue[0] = 0;
            }

            if(positionStack.getFirst().timeIn.equals(now)) {
                positions[i] = positionStack.getFirst().getType() == PositionType.Buy ? 1 : -1;
                positionValue[i] = positionStack.getFirst().getProfit();
            }

            if(positions[i] != 0 && positionStack.getFirst().timeOut.equals(now)) {
                positions[i] = 0;
                positionValue[i] = 0;
                PositionType lastType = positionStack.removeFirst().getType();

                if(!positionStack.isEmpty() && positionStack.getFirst().timeIn.equals(now)) {
                    if(!lastType.equals(positionStack.getFirst().getType())) {
                        positions[i] = positionStack.getFirst().getType() == PositionType.Buy ? 1 : -1;
                        positionValue[i] = positionStack.getFirst().getProfit();
                    }
                    else
                        throw new RuntimeException("Trying to open the same position just after closing it: " + positionStack.getFirst().toString());
                }
            }

            if(!positionStack.isEmpty() && positions[i] == 0 && positionStack.getFirst().timeIn.isBefore(now))
                throw new RuntimeException("Should not happen: timeIn.isBefore(now)");

            if(!positionStack.isEmpty() && positions[i] != 0 && positionStack.getFirst().timeOut.isBefore(now))
                throw new RuntimeException("Should not happen: timeOut.isBefore(now)");
        }

        checkLength(positions);

        double[] buy = new double[positions.length];
        double[] sell = new double[positions.length];

        for(int i = 0; i < positions.length; i++){
            buy[i] = positions[i] == 1 ? 1 : 0;
            sell[i] = positions[i] == -1 ? 1 : 0;
        }

        checkLength(buy);
        checkLength(sell);
        checkLength(positionValue);

        chart.addSeries("Buy", buy)
                .setXYSeriesRenderStyle(XYSeries.XYSeriesRenderStyle.Area)
                .setMarker(SeriesMarkers.NONE)
                .setLineColor(new Color(0, 0, 0, 0))
                .setFillColor(new Color(0, 255, 0, 50))
                .setYAxisGroup(AxisGroup.ZeroOne.ordinal());

        chart.addSeries("Sell", sell)
                .setXYSeriesRenderStyle(XYSeries.XYSeriesRenderStyle.Area)
                .setMarker(SeriesMarkers.NONE)
                .setLineColor(new Color(0, 0, 0, 0))
                .setFillColor(new Color(255, 0, 0, 50))
                .setYAxisGroup(AxisGroup.ZeroOne.ordinal());

        chart.addSeries("Profit", positionValue)
                .setLineColor(Color.orange)
                .setYAxisGroup(AxisGroup.Profits.ordinal());
    }

    public void addStrategyData(StrategyData data){
        HashMap<String, LinkedList<Double>> internalData = data.getData();
        for(Map.Entry<String, LinkedList<Double>> entry : internalData.entrySet()){

            int axisGroup = data.getAxisGroup(entry.getKey());

            //Do not allow 0 in price chart
            if(axisGroup == AxisGroup.Price.ordinal()){
                entry.getValue().replaceAll(d -> d == 0 ? Double.NaN : d);
            }

            if(axisGroup == AxisGroup.ZeroOneFilled.ordinal()) {
            chart.addSeries("[" + entry.getKey() + "]", entry.getValue())
                    .setXYSeriesRenderStyle(XYSeries.XYSeriesRenderStyle.Area)
                    .setLineColor(new Color(0, 0, 0, 0))
                    .setFillColor(new Color(0, 0, 0, 50))
                    .setYAxisGroup(AxisGroup.ZeroOne.ordinal());
            }
            else
                chart.addSeries("[" + entry.getKey() + "]", entry.getValue())
                        .setYAxisGroup(axisGroup);
        }
    }

    public void show(){
        show("");
    }

    public void show(String info){
        JFrame frame = new JFrame(this.title);
        frame.setLayout(new BorderLayout());
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JPanel chartPanel = new XChartPanel<>(chart);
        frame.add(chartPanel, BorderLayout.CENTER);

        JLabel label = new JLabel(info, SwingConstants.CENTER);
        frame.add(label, BorderLayout.SOUTH);

        frame.pack();
        frame.setVisible(true);
    }

    private void checkLength(double[] array){
        if(length == -1)
            length = array.length;

        if(length != array.length)
            throw new RuntimeException("Invalid length: Length should be " + length + " but was " + array.length);
    }
}

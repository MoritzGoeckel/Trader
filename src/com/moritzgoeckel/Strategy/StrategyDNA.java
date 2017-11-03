package com.moritzgoeckel.Strategy;

import java.util.HashMap;
import java.util.Map;

public class StrategyDNA {
    private Map<String, Double> values = new HashMap<>();
    private Class <? extends Strategy> strategyLogic;

    public StrategyDNA(Class <? extends Strategy> strategyLogic){
        this.strategyLogic = strategyLogic;
    }

    public void put(String key, double value){
        values.put(key, value);
    }

    public double get(String key){
        return values.get(key);
    }

    public Class <? extends Strategy> getStrategyLogic() {
        return strategyLogic;
    }
}

package com.moritzgoeckel.Strategy;

import java.util.HashMap;
import java.util.Map;

public class StrategyDNA {
    private Map<String, Double> values = new HashMap<>();
    private Class <? extends Strategy> strategyLogic;

    public StrategyDNA(Class <? extends Strategy> strategyLogic){
        this.strategyLogic = strategyLogic;
    }

    public StrategyDNA put(String key, double value){
        values.put(key, value);
        return this;
    }

    public double get(String key){
        return values.get(key);
    }

    public Class <? extends Strategy> getStrategyLogic() {
        return strategyLogic;
    }

    public String getHash(){
        return strategyLogic.getName() + "_" + values.toString(); //Todo: To be tested
    }

    @Override
    public String toString() {
        return strategyLogic.getName() + "_" + values.toString();
    }
}

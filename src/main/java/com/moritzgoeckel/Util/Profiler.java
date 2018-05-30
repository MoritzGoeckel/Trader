package com.moritzgoeckel.Util;

public class Profiler {
    private long startMills;
    public Profiler(){
        startMills = System.nanoTime();
    }

    public void print(){
        print("Took");
    }

    public void print(String name){
        double seconds = (double) (System.nanoTime() - startMills) / (1000d * 1000 * 1000);
        System.out.println(name + " " + seconds);
    }
}

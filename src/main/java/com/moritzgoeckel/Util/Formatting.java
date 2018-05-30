package com.moritzgoeckel.Util;

public class Formatting {
    public static double round(double number, int decimals){
        return ((int)(number * Math.pow(10, decimals))) / Math.pow(10, decimals);
    }
}

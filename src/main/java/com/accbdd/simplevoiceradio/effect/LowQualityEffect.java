package com.accbdd.simplevoiceradio.effect;

public class LowQualityEffect extends AudioEffect {
    
    public short[] apply(short[] audio) {   
        short[] result = new short[audio.length];
        for (int i = 0; i < audio.length; i += severity) {
            int sum = 0;
            for (int j = 0; j < severity; j++) {
                sum += audio[i + j];
            }
            int avg = sum / severity;
            for (int j = 0; j < severity; j++) {
                result[i + j] = (short) avg;
            }
        }
        return result;
    }
}

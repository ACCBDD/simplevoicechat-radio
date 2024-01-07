package com.accbdd.simplevoiceradio.effect;

import java.util.Random;

public class IntermittentEffect extends AudioEffect {
    public static Random rand = new Random();

    public short[] apply(short[] audio) {   
        for (int i = 0; i < audio.length; i++) {
            if (rand.nextFloat(100) < severity) {
                audio[i] *= 0;
            }
        }
        return audio;
    }
}

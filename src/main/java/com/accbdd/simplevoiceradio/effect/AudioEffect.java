package com.accbdd.simplevoiceradio.effect;

public abstract class AudioEffect {
    public int severity;

    public abstract short[] apply(short[] audio);
}

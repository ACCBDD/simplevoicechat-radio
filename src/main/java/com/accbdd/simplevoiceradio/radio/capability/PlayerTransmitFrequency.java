package com.accbdd.simplevoiceradio.radio.capability;

import com.accbdd.simplevoiceradio.radio.Frequency;

import net.minecraft.nbt.CompoundTag;

public class PlayerTransmitFrequency {
    public String frequencyName = "";

    public void clearFrequency() {
        this.frequencyName = "";
    }

    public Frequency getFrequency() {
        return Frequency.getOrCreateFrequency(frequencyName); 
    }

    public void setFrequency(String frequencyName) {
        this.frequencyName = frequencyName;
    }

    public void copyFrom(PlayerTransmitFrequency source) {
        this.frequencyName = source.frequencyName;
    }

    public void saveNBTData(CompoundTag nbt) {
        nbt.putString("frequency", frequencyName);
    }

    public void loadNBTData(CompoundTag nbt) {
        frequencyName = nbt.getString("frequency");
    }
}

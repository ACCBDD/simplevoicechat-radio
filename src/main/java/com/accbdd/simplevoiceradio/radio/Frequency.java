package com.accbdd.simplevoiceradio.radio;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import javax.annotation.Nullable;

import com.accbdd.simplevoiceradio.SimpleVoiceRadio;

import net.minecraft.world.entity.player.Player;

public class Frequency {
    public static final List<Frequency> frequencies = new ArrayList<>();

    public static final int FREQUENCY_WHOLE_PLACES = 3;
    public static final int FREQUENCY_DECIMAL_PLACES = 2;
    public static final int FREQUENCY_DIGITS = FREQUENCY_WHOLE_PLACES + FREQUENCY_DECIMAL_PLACES;
    public static final int MAX_FREQUENCY = (int) Math.pow(10, FREQUENCY_DIGITS);
    private static final String FREQUENCY_PATTERN = "^\\d{"+FREQUENCY_WHOLE_PLACES+"}.\\d{"+FREQUENCY_DECIMAL_PLACES+"}$";

    public final String frequency;
    public final List<RadioChannel> listeners;

    public Frequency(String frequency) {
        if (!frequency.matches(FREQUENCY_PATTERN))
            throw new IllegalArgumentException(frequency + " does not follow frequency pattern!");

        this.frequency = frequency;
        this.listeners = new ArrayList<>();

        frequencies.add(this);
    }

    public static boolean validate(String frequency) {
        return frequency.matches(FREQUENCY_PATTERN);
    }

    public static int getFrequency(String string) {
        for (int i = 0; i < frequencies.size(); i++) {
            Frequency frequency = frequencies.get(i);
            if (frequency.frequency.equals(string))
                return i;
        }

        return -1;
    }

    public RadioChannel getChannel(UUID player) {
        for (RadioChannel listener : listeners)
            if (listener.owner.equals(player)) return listener;

        return null;
    }

    public RadioChannel getChannel(Player player) {
        return getChannel(player.getUUID());
    }

    @Nullable
    public RadioChannel tryAddListener(UUID owner) {
        if (getChannel(owner) == null)
            return addListener(owner);

        return null;
    }
    
    public RadioChannel addListener(UUID owner) {
        RadioChannel channel = new RadioChannel(owner);
        listeners.add(channel);
        SimpleVoiceRadio.LOGGER.info(String.format("added %s to frequency %s", owner, this.frequency));
        return channel;
    }

    public void removeListener(Player player) {
        removeListener(player.getUUID());
    }
    public void removeListener(UUID player) {
        listeners.removeIf(channel -> channel.owner.equals(player));

        SimpleVoiceRadio.LOGGER.info(String.format("removed %s from frequency %s", player, this.frequency));

        if (listeners.isEmpty())
            frequencies.remove(this);
    }

    public static Frequency getOrCreateFrequency(String frequency) {
        int index = getFrequency(frequency);
        if (index != -1) return frequencies.get(index);
        return new Frequency(frequency);
    }

    public static String incrementFrequency(String frequency, int amt) {
        int convertedFrequency = Integer.parseInt(frequency.replaceAll("[.]", ""));
        String str = String.format("%0"+FREQUENCY_DIGITS+"d", Math.max(100, Math.min(MAX_FREQUENCY - 1, convertedFrequency + amt)));
        SimpleVoiceRadio.LOGGER.info("returning {}", str);
        return new StringBuilder(str).insert(str.length() - FREQUENCY_DECIMAL_PLACES, ".").toString();
    }
}

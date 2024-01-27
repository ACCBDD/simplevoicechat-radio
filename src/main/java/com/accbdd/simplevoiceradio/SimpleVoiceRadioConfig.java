package com.accbdd.simplevoiceradio;

import net.minecraftforge.common.ForgeConfigSpec;

public class SimpleVoiceRadioConfig {
    public static ForgeConfigSpec COMMON_CONFIG;

    public static ForgeConfigSpec.BooleanValue ENTITY_VOICE_CHANNEL;
    public static ForgeConfigSpec.IntValue RADIO_DISTORTION_FACTOR;
    public static ForgeConfigSpec.IntValue RADIO_AUDIBLE_RANGE;

    static {
        ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();

        ENTITY_VOICE_CHANNEL = builder.comment("If true, a received radio transmission will be audible to everyone around the player holding the receiving radio")
            .define("entity_voice_channel", true);
        
        RADIO_AUDIBLE_RANGE = builder.comment("The maximum distance (in blocks) a player can be from another player holding a handheld radio and still hear that player's received audio - has no effect if entity_voice_channel is false; 1-128, default 16")
            .defineInRange("radio_audible_range", 16, 1, 128);

        RADIO_DISTORTION_FACTOR = builder.comment("How much fuzz is applied to a radio transmission; 0-100, default 5").defineInRange("radio_distortion_factor", 5, 0, 100);

        COMMON_CONFIG = builder.build();
    }
}

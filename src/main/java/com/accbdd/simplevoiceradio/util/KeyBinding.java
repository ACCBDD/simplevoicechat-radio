package com.accbdd.simplevoiceradio.util;

import org.lwjgl.glfw.GLFW;

import com.mojang.blaze3d.platform.InputConstants;

import net.minecraft.client.KeyMapping;
import net.minecraftforge.client.settings.KeyConflictContext;

public class KeyBinding {
    public static final String KEY_CATEGORY_RADIO = "key.category.simplevoiceradio.radio";
    public static final String KEY_ACTIVATE_RADIO = "key.simplevoiceradio.activate_radio";

    public static final KeyMapping ACTIVATE_RADIO_KEY = new KeyMapping(KEY_ACTIVATE_RADIO, KeyConflictContext.IN_GAME, 
        InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_GRAVE_ACCENT, KEY_CATEGORY_RADIO);
}

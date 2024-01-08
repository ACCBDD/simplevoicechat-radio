package com.accbdd.simplevoiceradio;

import java.util.ServiceLoader;

import org.slf4j.Logger;

import com.accbdd.simplevoiceradio.item.RadioItem;
import com.accbdd.simplevoiceradio.networking.NetworkingManager;
import com.accbdd.simplevoiceradio.radio.capability.PlayerTransmitFrequency;
import com.accbdd.simplevoiceradio.radio.capability.PlayerTransmitFrequencyProvider;
import com.accbdd.simplevoiceradio.registry.SoundRegistry;
import com.mojang.logging.LogUtils;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(SimpleVoiceRadio.MOD_ID)
public class SimpleVoiceRadio {

    public static final String MOD_ID = "simplevoiceradio";
    public static final Logger LOGGER = LogUtils.getLogger();

    public SimpleVoiceRadio() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        RadioItem.register(modEventBus);
        SoundRegistry.register(modEventBus);

        modEventBus.addListener(this::setup);

        MinecraftForge.EVENT_BUS.register(this);
    }

    private void setup(FMLCommonSetupEvent event) {
        LOGGER.info("Setting up Simple Voice Radio");
        NetworkingManager.register();
    }

    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {
        LOGGER.info("Server starting");
    }

    public static void error(Object object, Object... substitutions) {
        LOGGER.error(String.valueOf(object), substitutions);
    }
}

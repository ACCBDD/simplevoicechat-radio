package com.accbdd.simplevoiceradio;

import org.slf4j.Logger;

import com.accbdd.simplevoiceradio.networking.NetworkingManager;
import com.accbdd.simplevoiceradio.registry.ItemRegistry;
import com.accbdd.simplevoiceradio.registry.SoundRegistry;
import com.mojang.logging.LogUtils;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.InterModComms;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig.Type;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.lifecycle.InterModEnqueueEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.SlotTypeMessage;
import top.theillusivec4.curios.api.SlotTypePreset;

@Mod(SimpleVoiceRadio.MOD_ID)
public class SimpleVoiceRadio {

    public static final String MOD_ID = "simplevoiceradio";
    public static final Logger LOGGER = LogUtils.getLogger();

    public SimpleVoiceRadio() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        ItemRegistry.register(modEventBus);
        SoundRegistry.register(modEventBus);

        ModLoadingContext.get().registerConfig(Type.COMMON, SimpleVoiceRadioConfig.COMMON_CONFIG);

        modEventBus.addListener(this::setup);
        modEventBus.addListener(this::enqueue);

        MinecraftForge.EVENT_BUS.register(this);
    }

    private void setup(FMLCommonSetupEvent event) {
        LOGGER.info("Setting up Simple Voice Radio");
        NetworkingManager.register();
    }

    private void enqueue(final InterModEnqueueEvent evt) {
        InterModComms.sendTo(CuriosApi.MODID, SlotTypeMessage.REGISTER_TYPE,
            () -> SlotTypePreset.CURIO.getMessageBuilder().build());
    }

    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {
        LOGGER.info("Server starting");
    }

    public static void error(Object object, Object... substitutions) {
        LOGGER.error(String.valueOf(object), substitutions);
    }
}

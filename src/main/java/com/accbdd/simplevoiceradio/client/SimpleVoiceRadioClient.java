package com.accbdd.simplevoiceradio.client;

import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

import com.accbdd.simplevoiceradio.SimpleVoiceRadio;
import com.accbdd.simplevoiceradio.item.RadioItem;

@Mod.EventBusSubscriber(modid = SimpleVoiceRadio.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class SimpleVoiceRadioClient {
    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        ItemProperties.register(RadioItem.RADIO_ITEM.get(), new ResourceLocation("using"), 
            (stack, level, entity, i) -> entity != null && entity.isUsingItem() && entity.getUseItem() == stack ? 1.0f : 0.0f);
    }
}

package com.accbdd.simplevoiceradio.event;
import com.accbdd.simplevoiceradio.SimpleVoiceRadio;
import com.accbdd.simplevoiceradio.networking.NetworkingManager;
import com.accbdd.simplevoiceradio.networking.packet.RadioTransmitPacket;
import com.accbdd.simplevoiceradio.registry.ItemRegistry;
import com.accbdd.simplevoiceradio.screen.RadioConfigureScreen;
import com.accbdd.simplevoiceradio.util.KeyBinding;
import com.mojang.blaze3d.platform.InputConstants;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

public class ClientEvents {
    @Mod.EventBusSubscriber(modid = SimpleVoiceRadio.MOD_ID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
    public static class ClientModBusEvents {
        @SubscribeEvent
        public static void onKeyRegister(RegisterKeyMappingsEvent event) {
            event.register(KeyBinding.ACTIVATE_RADIO_KEY);
        }

        @SubscribeEvent
        public static void onClientSetup(FMLClientSetupEvent event) {
            ItemProperties.register(ItemRegistry.RADIO_ITEM.get(), new ResourceLocation("using"), 
                (stack, level, entity, i) -> entity != null && entity.isUsingItem() && entity.getUseItem() == stack ? 1.0f : 0.0f);
            
            
        }
    }

    @Mod.EventBusSubscriber(modid = SimpleVoiceRadio.MOD_ID, value = Dist.CLIENT)
    public static class ClientForgeEvents {
        @SubscribeEvent
        public static void onKeyInput(InputEvent.Key event) {
            if(event.getKey() == KeyBinding.ACTIVATE_RADIO_KEY.getKey().getValue()) {
                if(event.getAction() == InputConstants.PRESS) {
                    NetworkingManager.sendToServer(new RadioTransmitPacket(true, RadioTransmitPacket.PacketContext.KEYBIND));
                } else if(event.getAction() == InputConstants.RELEASE) {
                    NetworkingManager.sendToServer(new RadioTransmitPacket(false, RadioTransmitPacket.PacketContext.KEYBIND));
                }
            }

            if(KeyBinding.CONFIGURE_RADIO_KEY.consumeClick()) {
                Minecraft instance = Minecraft.getInstance();
                if (instance == null)
                    return;
                Item useItem = instance.player.getMainHandItem().getItem();
                if (useItem == ItemRegistry.RADIO_ITEM.get())
                    Minecraft.getInstance().setScreen(new RadioConfigureScreen());
                
            }
        }
    }

    
}

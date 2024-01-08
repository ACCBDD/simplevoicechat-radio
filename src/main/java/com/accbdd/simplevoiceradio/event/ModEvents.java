package com.accbdd.simplevoiceradio.event;

import java.util.UUID;

import com.accbdd.simplevoiceradio.SimpleVoiceRadio;
import com.accbdd.simplevoiceradio.item.RadioItem;
import com.accbdd.simplevoiceradio.radio.Frequency;
import com.accbdd.simplevoiceradio.radio.capability.PlayerTransmitFrequency;
import com.accbdd.simplevoiceradio.radio.capability.PlayerTransmitFrequencyProvider;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.entity.player.EntityItemPickupEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerEvent.PlayerLoggedInEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = SimpleVoiceRadio.MOD_ID)
public class ModEvents {
    @SubscribeEvent
    public static void onAttachCapabilitiesPlayer(AttachCapabilitiesEvent<net.minecraft.world.entity.Entity> event) {
        if(event.getObject() instanceof Player) {
            if(!event.getObject().getCapability(PlayerTransmitFrequencyProvider.PLAYER_TRANSMIT_FREQUENCY).isPresent()) {
                event.addCapability(new ResourceLocation(SimpleVoiceRadio.MOD_ID, "properties"), new PlayerTransmitFrequencyProvider());
            }
        }
    }

    @SubscribeEvent
    public static void onPlayerCloned(PlayerEvent.Clone event) {
        if (event.isWasDeath()) {
            event.getOriginal().getCapability(PlayerTransmitFrequencyProvider.PLAYER_TRANSMIT_FREQUENCY).ifPresent(oldStore -> {
                event.getOriginal().getCapability(PlayerTransmitFrequencyProvider.PLAYER_TRANSMIT_FREQUENCY).ifPresent(newStore -> {
                    newStore.copyFrom(oldStore);
                });
            });
        }
    }

    @SubscribeEvent
    public static void onRegisterCapabilities(RegisterCapabilitiesEvent event) {
        event.register(PlayerTransmitFrequency.class);
    }

    @SubscribeEvent
    public static void onEntityItemPickupEvent(EntityItemPickupEvent event) {
        Level level = event.getEntity().getLevel();
        ItemStack stack = event.getItem().getItem();
        if (!level.isClientSide) {
            if (event.getEntity() instanceof Player player) {
                Frequency.getOrCreateFrequency(stack.getOrCreateTag().getString("frequency")).tryAddListener(player.getUUID());
            }
        }
    }

    @SubscribeEvent
    public static void onPlayerLoggedInEvent(PlayerLoggedInEvent event) {
        Player player = event.getEntity();
        for (ItemStack item : player.getInventory().items) {
            if (item.getItem() == RadioItem.RADIO_ITEM.get()) {
                Frequency.getOrCreateFrequency(item.getOrCreateTag().getString("frequency")).tryAddListener(player.getUUID());
            }
        }
    }
}

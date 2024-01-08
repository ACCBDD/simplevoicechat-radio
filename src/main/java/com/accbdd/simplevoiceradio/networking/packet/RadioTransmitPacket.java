package com.accbdd.simplevoiceradio.networking.packet;

import java.util.function.Supplier;

import com.accbdd.simplevoiceradio.SimpleVoiceRadio;
import com.accbdd.simplevoiceradio.item.RadioItem;
import com.accbdd.simplevoiceradio.networking.Packeter;
import com.accbdd.simplevoiceradio.radio.Frequency;
import com.accbdd.simplevoiceradio.radio.capability.PlayerTransmitFrequency;
import com.accbdd.simplevoiceradio.radio.capability.PlayerTransmitFrequencyProvider;
import com.accbdd.simplevoiceradio.registry.SoundRegistry;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;

//packet from client to server telling it that we are transmitting!
public record RadioTransmitPacket(boolean transmitting, Enum<RadioTransmitPacket.PacketContext> packetContext) implements Packeter {
    public enum PacketContext {
        ITEM("item"),
        KEYBIND("keybind");

        public final String shorthand;

        PacketContext(String shorthand) {
            this.shorthand = shorthand;
        }
    }

    public static ResourceLocation ID = new ResourceLocation(SimpleVoiceRadio.MOD_ID, "radio_packet");
    @Override
    public ResourceLocation resource() {
        return ID;
    }

    public void encode(FriendlyByteBuf buffer) {
        buffer.writeBoolean(this.transmitting);
        buffer.writeEnum(this.packetContext);
    }

    public static RadioTransmitPacket decode(FriendlyByteBuf buffer) {
        return new RadioTransmitPacket(buffer.readBoolean(), buffer.readEnum(RadioTransmitPacket.PacketContext.class));
    }

    public boolean handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        boolean start = this.transmitting;

        context.enqueueWork(() -> {
            //we are on the server
            RadioItem radioItem = RadioItem.RADIO_ITEM.get();
            ServerPlayer player = context.getSender();
            ServerLevel level = player.getLevel();
            ItemStack radio = ((player.getItemInHand(InteractionHand.MAIN_HAND).getItem() == radioItem) ? player.getItemInHand(InteractionHand.MAIN_HAND) : player.getItemInHand(InteractionHand.OFF_HAND));
            if (this.packetContext == PacketContext.KEYBIND) {
                for (ItemStack item : player.getInventory().items) {
                    if (item.getItem() == radioItem) {
                        radio = item;
                        break;
                    }
                }
            }
            String frequency = radio.getOrCreateTag().getString("frequency");
            SimpleVoiceRadio.LOGGER.info("received! transmitting is {}! player is {}! frequency is {}!", start, player.getName(), frequency);
            if (start && !player.getCooldowns().isOnCooldown(radioItem)) {
                player.getCapability(PlayerTransmitFrequencyProvider.PLAYER_TRANSMIT_FREQUENCY).ifPresent(freq -> {
                    freq.setFrequency(frequency);
                    SimpleVoiceRadio.LOGGER.info("set {} transmit to {}!",player.getName(),frequency);
                });
                level.playSound(
                    null, player.blockPosition(),
                    SoundRegistry.RADIO_OPEN.get(),
                    SoundSource.PLAYERS,
                    1f,1f
                );
            } else {
                player.getCapability(PlayerTransmitFrequencyProvider.PLAYER_TRANSMIT_FREQUENCY).ifPresent(freq -> {
                    SimpleVoiceRadio.LOGGER.info("cleared {} transmit!",player.getName());
                    freq.clearFrequency();
                });
                level.playSound(
                    null, player.blockPosition(),
                    SoundRegistry.RADIO_CLOSE.get(),
                    SoundSource.PLAYERS,
                    1f,1f
                );
            }
        });

        return true;
    }
}
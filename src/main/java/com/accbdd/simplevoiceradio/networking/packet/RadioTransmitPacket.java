package com.accbdd.simplevoiceradio.networking.packet;

import java.util.function.Supplier;

import com.accbdd.simplevoiceradio.SimpleVoiceRadio;
import com.accbdd.simplevoiceradio.networking.Packeter;
import com.accbdd.simplevoiceradio.radio.capability.PlayerTransmitFrequencyProvider;
import com.accbdd.simplevoiceradio.registry.ItemRegistry;
import com.accbdd.simplevoiceradio.registry.SoundRegistry;
import com.accbdd.simplevoiceradio.registry.item.RadioItem;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.network.NetworkEvent;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.SlotResult;

//packet from client to server telling it that we are transmitting
public record RadioTransmitPacket(boolean transmitting, Enum<RadioTransmitPacket.PacketContext> packetContext) implements Packeter {
    public enum PacketContext {
        ITEM("item"),
        KEYBIND("keybind");

        public final String shorthand;

        PacketContext(String shorthand) {
            this.shorthand = shorthand;
        }
    }

    public static ResourceLocation ID = new ResourceLocation(SimpleVoiceRadio.MOD_ID, "radio_transmit_packet");
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
            RadioItem radioItem = ItemRegistry.RADIO_ITEM.get();
            ServerPlayer player = context.getSender();
            ServerLevel level = player.getLevel();
            ItemStack radio = ((player.getItemInHand(InteractionHand.MAIN_HAND).getItem() == radioItem) ? player.getItemInHand(InteractionHand.MAIN_HAND) : player.getItemInHand(InteractionHand.OFF_HAND));
            if (this.packetContext == PacketContext.KEYBIND) {
                radio = CuriosApi.getCuriosHelper().findFirstCurio(player, radioItem).map(SlotResult::stack).orElse(ItemStack.EMPTY);
                if (radio.getItem() != radioItem) {
                    for (ItemStack item : player.getInventory().items) {
                        if (item.getItem() == radioItem) {
                            radio = item;
                            break;
                        }
                    }
                }
            }

            if (radio.getItem() != radioItem)
                return;

            String frequency = radio.getOrCreateTag().getString("frequency");
            if (start && !player.getCooldowns().isOnCooldown(radioItem)) {
                player.getCapability(PlayerTransmitFrequencyProvider.PLAYER_TRANSMIT_FREQUENCY).ifPresent(freq -> {
                    freq.setFrequency(frequency);
                });
                level.playSound(
                    null, player.blockPosition(),
                    SoundRegistry.RADIO_OPEN.get(),
                    SoundSource.PLAYERS,
                    1f,1f
                );
            } else {
                player.getCapability(PlayerTransmitFrequencyProvider.PLAYER_TRANSMIT_FREQUENCY).ifPresent(freq -> {
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

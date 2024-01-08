package com.accbdd.simplevoiceradio.networking.packet;

import java.util.function.Supplier;

import com.accbdd.simplevoiceradio.SimpleVoiceRadio;
import com.accbdd.simplevoiceradio.networking.Packeter;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

//packet from client to server telling it that we are transmitting!
public record RadioPacket(boolean transmitting) implements Packeter {
    public static ResourceLocation ID = new ResourceLocation(SimpleVoiceRadio.MOD_ID, "radio_packet");
    @Override
    public ResourceLocation resource() {
        return ID;
    }

    public void encode(FriendlyByteBuf buffer) {
        buffer.writeBoolean(this.transmitting);
    }

    public static RadioPacket decode(FriendlyByteBuf buffer) {
        return new RadioPacket(buffer.readBoolean());
    }

    public boolean handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        boolean started = this.transmitting;

        context.enqueueWork(() -> {
            //we are on the server
            ServerPlayer player = context.getSender();
            SimpleVoiceRadio.LOGGER.info("received! transmitting is {}! player uuid is {}!", started, player.getUUID());
        });

        return true;
    }
}

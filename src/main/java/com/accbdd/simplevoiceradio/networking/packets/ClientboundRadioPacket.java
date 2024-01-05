package com.accbdd.simplevoiceradio.networking.packets;

import java.util.UUID;

import com.accbdd.simplevoiceradio.SimpleVoiceRadio;
import com.accbdd.simplevoiceradio.networking.Packeter;

import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

public record ClientboundRadioPacket(boolean started, UUID player) implements Packeter {
    public static ResourceLocation ID = new ResourceLocation(SimpleVoiceRadio.MOD_ID, "radio_packet");
    @Override
    public ResourceLocation resource() {
        return ID;
    }

    public void encode(FriendlyByteBuf buffer) {
        buffer.writeBoolean(this.started);
        buffer.writeUUID(this.player);
    }

    public static ClientboundRadioPacket decode(FriendlyByteBuf buffer) {
        return new ClientboundRadioPacket(buffer.readBoolean(), buffer.readUUID());
    }

    public static void handle(ClientboundRadioPacket packet) {
        boolean started = packet.started();
        UUID player = packet.player();

        Minecraft.getInstance().execute(() -> {
            SimpleVoiceRadio.LOGGER.info("received! started is {}! player uuid is {}!", started, player);
        });
    }
}

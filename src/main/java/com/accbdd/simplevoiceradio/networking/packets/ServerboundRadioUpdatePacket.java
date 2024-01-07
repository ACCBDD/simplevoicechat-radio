package com.accbdd.simplevoiceradio.networking.packets;

import com.accbdd.simplevoiceradio.SimpleVoiceRadio;
import com.accbdd.simplevoiceradio.networking.Packeter;
import com.accbdd.simplevoiceradio.radio.Frequency;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

public record ServerboundRadioUpdatePacket(String frequency, Frequency.Modulation modulation) implements Packeter {
    public static ResourceLocation ID = new ResourceLocation(SimpleVoiceRadio.MOD_ID, "radio_update_packet");
    @Override
    public ResourceLocation resource() {
        return ID;
    }

    public void encode(FriendlyByteBuf buffer) {
        buffer.writeUtf(this.frequency);
        buffer.writeUtf(this.modulation.shorthand);
    }

    public static ServerboundRadioUpdatePacket decode(FriendlyByteBuf buffer) {
        return new ServerboundRadioUpdatePacket(buffer.readUtf(), Frequency.modulationOf(buffer.readUtf()));
    }

    public void handle(MinecraftServer server, ServerPlayer player) {
        server.execute(() -> {
            if (!Frequency.validate(frequency)) return;
        });
    }
}

package com.accbdd.simplevoiceradio;

import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.NetworkEvent.ClientCustomPayloadEvent;
import net.minecraftforge.network.NetworkRegistry.ChannelBuilder;
import net.minecraftforge.network.simple.SimpleChannel;

import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Supplier;

import com.accbdd.simplevoiceradio.networking.packets.ClientboundRadioPacket;

import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;

@Mod.EventBusSubscriber(modid = SimpleVoiceRadio.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ForgeLoader {
    private static final String PROTOCOL_VERSION = "0";

    public static final SimpleChannel CHANNEL = ChannelBuilder.named(new ResourceLocation(SimpleVoiceRadio.MOD_ID,"channel"))
        .networkProtocolVersion(() -> PROTOCOL_VERSION)
        .clientAcceptedVersions(PROTOCOL_VERSION::equals)
        .serverAcceptedVersions(PROTOCOL_VERSION::equals)
        .simpleChannel();

    public static void loadPackets() {
        CHANNEL.messageBuilder(ClientboundRadioPacket.class, 1).decoder(ClientboundRadioPacket::decode).encoder(ClientboundRadioPacket::encode)
                .consumerMainThread(clientbound(ClientboundRadioPacket::handle)).add();
    }

    public static <P> BiConsumer<P, Supplier<ClientCustomPayloadEvent.Context>> clientbound(Consumer<P> consumer) {
        return (packet, context) -> {
            consumer.accept(packet);
            context.get().setPacketHandled(true);
        };
    }
}

package com.accbdd.simplevoiceradio.networking;

import com.accbdd.simplevoiceradio.SimpleVoiceRadio;
import com.accbdd.simplevoiceradio.networking.packet.RadioTransmitPacket;

import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

public class NetworkingManager {
    private static SimpleChannel INSTANCE;
    private static String PROTOCOL_VERSION = "1.0";

    private static int packetId = 0;
    private static int id() {
        return packetId++;
    }

    public static void register() {
        SimpleChannel net = NetworkRegistry.ChannelBuilder
            .named(new ResourceLocation(SimpleVoiceRadio.MOD_ID, "radio_packet"))
            .networkProtocolVersion(() -> PROTOCOL_VERSION)
            .clientAcceptedVersions(PROTOCOL_VERSION::equals)
            .serverAcceptedVersions(PROTOCOL_VERSION::equals)
            .simpleChannel();
        
        INSTANCE = net;

        net.messageBuilder(RadioTransmitPacket.class, id(), NetworkDirection.PLAY_TO_SERVER)
            .decoder(RadioTransmitPacket::decode)
            .encoder(RadioTransmitPacket::encode)
            .consumerMainThread(RadioTransmitPacket::handle)
            .add();
    }

    public static void sendToServer(Packeter packet) {
        INSTANCE.sendToServer(packet);
    }
}

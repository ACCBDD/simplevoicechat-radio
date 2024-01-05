package com.accbdd.simplevoiceradio;

import java.util.List;

import javax.annotation.Nullable;

import com.accbdd.simplevoiceradio.item.RadioItem;

import de.maxhenkel.voicechat.api.ForgeVoicechatPlugin;
import de.maxhenkel.voicechat.api.ServerPlayer;
import de.maxhenkel.voicechat.api.VoicechatApi;
import de.maxhenkel.voicechat.api.VoicechatConnection;
import de.maxhenkel.voicechat.api.VoicechatPlugin;
import de.maxhenkel.voicechat.api.VoicechatServerApi;
import de.maxhenkel.voicechat.api.events.EventRegistration;
import de.maxhenkel.voicechat.api.events.MicrophonePacketEvent;
import de.maxhenkel.voicechat.api.events.VoicechatServerStartedEvent;
import de.maxhenkel.voicechat.api.opus.OpusDecoder;
import de.maxhenkel.voicechat.api.opus.OpusEncoder;
import de.maxhenkel.voicechat.api.packets.MicrophonePacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;


@ForgeVoicechatPlugin
public class SimpleVoiceRadioPlugin implements VoicechatPlugin {
    
    public static VoicechatApi voicechatApi;
    @Nullable
    public static VoicechatServerApi voicechatServerApi;

    /**
     * @return the unique ID for this voice chat plugin
     */
    @Override
    public String getPluginId() {
        return SimpleVoiceRadio.MOD_ID;
    }

    /**
     * Called when the voice chat initializes the plugin.
     *
     * @param api the voice chat API
     */
    @Override
    public void initialize(VoicechatApi api) {
        SimpleVoiceRadio.LOGGER.info("Simple Voice Radio initialized!");
        voicechatApi = api;
    }

    /**
     * Called once by the voice chat to register all events.
     *
     * @param registration the event registration
     */
    @Override
    public void registerEvents(EventRegistration registration) {
        //hook into the normal group static sound event
        registration.registerEvent(MicrophonePacketEvent.class, this::onMicPacket);
        registration.registerEvent(VoicechatServerStartedEvent.class, this::onServerStarted);
    }

    /**
     * Gets the server api for voice chat.
     * 
     * @param event the server started event
     */
    private void onServerStarted(VoicechatServerStartedEvent event) {
        voicechatServerApi = event.getVoicechat();
        SimpleVoiceRadio.LOGGER.info("Simple Voice Radio server initialized!");
    }

    /**
     * Called when any microphone packet is sent to the server. 
     * 
     * @param event the mic packet event
     */
    public void onMicPacket(MicrophonePacketEvent event) {
        VoicechatConnection connection = event.getSenderConnection();

        if (connection == null || voicechatServerApi == null)  {
            SimpleVoiceRadio.LOGGER.info("connection: " + ((connection == null) ? "null" : "nonnull"));
            SimpleVoiceRadio.LOGGER.info("voicechatServerApi: " + ((voicechatServerApi) == null ? "null" : "nonnull"));
            return;
        }

        MicrophonePacket packet = event.getPacket();
        ServerPlayer sendingPlayer = connection.getPlayer();
        Player forgePlayer = (Player) sendingPlayer.getPlayer();

        if (!forgePlayer.getUseItem().getItem().equals(RadioItem.RADIO_ITEM.get())) {
            return;
        }
        
        if (packet.getOpusEncodedData().length <= 0) {
            SimpleVoiceRadio.LOGGER.info("0 length packet");
            return;
        }

        if (connection.isInGroup()) {
            SimpleVoiceRadio.LOGGER.info("in group");
            return;
        }

        if (!(sendingPlayer instanceof ServerPlayer)) {
            SimpleVoiceRadio.LOGGER.warn("Recieved mic packet from non-player - do you have an addon that generates these?");
            return;
        }

        OpusDecoder decoder = voicechatApi.createDecoder();
        OpusEncoder encoder = voicechatApi.createEncoder();

        byte[] crunchedPacket = encoder.encode(lowQuality(decoder.decode(packet.getOpusEncodedData())));

        MinecraftServer server = forgePlayer.level.getServer();
        List<net.minecraft.server.level.ServerPlayer> players = server.getPlayerList().getPlayers();
        for (net.minecraft.server.level.ServerPlayer player : players) {
            Player recievingForgePlayer = (Player) player;
            SimpleVoiceRadio.LOGGER.info("sending to: " + recievingForgePlayer.getEyePosition());
            if (recievingForgePlayer.getUUID() == ((Player) sendingPlayer.getPlayer()).getUUID()) {
                return;
            }

            VoicechatConnection recievingConnection = voicechatServerApi.getConnectionOf(player.getUUID());
            if (!recievingForgePlayer.getInventory().contains(new ItemStack(RadioItem.RADIO_ITEM.get()))) {
                //cancel generating static packet if reciever doesn't have a radio in their inventory
                SimpleVoiceRadio.LOGGER.info("no radio on reciever");
                return;
            }
            //generate a static sound packet
            voicechatServerApi.sendStaticSoundPacketTo(recievingConnection, packet.staticSoundPacketBuilder().opusEncodedData(crunchedPacket).build());
        }
    }

    //thank you to max henkel for audio filters!
    public short[] lowQuality(short[] audio) {
        int reduction = 4;
        
        short[] result = new short[audio.length];
        for (int i = 0; i < audio.length; i += reduction) {
            int sum = 0;
            for (int j = 0; j < reduction; j++) {
                sum += audio[i + j];
            }
            int avg = sum / reduction;
            for (int j = 0; j < reduction; j++) {
                result[i + j] = (short) avg;
            }
        }
        return result;
    }
}

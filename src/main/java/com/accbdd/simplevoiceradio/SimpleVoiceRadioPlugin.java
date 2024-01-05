package com.accbdd.simplevoiceradio;

import java.util.List;
import java.util.UUID;

import javax.annotation.Nullable;

import com.accbdd.simplevoiceradio.item.RadioItems;

import de.maxhenkel.voicechat.api.ForgeVoicechatPlugin;
import de.maxhenkel.voicechat.api.ServerPlayer;
import de.maxhenkel.voicechat.api.VoicechatApi;
import de.maxhenkel.voicechat.api.VoicechatConnection;
import de.maxhenkel.voicechat.api.VoicechatPlugin;
import de.maxhenkel.voicechat.api.VoicechatServerApi;
import de.maxhenkel.voicechat.api.events.EventRegistration;
import de.maxhenkel.voicechat.api.events.MicrophonePacketEvent;
import de.maxhenkel.voicechat.api.events.VoicechatServerStartedEvent;
import de.maxhenkel.voicechat.api.packets.MicrophonePacket;
import net.minecraft.client.particle.CampfireSmokeParticle.SignalProvider;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.client.model.IModelBuilder.Simple;

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

    private void onServerStarted(VoicechatServerStartedEvent event) {
        voicechatServerApi = event.getVoicechat();
        SimpleVoiceRadio.LOGGER.info("Simple Voice Radio server initialized!");
    }

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
        
        if (event.getPacket().getOpusEncodedData().length <= 0) {
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
        
        if(!(forgePlayer.getItemInHand(InteractionHand.MAIN_HAND).getItem().equals(RadioItems.RADIO_ITEM.get())) && !(forgePlayer.getItemInHand(InteractionHand.OFF_HAND).getItem().equals(RadioItems.RADIO_ITEM.get()))) {
            //TODO: implement curio support
            return;
        }
        
        //SimpleVoiceRadio.LOGGER.info(packet.getOpusEncodedData().toString());

        MinecraftServer server = forgePlayer.level.getServer();
        List<net.minecraft.server.level.ServerPlayer> players = server.getPlayerList().getPlayers();
        for (net.minecraft.server.level.ServerPlayer player : players) {
            Player recievingForgePlayer = (Player) player;
            if (recievingForgePlayer == (Player) sendingPlayer.getPlayer())
                return;
            VoicechatConnection recievingConnection = voicechatServerApi.getConnectionOf(player.getUUID());
            ServerPlayer recievingPlayer = recievingConnection.getPlayer();
            if (!recievingForgePlayer.getInventory().contains(new ItemStack(RadioItems.RADIO_ITEM.get()))) {
                //cancel generating static packet if reciever doesn't have a radio in their inventory
                SimpleVoiceRadio.LOGGER.info("no radio on reciever");
                return;
            }
            //generate a static sound packet
            voicechatServerApi.sendStaticSoundPacketTo(recievingConnection, packet.staticSoundPacketBuilder().build());
        }
    }
}

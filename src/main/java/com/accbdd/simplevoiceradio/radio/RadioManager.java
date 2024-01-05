package com.accbdd.simplevoiceradio.radio;

import java.util.UUID;

import com.accbdd.simplevoiceradio.item.RadioItem;

import de.maxhenkel.voicechat.api.VoicechatConnection;
import de.maxhenkel.voicechat.api.events.MicrophonePacketEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

public class RadioManager {
    private static RadioManager INSTANCE;

    public static RadioManager getInstance() {
        if (INSTANCE == null) INSTANCE = new RadioManager();
        return INSTANCE;
    }

    public RadioManager() {

    }

    public void onMicPacket(MicrophonePacketEvent event) {
        VoicechatConnection senderConnection = event.getSenderConnection();
        if (senderConnection == null) return;

        ServerPlayer sender = (ServerPlayer) senderConnection.getPlayer().getPlayer();
        ServerLevel level = sender.getLevel();

        ItemStack radio = sender.getUseItem();
        if (!(radio.getItem() instanceof RadioItem)) return;
        Frequency frequency = Frequency.getOrCreateFrequency(radio.getOrCreateTag().getString("frequency"), Frequency.Modulation.FREQUENCY);

        transmit(level, frequency, sender.getUUID(), sender.position(), event.getPacket().getOpusEncodedData());
    }

    private void transmit(ServerLevel serverLevel, Frequency frequency, UUID sender, Vec3 senderLocation, byte[] opusEncodedData) {
        for (RadioChannel channel : frequency.listeners) {
            if (sender.equals(channel.owner)) continue;

            channel.transmit(sender, senderLocation, opusEncodedData);
        }
    }
}

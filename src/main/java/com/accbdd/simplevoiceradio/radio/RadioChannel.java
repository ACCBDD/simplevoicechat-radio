package com.accbdd.simplevoiceradio.radio;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Supplier;

import com.accbdd.simplevoiceradio.SimpleVoiceRadioPlugin;
import com.accbdd.simplevoiceradio.effect.AudioEffect;
import com.accbdd.simplevoiceradio.effect.LowQualityEffect;

import de.maxhenkel.voicechat.api.VoicechatConnection;
import de.maxhenkel.voicechat.api.audiochannel.AudioPlayer;
import de.maxhenkel.voicechat.api.audiochannel.EntityAudioChannel;
import de.maxhenkel.voicechat.api.opus.OpusDecoder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

public class RadioChannel implements Supplier<short[]> {
    public UUID owner;
    public AudioPlayer audioPlayer;
    private final Map<UUID, List<short[]>> packetBuffer;
    private final Map<UUID, OpusDecoder> decoders;
    private final AudioEffect effect;

    public RadioChannel(Player owner) {
        this.owner = owner.getUUID();

        packetBuffer = new HashMap<>();
        decoders = new HashMap<>();
        effect = new LowQualityEffect();
    }

    @Override
    public short[] get() {
        short[] audio = generatePacket();
        if (audio == null) {
            if (audioPlayer != null) {
                audioPlayer.stopPlaying();
            }
            audioPlayer = null;
            return null;
        }
        return audio;
    }

    public short[] generatePacket() {
        List<short[]> packetsToCombine = new ArrayList<>();
        for (Map.Entry<UUID, List<short[]>> packets : packetBuffer.entrySet()) {
            if (packets.getValue().isEmpty()) {
                continue;
            }
            short[] audio = packets.getValue().remove(0);
            packetsToCombine.add(audio);
        }
        packetBuffer.values().removeIf(List::isEmpty);

        if (packetsToCombine.isEmpty()) {
            return null;
        }

        short[] combinedAudio = SimpleVoiceRadioPlugin.combineAudio(packetsToCombine);

        return effect.apply(combinedAudio);
    }

    public void transmit(UUID sender, Vec3 senderLocation, byte[] data) {
        List<short[]> microphonePackets = packetBuffer.computeIfAbsent(sender, k -> new ArrayList<>());

        if (microphonePackets.isEmpty()) {
            for (int i = 0; i < 6; i++) {
                microphonePackets.add(null);
            }
        }

        OpusDecoder decoder = getDecoder(sender);
        if (data == null || data.length <= 0) {
            decoder.resetState();
            return;
        }
        microphonePackets.add(decoder.decode(data));

        effect.severity = 5;

        if (audioPlayer == null) {
            getAudioPlayer().startPlaying();
        }
    }

    private OpusDecoder getDecoder(UUID sender) {
        return decoders.computeIfAbsent(sender, uuid -> SimpleVoiceRadioPlugin.serverApi.createDecoder());
    }

    private AudioPlayer getAudioPlayer() {
        if (audioPlayer == null) {
            VoicechatConnection connection = SimpleVoiceRadioPlugin.serverApi.getConnectionOf(owner);
            EntityAudioChannel channel = SimpleVoiceRadioPlugin.serverApi.createEntityAudioChannel(this.owner, connection.getPlayer());
            audioPlayer = SimpleVoiceRadioPlugin.serverApi.createAudioPlayer(channel, SimpleVoiceRadioPlugin.serverApi.createEncoder(), this);
        }
        return audioPlayer;
    }
}

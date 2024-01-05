package com.accbdd.simplevoiceradio;

import java.awt.image.BufferedImage;
import java.net.URL;
import java.util.Enumeration;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.annotation.Nullable;
import javax.imageio.ImageIO;

import com.accbdd.simplevoiceradio.radio.RadioManager;

import de.maxhenkel.voicechat.api.VoicechatPlugin;
import de.maxhenkel.voicechat.api.VoicechatServerApi;
import de.maxhenkel.voicechat.api.VolumeCategory;
import de.maxhenkel.voicechat.api.events.EventRegistration;
import de.maxhenkel.voicechat.api.events.MicrophonePacketEvent;
import de.maxhenkel.voicechat.api.events.VoicechatServerStartedEvent;

public class SimpleVoiceRadioPlugin implements VoicechatPlugin {
    @Nullable
    public static VoicechatServerApi serverApi;
    @Nullable
    public static VolumeCategory radios;

    private ExecutorService executor;

    public SimpleVoiceRadioPlugin() {
        executor = Executors.newSingleThreadExecutor(runnable -> {
            Thread thread = new Thread(runnable);
            thread.setName("RadioMicrophoneProcessThread");
            thread.setUncaughtExceptionHandler((t, e) -> SimpleVoiceRadio.error("Error in radio process thread: {}", e));
            thread.setDaemon(true);
            return thread;
        });
    }

    //clamps combined packets to not overflow senders
    public static short[] combineAudio(List<short[]> audioParts) {
        short[] result = new short[960];
        int sample;
        for (int i = 0; i < result.length; i++) {
            sample = 0;
            for (short[] audio : audioParts) {
                if (audio == null) {
                    sample += 0;
                } else {
                    sample += audio[i];
                }
            }
            if (sample > Short.MAX_VALUE) {
                result[i] = Short.MAX_VALUE;
            } else if (sample < Short.MIN_VALUE) {
                result[i] = Short.MIN_VALUE;
            } else {
                result[i] = (short) sample;
            }
        }
        return result;
    }

    //registers events
    public void registerEvents(EventRegistration reg) {
        reg.registerEvent(VoicechatServerStartedEvent.class, this::onServerStarted);
        reg.registerEvent(MicrophonePacketEvent.class, microphonePacketEvent -> {
            executor.submit(() -> RadioManager.getInstance().onMicPacket(microphonePacketEvent));
        });
    }

    //registers volume category for radios
    public void onServerStarted(VoicechatServerStartedEvent event) {
        serverApi = event.getVoicechat();

        radios = serverApi.volumeCategoryBuilder()
            .setId(SimpleVoiceRadio.MOD_ID)
            .setName("Radios")
            .setDescription("The volume of radios")
            .setIcon(getIcon("radio_icon.png"))
            .build();

        serverApi.registerVolumeCategory(radios);
    }

    public String getPluginId() {
        return SimpleVoiceRadio.MOD_ID;
    }

    //gets radio icon
    private int[][] getIcon(String path) {
        try {
            Enumeration<URL> resources = SimpleVoiceRadioPlugin.class.getClassLoader().getResources(path);
            while (resources.hasMoreElements()) {
                BufferedImage bufferedImage = ImageIO.read(resources.nextElement().openStream());
                if (bufferedImage.getWidth() != 16) {
                    continue;
                }
                if (bufferedImage.getHeight() != 16) {
                    continue;
                }
                int[][] image = new int[16][16];
                for (int x = 0; x < bufferedImage.getWidth(); x++) {
                    for (int y = 0; y < bufferedImage.getHeight(); y++) {
                        image[x][y] = bufferedImage.getRGB(x, y);
                    }
                }
                return image;
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}

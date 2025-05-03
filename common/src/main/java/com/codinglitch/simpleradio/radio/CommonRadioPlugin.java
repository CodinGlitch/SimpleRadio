package com.codinglitch.simpleradio.radio;

import com.codinglitch.simpleradio.CommonSimpleRadio;
import de.maxhenkel.voicechat.api.VoicechatApi;
import de.maxhenkel.voicechat.api.VoicechatServerApi;
import de.maxhenkel.voicechat.api.VolumeCategory;
import de.maxhenkel.voicechat.api.events.EventRegistration;
import de.maxhenkel.voicechat.api.events.MicrophonePacketEvent;
import de.maxhenkel.voicechat.api.events.VoicechatServerStartedEvent;
import org.jetbrains.annotations.Nullable;
import org.joml.Math;
import org.joml.Vector3f;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.net.URL;
import java.util.Enumeration;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CommonRadioPlugin {
    public static String RADIOS_CATEGORY = "radios";
    public static String SPEAKERS_CATEGORY = "speakers";
    public static String WALKIES_CATEGORY = "walkies";
    public static String TRANSCEIVERS_CATEGORY = "transceivers";

    @Nullable
    public static VolumeCategory speakers;
    @Nullable
    public static VolumeCategory radios;
    @Nullable
    public static VolumeCategory walkies;
    @Nullable
    public static VolumeCategory transceivers;

    @Nullable
    public static VoicechatServerApi serverApi;
    public static VoicechatApi commonApi;

    private ExecutorService executor;

    public CommonRadioPlugin() {
        executor = Executors.newSingleThreadExecutor(runnable -> {
            Thread thread = new Thread(runnable);
            thread.setName("RadioMicrophoneProcessThread");
            thread.setUncaughtExceptionHandler((t, e) -> CommonSimpleRadio.error("Error in radio process thread: {}", e));
            thread.setDaemon(true);
            return thread;
        });
    }

    public static boolean isAudioValid(short[] data) {
        return analyzeActivity(data) > 10f;
    }

    public static float analyzeActivity(short[] data) {
        float activity = 0;
        for (short datum : data) {
            activity += datum*datum;
        }

        return activity / data.length; // equivalent as the sample size of the array seems to always be 960
    }

    public static short[] combineAudio(List<short[]> audioParts) {
        if (audioParts.size() == 1) {
            return audioParts.get(0);
        }

        short[] result = new short[960];

        int sample;
        for (int i = 0; i < result.length; i++) {
            sample = 0;

            // Combining all the audio parts
            if (!audioParts.isEmpty()) {
                for (short[] audio : audioParts) {
                    if (audio == null) {
                        sample += 0;
                    } else {
                        sample += audio[i];
                    }
                }

                // Averaging the audio
                sample /= audioParts.size();
            }

            result[i] = (short) Math.max(Short.MIN_VALUE, Math.min(sample, Short.MAX_VALUE));
        }

        return result;
    }

    public static double getFalloff(float distance, float range) {
        return Math.max(0, 1 - (java.lang.Math.log(1 + distance) / java.lang.Math.log(1 + range)));
    }

    public static double getDoppler(Vector3f sourcePosition, Vector3f sourceVelocity, Vector3f observerPosition, Vector3f observerVelocity) {
        Vector3f sourceToObserver = new Vector3f(
                observerPosition.x - sourcePosition.x,
                observerPosition.y - sourcePosition.y,
                observerPosition.z - sourcePosition.z
        ).normalize();

        float observerFactor = 0;
        if (observerVelocity.x != 0 || observerVelocity.y != 0 || observerVelocity.z != 0) {
            observerFactor = observerVelocity.normalize().dot(sourceToObserver);
        }

        float sourceFactor = 0;
        if (sourceVelocity.x != 0 || sourceVelocity.y != 0 || sourceVelocity.z != 0) {
            sourceFactor = sourceVelocity.normalize().dot(sourceToObserver);
        }

        return (5 + observerVelocity.length()*-observerFactor) /
                (5 + sourceVelocity.length()*-sourceFactor);
    }

    public String getPluginId() {
        return CommonSimpleRadio.ID;
    }

    public void serverTick(int tickCount) {

    }

    public void registerEvents(EventRegistration registration) {
        registration.registerEvent(VoicechatServerStartedEvent.class, this::onServerStarted);
        registration.registerEvent(MicrophonePacketEvent.class, microphonePacketEvent -> executor.submit(() -> RadioManager.getInstance().onMicPacket(microphonePacketEvent)));
    }

    public void onServerStarted(VoicechatServerStartedEvent event) {
        serverApi = event.getVoicechat();

        radios = serverApi.volumeCategoryBuilder()
                .setId(RADIOS_CATEGORY)
                .setName("Radios")
                .setDescription("The volume of radios")
                .setIcon(getIcon("radio_icon.png"))
                .build();
        speakers = serverApi.volumeCategoryBuilder()
                .setId(SPEAKERS_CATEGORY)
                .setName("Speakers")
                .setDescription("The volume of speakers")
                .setIcon(getIcon("transceiver_icon.png"))
                .build();
        walkies = serverApi.volumeCategoryBuilder()
                .setId(WALKIES_CATEGORY)
                .setName("Walkie Talkies")
                .setDescription("The volume of walkie/spuddie talkies")
                .setIcon(getIcon("transceiver_icon.png"))
                .build();
        transceivers = serverApi.volumeCategoryBuilder()
                .setId(TRANSCEIVERS_CATEGORY)
                .setName("Transceivers")
                .setDescription("The volume of transceivers")
                .setIcon(getIcon("transceiver_icon.png"))
                .build();

        serverApi.registerVolumeCategory(radios);
        serverApi.registerVolumeCategory(speakers);
        serverApi.registerVolumeCategory(walkies);
        serverApi.registerVolumeCategory(transceivers);
    }

    private int[][] getIcon(String path) {
        try {
            Enumeration<URL> resources = CommonRadioPlugin.class.getClassLoader().getResources(path);
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

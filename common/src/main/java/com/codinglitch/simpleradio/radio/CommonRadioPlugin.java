package com.codinglitch.simpleradio.radio;

import com.codinglitch.simpleradio.CommonSimpleRadio;
import de.maxhenkel.voicechat.api.VoicechatPlugin;
import de.maxhenkel.voicechat.api.VoicechatServerApi;
import de.maxhenkel.voicechat.api.VolumeCategory;
import de.maxhenkel.voicechat.api.events.EventRegistration;
import de.maxhenkel.voicechat.api.events.MicrophonePacketEvent;
import de.maxhenkel.voicechat.api.events.VoicechatServerStartedEvent;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.net.URL;
import java.util.Enumeration;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Predicate;

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

    public static boolean verifyInventory(Entity entity, Predicate<ItemStack> criteria) {
        if (entity instanceof Player player) {
            return player.getInventory().hasAnyMatching(criteria);
        } else {
            AtomicBoolean result = new AtomicBoolean(false);
            entity.getHandSlots().forEach(stack -> {
                if (criteria.test(stack)) result.set(true);
            });
            return result.get();
        }
    }

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
                .setId(TRANSCEIVERS_CATEGORY)
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

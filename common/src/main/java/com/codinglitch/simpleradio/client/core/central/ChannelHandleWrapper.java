package com.codinglitch.simpleradio.client.core.central;

import com.codinglitch.simpleradio.radio.effects.AudioEffect;
import com.mojang.blaze3d.audio.Channel;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.client.sounds.ChannelAccess;

import java.util.function.Consumer;

public class ChannelHandleWrapper {
    public final ChannelAccess.ChannelHandle channelHandle;
    public AudioEffect effect;
    public String currentSound;
    public SoundInstance instance;

    public ChannelHandleWrapper(ChannelAccess.ChannelHandle channelHandle) {
        this.channelHandle = channelHandle;
    }

    public static ChannelHandleWrapper of(ChannelAccess.ChannelHandle channelHandle) {
        return new ChannelHandleWrapper(channelHandle);
    }

    public void execute(Consumer<Channel> consumer) {
        this.channelHandle.execute(consumer);
    }
}

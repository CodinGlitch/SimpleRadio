package com.codinglitch.simpleradio.client.core;

import com.codinglitch.simpleradio.radio.RadioRouter;
import net.minecraft.client.sounds.ChannelAccess;

import java.util.HashMap;

public class ClientRouterWrapper {
    public final HashMap<Long, ChannelAccess.ChannelHandle> audioChannels = new HashMap<>();
    public final RadioRouter router;

    public ClientRouterWrapper(RadioRouter router) {
        this.router = router;
    }

    public static ClientRouterWrapper of(RadioRouter router) {
        return new ClientRouterWrapper(router);
    }

    public ChannelAccess.ChannelHandle getChannel(long seed) {
        return audioChannels.get(seed);
    }

    public ChannelAccess.ChannelHandle addChannel(long seed, ChannelAccess.ChannelHandle channelHandle) {
        return audioChannels.put(seed, channelHandle);
    }

    public ChannelAccess.ChannelHandle removeChannel(long seed) {
        return audioChannels.remove(seed);
    }
}

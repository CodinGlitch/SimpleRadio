package com.codinglitch.simpleradio.client.core.central;

import com.codinglitch.simpleradio.routers.Router;
import com.mojang.blaze3d.audio.Channel;

import java.util.HashMap;
import java.util.Map;

public class ClientRouterWrapper {
    public final HashMap<Long, ChannelHandleWrapper> audioChannels = new HashMap<>();
    public final Router router;

    public ClientRouterWrapper(Router router) {
        this.router = router;
    }

    public static ClientRouterWrapper of(Router router) {
        return new ClientRouterWrapper(router);
    }

    public ChannelHandleWrapper getChannel(long seed) {
        return audioChannels.get(seed);
    }

    public ChannelHandleWrapper addChannel(long seed, ChannelHandleWrapper channelHandle) {
        return audioChannels.put(seed, channelHandle);
    }

    public ChannelHandleWrapper removeChannel(long seed) {
        return audioChannels.remove(seed);
    }

    public void close() {
        for (Map.Entry<Long, ChannelHandleWrapper> entry : audioChannels.entrySet()) {
            entry.getValue().execute(Channel::destroy);
        }
    }

    @Override
    public String toString() {
        return "Wrapper: " + router.getClass().getSimpleName() + "[" + router.getIdentifier() + "]" + router.getLocation().toString();
    }
}

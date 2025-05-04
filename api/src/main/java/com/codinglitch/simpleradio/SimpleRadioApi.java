package com.codinglitch.simpleradio;

import com.codinglitch.simpleradio.central.ConfigHolder;
import com.codinglitch.simpleradio.routers.Router;

import java.util.UUID;

public abstract class SimpleRadioApi {

    public abstract ConfigHolder getConfig();

    public static void removeRouterSided(UUID uuid, boolean isClient) {
        if (isClient) {
            ClientSimpleRadioApi.getInstance().removeRouter(uuid);
        } else {
            ServerSimpleRadioApi.getInstance().removeRouter(uuid);
        }
    }

    public static void removeRouterSided(Router router, boolean isClient) {
        if (isClient) {
            ClientSimpleRadioApi.getInstance().removeRouter(router);
        } else {
            ServerSimpleRadioApi.getInstance().removeRouter(router);
        }
    }
}

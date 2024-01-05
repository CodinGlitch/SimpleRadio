package com.codinglitch.simpleradio.platform;

import com.codinglitch.simpleradio.CommonSimpleRadio;
import com.codinglitch.simpleradio.platform.services.*;

import java.util.ServiceLoader;

public class ClientServices {
    public static final ClientNetworkingHelper NETWORKING = CommonSimpleRadio.loadService(ClientNetworkingHelper.class);
    public static final ClientRegistryHelper REGISTRY = CommonSimpleRadio.loadService(ClientRegistryHelper.class);

}
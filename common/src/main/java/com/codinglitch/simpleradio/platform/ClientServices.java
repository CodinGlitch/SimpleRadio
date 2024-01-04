package com.codinglitch.simpleradio.platform;

import com.codinglitch.simpleradio.CommonSimpleRadio;
import com.codinglitch.simpleradio.platform.services.ClientRegistryHelper;
import com.codinglitch.simpleradio.platform.services.NetworkingHelper;
import com.codinglitch.simpleradio.platform.services.PlatformHelper;
import com.codinglitch.simpleradio.platform.services.RegistryHelper;

import java.util.ServiceLoader;

public class ClientServices {
    public static final ClientRegistryHelper REGISTRY = CommonSimpleRadio.loadService(ClientRegistryHelper.class);

}
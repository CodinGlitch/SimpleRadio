package com.codinglitch.simpleradio.platform;

import com.codinglitch.simpleradio.CommonSimpleRadio;
import com.codinglitch.simpleradio.platform.services.NetworkingHelper;
import com.codinglitch.simpleradio.platform.services.PlatformHelper;
import com.codinglitch.simpleradio.platform.services.RegistryHelper;

import java.util.ServiceLoader;

public class Services {
    public static final PlatformHelper PLATFORM = CommonSimpleRadio.loadService(PlatformHelper.class);
    public static final NetworkingHelper NETWORKING = CommonSimpleRadio.loadService(NetworkingHelper.class);
    public static final RegistryHelper REGISTRY = CommonSimpleRadio.loadService(RegistryHelper.class);
}
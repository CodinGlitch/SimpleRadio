package com.codinglitch.simpleradio.platform;

import com.codinglitch.simpleradio.CommonSimpleRadio;
import com.codinglitch.simpleradio.platform.services.ClientCompatPlatform;
import com.codinglitch.simpleradio.platform.services.ClientNetworkingHelper;
import com.codinglitch.simpleradio.platform.services.ClientRegistryHelper;
import com.codinglitch.simpleradio.platform.services.ClientRenderingHelper;

public class ClientServices {
    public static final ClientNetworkingHelper NETWORKING = CommonSimpleRadio.loadService(ClientNetworkingHelper.class);
    public static final ClientRegistryHelper REGISTRY = CommonSimpleRadio.loadService(ClientRegistryHelper.class);
    public static final ClientRenderingHelper RENDERING = CommonSimpleRadio.loadService(ClientRenderingHelper.class);
    public static final ClientCompatPlatform COMPAT = CommonSimpleRadio.loadService(ClientCompatPlatform.class);

}
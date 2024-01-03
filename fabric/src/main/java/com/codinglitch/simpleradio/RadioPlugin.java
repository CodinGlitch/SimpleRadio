package com.codinglitch.simpleradio;

import com.codinglitch.simpleradio.radio.CommonRadioPlugin;
import de.maxhenkel.voicechat.api.VoicechatPlugin;
import de.maxhenkel.voicechat.api.events.EventRegistration;
import de.maxhenkel.voicechat.api.events.VoicechatServerStartedEvent;

public class RadioPlugin implements VoicechatPlugin {
    private CommonRadioPlugin common;

    public RadioPlugin() {
        common = new CommonRadioPlugin();
    }

    @Override
    public String getPluginId() {
        return common.getPluginId();
    }
    @Override
    public void registerEvents(EventRegistration registration) {
        common.registerEvents(registration);
    }
}

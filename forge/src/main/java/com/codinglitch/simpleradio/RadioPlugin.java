package com.codinglitch.simpleradio;

import com.codinglitch.simpleradio.radio.CommonRadioPlugin;
import com.codinglitch.simpleradio.radio.RadioManager;
import de.maxhenkel.voicechat.api.ForgeVoicechatPlugin;
import de.maxhenkel.voicechat.api.VoicechatPlugin;
import de.maxhenkel.voicechat.api.events.EventRegistration;
import de.maxhenkel.voicechat.api.events.MicrophonePacketEvent;
import de.maxhenkel.voicechat.api.events.VoicechatServerStartedEvent;

@ForgeVoicechatPlugin
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

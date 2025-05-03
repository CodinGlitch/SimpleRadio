package com.codinglitch.simpleradio;

import com.codinglitch.simpleradio.radio.RadioManager;

public interface ServerSimpleRadioApi extends SimpleRadioApi {
    static ServerSimpleRadioApi getInstance() {
        return RadioManager.getInstance();
    }
}

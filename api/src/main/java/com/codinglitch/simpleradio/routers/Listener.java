package com.codinglitch.simpleradio.routers;

import com.codinglitch.simpleradio.radio.Source;

import java.util.function.UnaryOperator;

public interface Listener extends Router {

    float getRange();
    void setRange(float range);

    void transformer(UnaryOperator<Source> transformer);

    void listen(Source source);
}

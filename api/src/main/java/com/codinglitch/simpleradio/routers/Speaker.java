package com.codinglitch.simpleradio.routers;

public interface Speaker extends Router {

    float getRange();
    void setRange(float range);

    String getCategory();
    void setCategory(String category);

    int getSpeakingTime();
}

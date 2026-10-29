package com.codinglitch.simpleradio.compat;

import com.codinglitch.intermediator.Intermediation;
import com.codinglitch.intermediator.Mediation;
import com.codinglitch.intermediator.Subscriber;
import com.codinglitch.simpleradio.compat.phases.OnDataPhase;

public class CommonInteractionCompat implements Mediation {
    @Override
    public void mediate(Intermediation intermediation) {
        intermediation.subscribe(OnDataPhase.class, Subscriber.Condition.ENABLED, this::onData);
    }

    public void onData(OnDataPhase data) {

    }
}

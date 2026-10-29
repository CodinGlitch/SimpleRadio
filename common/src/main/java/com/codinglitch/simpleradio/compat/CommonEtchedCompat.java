package com.codinglitch.simpleradio.compat;

import com.codinglitch.intermediator.Intermediation;
import com.codinglitch.intermediator.Mediation;
import com.codinglitch.intermediator.Subscriber;
import com.codinglitch.simpleradio.compat.phases.ComputeSoundPhase;

public class CommonEtchedCompat implements Mediation {
    @Override
    public void mediate(Intermediation intermediation) {
        intermediation.subscribe(ComputeSoundPhase.class, Subscriber.Condition.ENABLED, this::computeSound);
    }

    public void computeSound(ComputeSoundPhase data) {}
}

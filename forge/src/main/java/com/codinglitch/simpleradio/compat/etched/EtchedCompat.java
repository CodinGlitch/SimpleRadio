package com.codinglitch.simpleradio.compat.etched;

import com.codinglitch.simpleradio.compat.CommonEtchedCompat;
import com.codinglitch.simpleradio.compat.phases.ComputeSoundPhase;
import gg.moonflower.etched.api.record.PlayableRecord;
import gg.moonflower.etched.api.record.TrackData;

public class EtchedCompat extends CommonEtchedCompat {

    @Override
    public void computeSound(ComputeSoundPhase data) {
        TrackData[] tracks = PlayableRecord.getStackMusic(data.stack).orElse(null);
        if (tracks == null) return;

        data.sound = tracks[0].url();
    }
}

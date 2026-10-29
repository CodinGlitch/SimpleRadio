package com.codinglitch.simpleradio.compat;

import com.codinglitch.intermediator.annotations.Mediary;
import com.codinglitch.simpleradio.CommonSimpleRadio;
import com.codinglitch.simpleradio.SimpleRadioIntermediary;
import com.codinglitch.simpleradio.compat.create.CreateCompat;
import com.codinglitch.simpleradio.compat.etched.EtchedCompat;

@Mediary(name = CommonSimpleRadio.ID)
public class ForgeIntermediary extends SimpleRadioIntermediary {
    @Override
    public void intermediate() {
        super.intermediate();
        begin()
                .add(ETCHED, () -> EtchedCompat::new)
                .add(CREATE, () -> CreateCompat::new)
                .add(COMPUTER_CRAFT, () -> CCCompat::new)
                .add(VALKYRIEN_SKIES, () -> CommonValkyrienCompat::new)
                .add(CURIOS, () -> CuriosCompat::new)
                .mediate();
    }
}

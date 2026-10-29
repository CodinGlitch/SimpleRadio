package com.codinglitch.simpleradio.compat;

import com.codinglitch.intermediator.annotations.Mediary;
import com.codinglitch.simpleradio.CommonSimpleRadio;
import com.codinglitch.simpleradio.SimpleRadioIntermediary;

@Mediary(name = CommonSimpleRadio.ID)
public class FabricIntermediary extends SimpleRadioIntermediary {
    @Override
    public void intermediate() {
        super.intermediate();
        begin()
                .add(VC_INTERACTION, () -> InteractionCompat::new)
                .add(COMPUTER_CRAFT, () -> CCCompat::new)
                .add(VALKYRIEN_SKIES, () -> CommonValkyrienCompat::new)
                .add(TRINKETS, () -> TrinketsCompat::new)
                .mediate();
    }
}

package com.codinglitch.simpleradio.compat;

import com.codinglitch.intermediator.Intermediation;
import com.codinglitch.intermediator.Mediation;
import com.codinglitch.intermediator.Subscriber;
import com.codinglitch.intermediator.phases.PostInitializePhase;
import com.codinglitch.intermediator.phases.PostLoadCompatibilityPhase;
import com.codinglitch.simpleradio.core.events.CollectionEvent;
import com.codinglitch.simpleradio.core.registry.SimpleRadioBlocks;
import net.minecraft.world.level.block.Block;

import java.util.List;

public abstract class CommonCreateCompat implements Mediation {
    @Override
    public void mediate(Intermediation intermediation) {
        intermediation.subscribe(PostInitializePhase.class, Subscriber.Condition.ENABLED, this::postInitialize);
        intermediation.subscribe(PostLoadCompatibilityPhase.class, Subscriber.Condition.ENABLED, this::postLoadCompatibility);
        intermediation.subscribe(CollectionEvent.VerifyEntity.class, Subscriber.Condition.ENABLED, this::verifyEntityCollection);
    }

    public List<Block> getCentralBlocks() {
        return List.of(
                SimpleRadioBlocks.RADIO,
                SimpleRadioBlocks.SPEAKER,
                SimpleRadioBlocks.MICROPHONE,
                SimpleRadioBlocks.RECEIVER,
                SimpleRadioBlocks.TRANSMITTER,
                SimpleRadioBlocks.INSULATOR
        );
    }

    public void verifyEntityCollection(CollectionEvent.VerifyEntity data) {}

    public abstract void postInitialize(PostInitializePhase data);
    public abstract void postLoadCompatibility(PostLoadCompatibilityPhase data);
}

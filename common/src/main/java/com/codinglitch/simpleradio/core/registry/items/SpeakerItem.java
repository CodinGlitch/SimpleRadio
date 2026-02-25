package com.codinglitch.simpleradio.core.registry.items;

import com.codinglitch.simpleradio.central.Module;
import com.codinglitch.simpleradio.central.Speaking;
import com.codinglitch.simpleradio.core.central.Alterable;
import com.codinglitch.simpleradio.core.registry.SimpleRadioBlocks;
import com.codinglitch.simpleradio.core.registry.SimpleRadioModules;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.context.BlockPlaceContext;

public class SpeakerItem extends BlockItem implements Speaking, Alterable {
    public SpeakerItem(Properties settings) {
        super(SimpleRadioBlocks.SPEAKER, settings);
    }

    @Override
    public InteractionResult place(BlockPlaceContext context) {
        return super.place(context);
    }

    @Override
    public boolean canAcceptUpgrade(Module upgrade) {
        return upgrade == SimpleRadioModules.RANGE;
    }
}

package com.codinglitch.simpleradio.compat.phases;

import com.codinglitch.intermediator.Phase;
import net.minecraft.world.level.block.entity.BlockEntity;

public class BlockEntityRemovedPhase implements Phase {
    public BlockEntity blockEntity;

    public BlockEntityRemovedPhase(BlockEntity blockEntity) {
        this.blockEntity = blockEntity;
    }
}

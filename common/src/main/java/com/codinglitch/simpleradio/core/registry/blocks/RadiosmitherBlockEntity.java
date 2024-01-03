package com.codinglitch.simpleradio.core.registry.blocks;

import com.codinglitch.simpleradio.core.registry.SimpleRadioBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public class RadiosmitherBlockEntity extends BlockEntity {
    public RadiosmitherBlockEntity(BlockPos pos, BlockState state) {
        super(SimpleRadioBlockEntities.RADIOSMITHER, pos, state);
    }
}

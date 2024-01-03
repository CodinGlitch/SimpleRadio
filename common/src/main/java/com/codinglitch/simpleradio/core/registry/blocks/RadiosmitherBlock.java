package com.codinglitch.simpleradio.core.registry.blocks;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class RadiosmitherBlock extends Block {

    public static final VoxelShape SHAPE = Shapes.or(
            Block.box(1, 0, 6, 15, 1, 10),
            Block.box(6, 0, 1, 10, 1, 15),

            Block.box(4, 0, 4, 12, 2, 12),
            Block.box(5, 2, 5, 11, 4, 11),
            Block.box(4, 4, 4, 12, 8, 12),
            Block.box(1, 9, 1, 15, 10, 15)
    );

    public RadiosmitherBlock(Properties properties) {
        super(properties);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter getter, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }
}

package com.codinglitch.simpleradio.core.registry.blocks;

import com.codinglitch.simpleradio.CommonSimpleRadio;
import com.codinglitch.simpleradio.core.central.Catalyst;
import com.codinglitch.simpleradio.core.central.Frequency;
import com.codinglitch.simpleradio.core.central.Socket;
import com.codinglitch.simpleradio.core.registry.SimpleRadioCatalysts;
import com.codinglitch.simpleradio.core.registry.entities.Wire;
import com.codinglitch.simpleradio.radio.*;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Stream;

/**
 * A block entity containing a catalyst.
 */
public abstract class CatalyzingBlockEntity extends AuditoryBlockEntity {
    public Catalyst catalyst;
    public boolean catalyzed;

    public CatalyzingBlockEntity(BlockEntityType<?> blockEntityType, BlockPos pos, BlockState state) {
        super(blockEntityType, pos, state);
    }

    public InteractionResult trySwapCatalyst(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult result) {
        ItemStack stack = player.getItemInHand(hand);

        if (this.catalyst == null) {
            if (!stack.isEmpty()) {
                Catalyst catalyst = SimpleRadioCatalysts.fromItem(stack.getItem());
                if (catalyst != null) {
                    stack.shrink(1);
                    this.catalyst = catalyst;

                    if (!level.isClientSide) {
                        level.playSound(null, pos, SoundEvents.ITEM_FRAME_ADD_ITEM, SoundSource.BLOCKS, 1, 1);
                    }

                    return InteractionResult.SUCCESS;
                }
            }
        } else {
            if (stack.isEmpty()) {
                player.setItemInHand(hand, new ItemStack(this.catalyst.associate));
                this.catalyst = null;

                if (!level.isClientSide) {
                    level.playSound(null, pos, SoundEvents.ITEM_FRAME_REMOVE_ITEM, SoundSource.BLOCKS, 1, 1);
                }

                return InteractionResult.SUCCESS;
            }
        }

        return null;
    }

    public static void tick(Level level, BlockPos pos, BlockState blockState, CatalyzingBlockEntity blockEntity) {
        BlockPos adaptorLocation = blockEntity.getAdaptorLocation();

        BlockState state = level.getBlockState(adaptorLocation);
        if (state.isAir()) {
            blockEntity.catalyzed = false;
        } else {
            blockEntity.catalyzed = true;
        }
    }

    public abstract BlockPos getAdaptorLocation();

    @Override
    public void loadTag(CompoundTag tag) {
        super.loadTag(tag);

        if (tag.contains("catalyst")) {
            this.catalyst = SimpleRadioCatalysts.fromLocation(ResourceLocation.tryParse(tag.getString("catalyst")));
        }
    }

    @Override
    public void saveTag(CompoundTag tag) {
        super.saveTag(tag);

        if (this.catalyst != null) {
            tag.putString("catalyst", this.catalyst.location.toString());
        }
    }
}

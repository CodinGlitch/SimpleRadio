package com.codinglitch.simpleradio.core.central;

import com.codinglitch.simpleradio.radio.RadioReceiver;
import com.codinglitch.simpleradio.radio.RadioSpeaker;
import com.codinglitch.simpleradio.radio.RadioListener;
import com.codinglitch.simpleradio.radio.RadioTransmitter;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nullable;
import java.util.UUID;

public abstract class CentralBlockEntity extends BlockEntity {
    public Frequency frequency;

    public UUID id;

    @Nullable
    public RadioReceiver receiver;

    @Nullable
    public RadioTransmitter transmitter;

    @Nullable
    public RadioListener listener;

    @Nullable
    public RadioSpeaker speaker;

    public CentralBlockEntity(BlockEntityType<?> blockEntityType, BlockPos pos, BlockState state) {
        super(blockEntityType, pos, state);
        this.id = UUID.randomUUID();
    }
}

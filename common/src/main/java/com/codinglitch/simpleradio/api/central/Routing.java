package com.codinglitch.simpleradio.api.central;

import com.codinglitch.simpleradio.radio.*;
import net.minecraft.world.level.block.state.BlockState;

import java.util.UUID;

public interface Routing {
    default RadioReceiver getOrCreateReceiver(WorldlyPosition location, Frequency frequency, UUID id, BlockState state) {return null;}
    default RadioTransmitter getOrCreateTransmitter(WorldlyPosition location, Frequency frequency, UUID id, BlockState state) {return null;}

    default RadioListener getOrCreateListener(WorldlyPosition location, UUID id, BlockState state) {return null;}
    default RadioSpeaker getOrCreateSpeaker(WorldlyPosition location, UUID id, BlockState state) {return null;}

    default RadioRouter getOrCreateRouter(WorldlyPosition location, UUID id, BlockState state) {return null;}
}

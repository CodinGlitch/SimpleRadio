package com.codinglitch.simpleradio.central;

import com.codinglitch.simpleradio.routers.*;
import net.minecraft.world.level.block.state.BlockState;

import java.util.UUID;

public interface Routing {
    default Receiver getOrCreateReceiver(WorldlyPosition location, Frequency frequency, UUID id, BlockState state) {return null;}
    default Transmitter getOrCreateTransmitter(WorldlyPosition location, Frequency frequency, UUID id, BlockState state) {return null;}

    default Listener getOrCreateListener(WorldlyPosition location, UUID id, BlockState state) {return null;}
    default Speaker getOrCreateSpeaker(WorldlyPosition location, UUID id, BlockState state) {return null;}

    default Router getOrCreateRouter(WorldlyPosition location, UUID id, BlockState state) {return null;}
}

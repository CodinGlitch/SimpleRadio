package com.codinglitch.simpleradio.core.networking;

import com.codinglitch.simpleradio.core.central.Packeter;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.syncher.EntityDataSerializer;
import net.minecraft.network.syncher.EntityDataSerializers;

import java.util.function.Consumer;

public class SimpleRadioNetworking {
    public static final EntityDataSerializer<Short> SHORT = EntityDataSerializer.simple((buf, s) -> buf.writeShort(s), FriendlyByteBuf::readShort);

    static {
        EntityDataSerializers.registerSerializer(SHORT);
    }

    public static void load(Consumer<Packeter> packet) {

    }
}

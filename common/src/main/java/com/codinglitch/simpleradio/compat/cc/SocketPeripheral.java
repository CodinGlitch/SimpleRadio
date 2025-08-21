package com.codinglitch.simpleradio.compat.cc;

import com.codinglitch.simpleradio.central.Socket;
import com.codinglitch.simpleradio.routers.Router;
import dan200.computercraft.api.lua.*;
import dan200.computercraft.api.peripheral.AttachedComputerSet;
import dan200.computercraft.api.peripheral.IComputerAccess;
import dan200.computercraft.api.peripheral.IPeripheral;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jetbrains.annotations.Nullable;
import org.joml.Math;

import java.util.Optional;
import java.util.UUID;

public class SocketPeripheral<T extends BlockEntity & Socket> implements IPeripheral {
    private final AttachedComputerSet computers = new AttachedComputerSet();
    private final T socket;

    public SocketPeripheral(BlockEntity socket) {
        this.socket = (T) socket; // funny cast
        CommonCCCompat.putPeripheral(socket, this);
    }

    @Override
    public void attach(IComputerAccess computer) {
        this.computers.add(computer);
    }

    @Override
    public void detach(IComputerAccess computer) {
        this.computers.remove(computer);
    }

    @Override
    public String getType() {
        Block block = this.socket.getBlockState().getBlock();
        ResourceLocation resource = BuiltInRegistries.BLOCK.getKey(block);

        return resource.getPath();
    }

    @LuaFunction
    public final UUID getReference() {
        Router router = socket.getRouter();
        if (router == null) return null;
        return router.getReference();
    }

    @LuaFunction
    public final float getActivity() {
        Router router = socket.getRouter();
        if (router == null) return -1;
        return router.getActivity();
    }

    @LuaFunction(
            unsafe = true
    )
    public final boolean route(ILuaContext context, LuaTable<?, ?> audio, Optional<Double> volume) throws LuaException {
        Router router = socket.getRouter();
        if (router == null) return false;

        // absolutely STOLEN from the speaker code
        LuaValues.checkFinite(1, volume.orElse(0.0));
        int length = audio.length();
        if (length <= 0) {
            throw new LuaException("Cannot play empty audio");
        } else if (length > 131072) {
            throw new LuaException("Audio data is too large");
        } else {
            for (int x = 0; x < Math.floor(length/960d); x++) {
                short[] data = new short[960];
                for (int i = 1; i < 960; i++) {
                    int level = audio.getInt(x*960 + i);
                    if (level < Short.MIN_VALUE || level > Short.MAX_VALUE) {
                        throw new LuaException("table item #" + i + " must be between -32768 and 32767");
                    }

                    data[i] = (short) level;
                }
                router.send(data, volume.orElse(1d).floatValue());
            }

            return true;
        }
    }

    @Override
    public boolean equals(@Nullable IPeripheral other) {
        return other instanceof SocketPeripheral<?> o && socket == o.socket;
    }

}

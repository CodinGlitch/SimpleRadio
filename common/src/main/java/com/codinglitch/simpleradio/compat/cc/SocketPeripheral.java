package com.codinglitch.simpleradio.compat.cc;

import com.codinglitch.simpleradio.central.Socket;
import com.codinglitch.simpleradio.radio.RadioRouter;
import com.codinglitch.simpleradio.radio.Source;
import com.codinglitch.simpleradio.routers.Router;
import dan200.computercraft.api.lua.*;
import dan200.computercraft.api.peripheral.AttachedComputerSet;
import dan200.computercraft.api.peripheral.IComputerAccess;
import dan200.computercraft.api.peripheral.IPeripheral;
import de.maxhenkel.voicechat.api.opus.OpusDecoder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jetbrains.annotations.Nullable;
import org.joml.Math;

import java.util.*;

public class SocketPeripheral<T extends BlockEntity & Socket> implements IPeripheral {
    private final AttachedComputerSet computers = new AttachedComputerSet();
    private final T socket;

    public SocketPeripheral(BlockEntity socket) {
        this.socket = (T) socket; // funny cast
        CommonCCCompat.putPeripheral(socket, this);
    }

    public void accept(Router router, Source source) {
        RadioRouter radioRouter = (RadioRouter) router;
        OpusDecoder decoder = radioRouter.getDecoder(source.getOwner());

        LuaTable<?, ?> data;
        String sound;
        byte[] encodedData = source.getData();
        if (encodedData == null) {
            data = null;
            sound = source.getSound();
        } else {
            sound = null;

            short[] decoded = decoder.decode(source.getData());
            Map<Integer, Short> mapped = new HashMap<>();
            for (int i = 0; i < decoded.length; i++) {
                mapped.put(i+1, decoded[i]);
            }
            data = new ObjectLuaTable(mapped);
        }

        float power = source.getPower();

        computers.forEach((computer) -> {
            computer.queueEvent("receive_signal", data == null ? sound : data, power);
        });
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

        return resource.toString();
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
            List<short[]> datas = new ArrayList<>();
            for (int x = 0; x < Math.floor(length/960d); x++) {
                short[] data = new short[960];
                for (int i = 1; i < 960; i++) {
                    int level = audio.getInt(x*960 + i);
                    if (level < Short.MIN_VALUE || level > Short.MAX_VALUE) {
                        throw new LuaException("table item #" + i + " must be between -32768 and 32767");
                    }

                    data[i] = (short) level;
                }

                datas.add(data);
            }

            context.executeMainThreadTask(() -> {
                for (short[] data : datas) {
                    router.send(data, volume.orElse(1d).floatValue());
                }
                return new Object[0];
            });

            return true;
        }
    }

    @Override
    public boolean equals(@Nullable IPeripheral other) {
        return other instanceof SocketPeripheral<?> o && socket == o.socket;
    }
}

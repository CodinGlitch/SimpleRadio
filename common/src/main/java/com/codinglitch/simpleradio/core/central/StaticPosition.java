package com.codinglitch.simpleradio.core.central;

import com.codinglitch.simpleradio.radio.RadioChannel;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.Nullable;
import org.joml.Math;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class StaticPosition extends BlockPos {
    public ServerLevel level;

    public StaticPosition(int x, int y, int z, ServerLevel level) {
        super(x, y, z);
        this.level = level;
    }

    public static StaticPosition of(BlockPos pos, ServerLevel level) {
        return new StaticPosition(pos.getX(), pos.getY(), pos.getZ(), level);
    }
}

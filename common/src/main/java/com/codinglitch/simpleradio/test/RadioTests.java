package com.codinglitch.simpleradio.test;

import com.codinglitch.simpleradio.ServerSimpleRadioApi;
import com.codinglitch.simpleradio.SimpleRadioApi;
import com.codinglitch.simpleradio.central.WorldlyPosition;
import com.codinglitch.simpleradio.routers.Router;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestAssertException;
import net.minecraft.gametest.framework.GameTestAssertPosException;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;

import java.util.Optional;

public class RadioTests {
    public static <T extends Router> T assertRouter(final GameTestHelper helper, Class<T> type, BlockPos pos) {
        Optional<Router> router =
                ServerSimpleRadioApi.getInstance().getRouters().stream().filter(r ->
                        relativePos(helper, r.getLocation()).equals(pos) && type.isAssignableFrom(r.getClass())
                ).findFirst();

        if (router.isEmpty()) {
            throw new GameTestAssertPosException(type.getSimpleName()+" not found at location", helper.absolutePos(pos), pos, helper.getTick());
        }

        return (T) router.get();
    }

    public static <T extends Router> void assertNoRouter(final GameTestHelper helper, Class<T> type, BlockPos pos) {
        Optional<Router> router =
                ServerSimpleRadioApi.getInstance().getRouters().stream().filter(r ->
                        relativePos(helper, r.getLocation()).equals(pos) && type.isAssignableFrom(r.getClass())
                ).findFirst();

        if (router.isPresent()) {
            throw new GameTestAssertPosException(type.getSimpleName()+" found at location", helper.absolutePos(pos), pos, helper.getTick());
        }
    }

    public static BlockPos relativePos(final GameTestHelper helper, WorldlyPosition pos) {
        BlockPos blockpos = helper.absolutePos(BlockPos.ZERO);
        BlockPos blockpos1 = StructureTemplate.transform(pos.blockPos(), Mirror.NONE, helper.getTestRotation(), blockpos);
        return blockpos1.subtract(blockpos);
    }
}

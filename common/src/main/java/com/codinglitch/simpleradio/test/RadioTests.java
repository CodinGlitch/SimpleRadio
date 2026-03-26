package com.codinglitch.simpleradio.test;

import com.codinglitch.simpleradio.ServerSimpleRadioApi;
import com.codinglitch.simpleradio.SimpleRadioApi;
import com.codinglitch.simpleradio.routers.Router;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestAssertException;
import net.minecraft.gametest.framework.GameTestAssertPosException;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Optional;

public class RadioTests {
    public static <T extends Router> T assertRouter(final GameTestHelper helper, Class<T> type, BlockPos pos) {
        Optional<Router> router = ServerSimpleRadioApi.getInstance().getRouters()
                .stream().filter(r -> helper.relativePos(r.getLocation().blockPos()).equals(pos))
                .findFirst();

        if (router.isEmpty()) {
            throw new GameTestAssertPosException("Router not found at location", helper.absolutePos(pos), pos, helper.getTick());
        }

        if (router.get().getClass().isAssignableFrom(type)) {
            throw new GameTestAssertPosException("Router is incorrect type "+type.getName()+" != "+router.get().getClass().getName(), helper.absolutePos(pos), pos, helper.getTick());
        }

        return (T) router.get();
    }
}

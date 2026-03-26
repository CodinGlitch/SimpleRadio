package com.codinglitch.simpleradio.test;

import com.codinglitch.simpleradio.ServerSimpleRadioApi;
import com.codinglitch.simpleradio.central.WorldlyPosition;
import com.codinglitch.simpleradio.radio.RadioListener;
import com.codinglitch.simpleradio.radio.RadioManager;
import com.codinglitch.simpleradio.radio.RadioSpeaker;
import com.codinglitch.simpleradio.routers.Listener;
import com.codinglitch.simpleradio.routers.Speaker;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.sounds.SoundEvents;

import java.util.concurrent.atomic.AtomicBoolean;

public class SystemTest {

    @GameTest(template = "simpleradio:system/closed_system")
    public void testClosedSystem(final GameTestHelper helper) {
        helper.runAtTickTime(5, () -> {
            RadioTests.assertRouter(helper, Listener.class, new BlockPos(-1, 2, -1));
            RadioSpeaker speaker = (RadioSpeaker) RadioTests.assertRouter(helper, Speaker.class, new BlockPos(-3, 2, -3));

            AtomicBoolean received = new AtomicBoolean(false);
            speaker.acceptCriteria = source -> {
                received.set(true);
                return true;
            };

            ServerSimpleRadioApi.getInstance().sendSound(
                    WorldlyPosition.of(
                            helper.absolutePos(new BlockPos(-1, 2, -1)),
                            helper.getLevel()
                    ),
                    SoundEvents.ALLAY_AMBIENT_WITH_ITEM,
                    1, 1, 1
            );

            helper.succeedWhen(received::get);
        });
    }
}

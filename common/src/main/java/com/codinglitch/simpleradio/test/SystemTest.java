package com.codinglitch.simpleradio.test;

import com.codinglitch.simpleradio.ServerSimpleRadioApi;
import com.codinglitch.simpleradio.central.WorldlyPosition;
import com.codinglitch.simpleradio.core.registry.SimpleRadioEntities;
import com.codinglitch.simpleradio.radio.RadioSpeaker;
import com.codinglitch.simpleradio.routers.*;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.phys.Vec3;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

public class SystemTest {

    @GameTest(template = "simpleradio:system/all_routers")
    public void testRouterRegistry(final GameTestHelper helper) {
        helper.runAtTickTime(5, () -> {
            RadioTests.assertRouter(helper, Speaker.class, new BlockPos(2, 2, 2));
            RadioTests.assertRouter(helper, Listener.class, new BlockPos(1, 2, 2));
            RadioTests.assertRouter(helper, Router.class, new BlockPos(0, 2, 2));

            RadioTests.assertRouter(helper, Receiver.class, new BlockPos(2, 2, 1));
            RadioTests.assertRouter(helper, Speaker.class, new BlockPos(2, 2, 1));

            RadioTests.assertRouter(helper, Transmitter.class, new BlockPos(1, 2, 1));
            RadioTests.assertRouter(helper, Receiver.class, new BlockPos(0, 2, 1));

            RadioTests.assertRouter(helper, Listener.class, new BlockPos(2, 2, 0));
            RadioTests.assertRouter(helper, Speaker.class, new BlockPos(2, 2, 0));
            RadioTests.assertRouter(helper, Receiver.class, new BlockPos(2, 2, 0));
            RadioTests.assertRouter(helper, Transmitter.class, new BlockPos(2, 2, 0));

            helper.succeed();
        });
    }

    @GameTest(template = "simpleradio:system/all_routers")
    public void testGarbageCollection(final GameTestHelper helper) {
        helper.runAtTickTime(20, () -> {
            helper.destroyBlock(new BlockPos(2, 2, 2));
            helper.destroyBlock(new BlockPos(1, 2, 2));
            helper.destroyBlock(new BlockPos(0, 2, 2));
            helper.destroyBlock(new BlockPos(2, 2, 1));
            helper.destroyBlock(new BlockPos(1, 2, 1));
            helper.destroyBlock(new BlockPos(0, 2, 1));

            helper.killAllEntities();
        });

        helper.runAtTickTime(50, () -> {
            RadioTests.assertNoRouter(helper, Speaker.class, new BlockPos(2, 2, 2));
            RadioTests.assertNoRouter(helper, Listener.class, new BlockPos(1, 2, 2));
            RadioTests.assertNoRouter(helper, Router.class, new BlockPos(0, 2, 2));

            RadioTests.assertNoRouter(helper, Receiver.class, new BlockPos(2, 2, 1));
            RadioTests.assertNoRouter(helper, Speaker.class, new BlockPos(2, 2, 1));

            RadioTests.assertNoRouter(helper, Transmitter.class, new BlockPos(1, 2, 1));
            RadioTests.assertNoRouter(helper, Receiver.class, new BlockPos(0, 2, 1));

            RadioTests.assertNoRouter(helper, Listener.class, new BlockPos(2, 2, 0));
            RadioTests.assertNoRouter(helper, Speaker.class, new BlockPos(2, 2, 0));
            RadioTests.assertNoRouter(helper, Receiver.class, new BlockPos(2, 2, 0));
            RadioTests.assertNoRouter(helper, Transmitter.class, new BlockPos(2, 2, 0));

            helper.succeed();
        });
    }

    @GameTest(template = "simpleradio:system/closed_system")
    public void testClosedSystem(final GameTestHelper helper) {
        AtomicBoolean received = new AtomicBoolean(false);

        helper.runAfterDelay(10, () -> {
            RadioTests.assertRouter(helper, Listener.class, new BlockPos(0, 2, 0));
            RadioSpeaker speaker = (RadioSpeaker) RadioTests.assertRouter(helper, Speaker.class, new BlockPos(2, 2, 2));

            speaker.acceptCriteria = source -> {
                received.set(true);
                return true;
            };

            ServerSimpleRadioApi.getInstance().sendAudio(
                    WorldlyPosition.of(
                            helper.absolutePos(new BlockPos(0, 2, 0)),
                            helper.getLevel()
                    ),
                    UUID.randomUUID(),
                    new byte[] {}
            );

        });

        helper.onEachTick(() -> {
            if (received.get()) helper.succeed();
        });
    }

    @GameTest(template = "simpleradio:system/wire_survival")
    public void testWireSurvival(final GameTestHelper helper) {
        helper.runAfterDelay(10, () -> {
            helper.assertEntityPresent(SimpleRadioEntities.WIRE, new BlockPos(1, 1, 1), 4);
            helper.succeed();
        });
    }

    @GameTest(template = "simpleradio:system/open_system")
    public void testOpenSystem(final GameTestHelper helper) {
        AtomicBoolean received = new AtomicBoolean(false);

        helper.runAfterDelay(10, () -> {
            RadioTests.assertRouter(helper, Listener.class, new BlockPos(0, 2, 0));
            RadioSpeaker speaker = (RadioSpeaker) RadioTests.assertRouter(helper, Speaker.class, new BlockPos(2, 2, 0));

            speaker.acceptCriteria = source -> {
                received.set(true);
                return true;
            };

            ServerSimpleRadioApi.getInstance().sendAudio(
                    WorldlyPosition.of(
                            helper.absolutePos(new BlockPos(0, 2, 0)),
                            helper.getLevel()
                    ),
                    UUID.randomUUID(),
                    new byte[] {}
            );

        });

        helper.onEachTick(() -> {
            if (received.get()) helper.succeed();
        });
    }

    @GameTest(template = "simpleradio:system/short_circuit")
    public void testShortCircuit(final GameTestHelper helper) {
        helper.runAfterDelay(10, () -> {
            Router router = RadioTests.assertRouter(helper, Router.class, new BlockPos(0, 2, 1));

            router.send(new byte[] {}, 0);

            helper.runAfterDelay(40, () -> {
                helper.assertEntityNotPresent(SimpleRadioEntities.WIRE,
                        helper.absoluteVec(new Vec3(0, 1, 1)),
                        helper.absoluteVec(new Vec3(1, 3, 2))
                );
                helper.succeed();
            });
        });
    }

    @GameTest(template = "simpleradio:system/transceiver_ground_to_ground")
    public void testTransceiverGroundToGround(final GameTestHelper helper) {
        AtomicBoolean received = new AtomicBoolean(false);

        helper.runAtTickTime(5, () -> {
            RadioSpeaker speaker = (RadioSpeaker) RadioTests.assertRouter(helper, Speaker.class, new BlockPos(4, 2, 0));
            speaker.acceptCriteria = source -> {
                received.set(true);
                return true;
            };

            ServerSimpleRadioApi.getInstance().sendAudio(
                    WorldlyPosition.of(
                            helper.absolutePos(new BlockPos(1, 2, 0)),
                            helper.getLevel()
                    ),
                    UUID.randomUUID(),
                    new byte[] {}
            );
        });

        helper.onEachTick(() -> {
            if (received.get()) helper.succeed();
        });
    }
}

package com.codinglitch.simpleradio.gametest.tests;

import com.codinglitch.simpleradio.CommonSimpleRadio;
import com.codinglitch.simpleradio.ServerSimpleRadioApi;
import com.codinglitch.simpleradio.central.WorldlyPosition;
import com.codinglitch.simpleradio.core.registry.SimpleRadioEntities;
import com.codinglitch.simpleradio.gametest.SimpleRadioTestHelper;
import com.codinglitch.simpleradio.gametest.SimpleRadioTests;
import com.codinglitch.simpleradio.gametest.TestAlteration;
import com.codinglitch.simpleradio.gametest.TestHolder;
import com.codinglitch.simpleradio.radio.RadioSpeaker;
import com.codinglitch.simpleradio.routers.*;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.phys.Vec3;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

@TestHolder(
        namespace = CommonSimpleRadio.ID,
        dir = "system",

        name = "systems"
)
public class SystemTest {

    @GameTest(template = "all_routers")
    public static void routerRegistry(SimpleRadioTestHelper helper) {
        helper.runAtTickTime(5, () -> {
            helper.assertRouter(Speaker.class, new BlockPos(2, 2, 2));
            helper.assertRouter(Listener.class, new BlockPos(1, 2, 2));
            helper.assertRouter(Router.class, new BlockPos(0, 2, 2));

            helper.assertRouter(Receiver.class, new BlockPos(2, 2, 1));
            helper.assertRouter(Speaker.class, new BlockPos(2, 2, 1));

            helper.assertRouter(Transmitter.class, new BlockPos(1, 2, 1));
            helper.assertRouter(Receiver.class, new BlockPos(0, 2, 1));

            helper.assertRouter(Listener.class, new BlockPos(2, 2, 0));
            helper.assertRouter(Speaker.class, new BlockPos(2, 2, 0));
            helper.assertRouter(Receiver.class, new BlockPos(2, 2, 0));
            helper.assertRouter(Transmitter.class, new BlockPos(2, 2, 0));

            helper.succeed();
        });
    }

    @GameTest(template = "all_routers")
    public static void garbageCollection(SimpleRadioTestHelper helper) {
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
            helper.assertNoRouter(Speaker.class, new BlockPos(2, 2, 2));
            helper.assertNoRouter(Listener.class, new BlockPos(1, 2, 2));
            helper.assertNoRouter(Router.class, new BlockPos(0, 2, 2));

            helper.assertNoRouter(Receiver.class, new BlockPos(2, 2, 1));
            helper.assertNoRouter(Speaker.class, new BlockPos(2, 2, 1));

            helper.assertNoRouter(Transmitter.class, new BlockPos(1, 2, 1));
            helper.assertNoRouter(Receiver.class, new BlockPos(0, 2, 1));

            helper.assertNoRouter(Listener.class, new BlockPos(2, 2, 0));
            helper.assertNoRouter(Speaker.class, new BlockPos(2, 2, 0));
            helper.assertNoRouter(Receiver.class, new BlockPos(2, 2, 0));
            helper.assertNoRouter(Transmitter.class, new BlockPos(2, 2, 0));

            helper.succeed();
        });
    }

    @GameTest(template = "closed_system")
    public static void closedSystem(SimpleRadioTestHelper helper) {
        AtomicBoolean received = new AtomicBoolean(false);

        helper.runAfterDelay(10, () -> {
            helper.assertRouter(Listener.class, new BlockPos(0, 2, 0));
            RadioSpeaker speaker = (RadioSpeaker) helper.assertRouter(Speaker.class, new BlockPos(2, 2, 2));

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

    @GameTest(template = "wire_survival")
    public static void wireSurvival(SimpleRadioTestHelper helper) {
        helper.runAfterDelay(10, () -> {
            helper.assertEntityPresent(SimpleRadioEntities.WIRE, new BlockPos(1, 1, 1), 4);
            helper.succeed();
        });
    }

    @GameTest(template = "open_system")
    public static void openSystem(SimpleRadioTestHelper helper) {
        AtomicBoolean received = new AtomicBoolean(false);

        helper.runAfterDelay(10, () -> {
            helper.assertRouter(Listener.class, new BlockPos(0, 2, 0));
            RadioSpeaker speaker = (RadioSpeaker) helper.assertRouter(Speaker.class, new BlockPos(2, 2, 0));

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

    @GameTest(template = "short_circuit")
    public static void shortCircuit(SimpleRadioTestHelper helper) {
        helper.runAfterDelay(10, () -> {
            Router router = helper.assertRouter(Router.class, new BlockPos(0, 2, 1));

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

    @GameTest(template = "transceiver_ground_to_ground")
    public static void transceiverGroundToGround(SimpleRadioTestHelper helper) {
        AtomicBoolean received = new AtomicBoolean(false);

        helper.runAtTickTime(5, () -> {
            RadioSpeaker speaker = (RadioSpeaker) helper.assertRouter(Speaker.class, new BlockPos(4, 2, 0));
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

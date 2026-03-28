package com.codinglitch.simpleradio.core;

import com.codinglitch.simpleradio.CommonSimpleRadio;
import com.codinglitch.simpleradio.test.SystemTest;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

@GameTestHolder(CommonSimpleRadio.ID)
@PrefixGameTestTemplate(value = false)
public class NeoSystemTest extends SystemTest {
    @GameTest(template = "system/all_routers")
    public void testRouterRegistry(final GameTestHelper helper) {
        super.testRouterRegistry(helper);
    }

    @GameTest(template = "system/all_routers")
    public void testGarbageCollection(final GameTestHelper helper) {
        super.testGarbageCollection(helper);
    }

    @GameTest(template = "system/closed_system")
    public void testClosedSystem(final GameTestHelper helper) {
        super.testClosedSystem(helper);
    }

    @GameTest(template = "system/wire_survival")
    public void testWireSurvival(final GameTestHelper helper) {
        super.testWireSurvival(helper);
    }

    @GameTest(template = "system/open_system")
    public void testOpenSystem(final GameTestHelper helper) {
        super.testOpenSystem(helper);
    }

    @GameTest(template = "system/short_circuit")
    public void testShortCircuit(final GameTestHelper helper) {
        super.testShortCircuit(helper);
    }

    @GameTest(template = "system/transceiver_ground_to_ground")
    public void testTransceiverGroundToGround(final GameTestHelper helper) {
        super.testTransceiverGroundToGround(helper);
    }
}

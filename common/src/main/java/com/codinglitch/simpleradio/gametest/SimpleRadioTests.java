package com.codinglitch.simpleradio.gametest;

import com.codinglitch.simpleradio.ServerSimpleRadioApi;
import com.codinglitch.simpleradio.central.WorldlyPosition;
import com.codinglitch.simpleradio.gametest.tests.SystemTest;
import com.codinglitch.simpleradio.routers.Router;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.*;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.*;
import java.util.stream.Stream;

public class SimpleRadioTests {
    @GameTestGenerator
    public static Collection<TestFunction> generateTests() {
        return Stream.of(SystemTest.class)
                .map(SimpleRadioTestFunction::from)
                .flatMap(Collection::stream)
                .toList();
    }
}

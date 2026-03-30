package com.codinglitch.simpleradio.gametest;

import com.codinglitch.simpleradio.ServerSimpleRadioApi;
import com.codinglitch.simpleradio.central.WorldlyPosition;
import com.codinglitch.simpleradio.mixin.MixinGameTestHelper;
import com.codinglitch.simpleradio.routers.Router;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestAssertPosException;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.gametest.framework.GameTestInfo;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.List;
import java.util.Optional;

public class SimpleRadioTestHelper extends GameTestHelper {
    protected final GameTestInfo testInfo; // they made it private 🙄

    public SimpleRadioTestHelper(GameTestInfo testInfo) {
        super(testInfo);
        this.testInfo = testInfo;
    }

    public SimpleRadioTestHelper(GameTestHelper helper) {
        this(((MixinGameTestHelper) helper).getTestInfo());
    }

    public <T extends Router> T assertRouter(Class<T> type, BlockPos pos) {
        Optional<Router> router =
                ServerSimpleRadioApi.getInstance().getRouters().stream().filter(r ->
                        relativePos(r.getLocation()).equals(pos) && type.isAssignableFrom(r.getClass())
                ).findFirst();

        if (router.isEmpty()) {
            throw new GameTestAssertPosException(type.getSimpleName()+" not found at location", absolutePos(pos), pos, getTick());
        }

        return (T) router.get();
    }

    public <T extends Router> void assertNoRouter(Class<T> type, BlockPos pos) {
        Optional<Router> router =
                ServerSimpleRadioApi.getInstance().getRouters().stream().filter(r ->
                        relativePos(r.getLocation()).equals(pos) && type.isAssignableFrom(r.getClass())
                ).findFirst();

        if (router.isPresent()) {
            throw new GameTestAssertPosException(type.getSimpleName()+" found at location", absolutePos(pos), pos, getTick());
        }
    }

    public BlockPos relativePos(WorldlyPosition pos) {
        BlockPos blockpos = absolutePos(BlockPos.ZERO);
        BlockPos blockpos1 = StructureTemplate.transform(pos.blockPos(), Mirror.NONE, testInfo.getRotation(), blockpos);
        return blockpos1.subtract(blockpos);
    }

    public void assertEntityNotPresent(EntityType<?> type, Vec3 from, Vec3 to) {
        List<? extends Entity> list = getLevel().getEntities(type, new AABB(from, to), Entity::isAlive);
        if (!list.isEmpty()) {
            throw new GameTestAssertPosException("Did not expect " + type.toShortString() + " between ", BlockPos.containing(from), BlockPos.containing(to), getTick());
        }
    }
}

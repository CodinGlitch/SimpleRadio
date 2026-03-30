package com.codinglitch.simpleradio.mixin;

import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.gametest.framework.GameTestInfo;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(GameTestHelper.class)
public interface MixinGameTestHelper {
    @Accessor
    GameTestInfo getTestInfo();
}

package com.codinglitch.simpleradio.mixin;

import com.codinglitch.simpleradio.compat.create.CreateCompat;
import com.simibubi.create.content.contraptions.Contraption;
import com.simibubi.create.content.contraptions.StructureTransform;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import org.apache.commons.lang3.tuple.Pair;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.Iterator;
import java.util.Map;

@Mixin(Contraption.class)
public abstract class MixinContraption {

    @Shadow protected Map<BlockPos, StructureTemplate.StructureBlockInfo> blocks;

    // this only runs on the server but it's kinda fine because the routers are re-created in the contraption
    @Inject(method = "addBlock", at = @At("TAIL"), remap = false)
    private void simpleradio$addBlock(Level level, BlockPos pos, Pair<StructureTemplate.StructureBlockInfo, BlockEntity> pair, CallbackInfo ci) {
        CreateCompat.contraptionAddBlock((Contraption) (Object) this, pos, pair.getValue(), pair.getKey());
    }
}

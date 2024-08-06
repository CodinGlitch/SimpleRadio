package com.codinglitch.simpleradio.mixin;

import com.codinglitch.simpleradio.compat.create.CreateCompat;
import com.simibubi.create.content.contraptions.Contraption;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import org.apache.commons.lang3.tuple.Pair;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Contraption.class)
public abstract class MixinContraption {

    @Inject(method = "addBlock", at = @At("TAIL"), remap = false)
    private void simpleradio$addBlock(BlockPos pos, Pair<StructureTemplate.StructureBlockInfo, BlockEntity> pair, CallbackInfo ci) {
        CreateCompat.contraptionAddBlock((Contraption) (Object) this, pos, pair.getValue(), pair.getKey());
    }
}

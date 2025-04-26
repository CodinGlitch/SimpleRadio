package com.codinglitch.simpleradio.mixin;

import com.codinglitch.simpleradio.CompatCore;
import com.codinglitch.simpleradio.compat.create.CreateCompat;
import com.simibubi.create.content.contraptions.AbstractContraptionEntity;
import com.simibubi.create.content.contraptions.Contraption;
import com.simibubi.create.content.contraptions.ContraptionDisassemblyPacket;
import com.simibubi.create.content.contraptions.StructureTransform;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraftforge.entity.IEntityAdditionalSpawnData;
import org.apache.commons.lang3.tuple.Pair;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Map;

@Mixin(AbstractContraptionEntity.class)
public abstract class MixinAbstractContraptionEntity extends Entity implements IEntityAdditionalSpawnData {

    @Shadow protected Contraption contraption;

    public MixinAbstractContraptionEntity(EntityType<?> type, Level level) {
        super(type, level);
    }

    @Inject(method = "Lcom/simibubi/create/content/contraptions/AbstractContraptionEntity;moveCollidedEntitiesOnDisassembly(Lcom/simibubi/create/content/contraptions/StructureTransform;)V", at = @At("TAIL"), remap = false, require = 0)
    private void simpleradio$moveCollidedEntitiesOnDisassembly_resetRouters(StructureTransform transform, CallbackInfo ci) {
        if (!CompatCore.CREATE.enabled) return;
        if (this.contraption == null) return;
        for (StructureTemplate.StructureBlockInfo blockInfo : this.contraption.getBlocks().values()) {
            BlockPos pos = transform.apply(blockInfo.pos);
            BlockState state = transform.apply(blockInfo.state);

            if (blockInfo.nbt != null)
                CreateCompat.contraptionRemoveBlock(this.contraption, this.level, pos, state, blockInfo.nbt);
        }
    }
}

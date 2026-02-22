package com.codinglitch.simpleradio.mixin;

import com.codinglitch.simpleradio.core.central.WorldTicking;
import net.minecraft.commands.CommandSource;
import net.minecraft.world.Nameable;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.entity.EntityAccess;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Entity.class)
public abstract class MixinEntity implements CommandSource, Nameable, EntityAccess {

    @Shadow private Level level;

    @Inject(method = "baseTick()V", at = @At(value = "TAIL"))
    private void simpleradio$baseTick_itemInWorldTicking(CallbackInfo ci) {
        if ((Entity)(Object)this instanceof ItemEntity item) {
            if (item.getItem().getItem() instanceof WorldTicking worldTicking) {
                worldTicking.worldTick(item, this.level);
            }
        } else if ((Entity)(Object)this instanceof LivingEntity livingEntity) {
            for (EquipmentSlot slot : EquipmentSlot.values()) {
                ItemStack stack = livingEntity.getItemBySlot(slot);
                if (stack.getItem() instanceof WorldTicking worldTicking) {
                    stack.getItem().inventoryTick(stack, this.level, livingEntity, slot.getIndex(), false);
                }
            }
        }
    }
}
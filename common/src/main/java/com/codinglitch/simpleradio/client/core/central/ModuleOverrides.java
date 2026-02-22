package com.codinglitch.simpleradio.client.core.central;

import com.codinglitch.simpleradio.central.Module;
import com.codinglitch.simpleradio.client.core.registry.models.LayeredModuleModel;
import com.codinglitch.simpleradio.client.core.registry.models.ModuleModel;
import com.codinglitch.simpleradio.core.registry.items.ModuleItem;
import com.google.common.collect.Maps;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

public class ModuleOverrides extends ItemOverrides {
    private final Map<Module, BakedModel> cache = Maps.newHashMap();

    public ModuleOverrides() {
        super();
    }

    @Nullable
    @Override
    public BakedModel resolve(BakedModel model, ItemStack stack, @Nullable ClientLevel level, @Nullable LivingEntity entity, int i) {
        Module upgrade = ModuleItem.getModule(stack);

        if (!cache.containsKey(upgrade))
            cache.put(upgrade, new LayeredModuleModel((ModuleModel) model, upgrade));

        return cache.get(upgrade);
    }
}

package com.codinglitch.simpleradio.datagen;

import com.codinglitch.simpleradio.CommonSimpleRadio;
import com.codinglitch.simpleradio.core.registry.SimpleRadioBlocks;
import com.codinglitch.simpleradio.core.registry.SimpleRadioItems;
import com.codinglitch.simpleradio.core.registry.items.UpgradeModuleItem;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricBlockLootTableProvider;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricModelProvider;
import net.minecraft.client.renderer.block.model.ItemModelGenerator;
import net.minecraft.data.models.BlockModelGenerators;
import net.minecraft.data.models.ItemModelGenerators;
import net.minecraft.resources.ResourceLocation;

public class SimpleRadioModelProvider extends FabricModelProvider {
    public SimpleRadioModelProvider(FabricDataOutput generator) {
        super(generator);
    }

    @Override
    public void generateBlockStateModels(BlockModelGenerators blockStateModelGenerator) {

    }

    @Override
    public void generateItemModels(ItemModelGenerators itemModelGenerator) {
        SimpleRadioItems.ITEMS.forEach((location, item) -> {
            if (item instanceof UpgradeModuleItem upgradeModuleItem) {
                itemModelGenerator.generateLayeredItem(location.withPrefix("item/"),
                        CommonSimpleRadio.id("item/", upgradeModuleItem.getTier().toString().toLowerCase(), "_upgrade_module"),
                        CommonSimpleRadio.id("item/", upgradeModuleItem.getType().toString().toLowerCase(), "_upgrade")
                );
            }
        });
    }
}

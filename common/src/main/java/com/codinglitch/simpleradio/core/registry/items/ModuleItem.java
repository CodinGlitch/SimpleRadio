package com.codinglitch.simpleradio.core.registry.items;

import com.codinglitch.simpleradio.CommonSimpleRadio;
import com.codinglitch.simpleradio.core.registry.SimpleRadioModules;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.TieredItem;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class ModuleItem extends TieredItem {

    public ModuleItem(Tier tier, Properties properties) {
        super(tier, properties);
    }

    public static Module getModule(ItemStack stack) {
        CompoundTag tag = stack.getOrCreateTag();

        ResourceLocation type = tag.contains("type") ? ResourceLocation.tryParse(tag.getString("type")) : CommonSimpleRadio.id("range");
        return SimpleRadioModules.get(type);
    }

    @Override
    public Component getName(ItemStack stack) {
        if (stack.getOrCreateTag().contains("type")) {
            Module module = getModule(stack);
            String modulePath = "module."+module.identifier.getNamespace()+"."+module.identifier.getPath();

            return Component.translatable(this.getDescriptionId(stack), Component.translatable(modulePath));
        }

        return Component.translatable(this.getDescriptionId(stack), Component.translatable("module.simpleradio.empty"));
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> components, TooltipFlag tooltip) {
        CompoundTag tag = stack.getOrCreateTag();

        if (tag.contains("type")) {
            Module module = getModule(stack);
            String namespace = module.identifier.getNamespace();
            String moduleName = module.identifier.getPath();
            String modulePath = "module."+namespace+"."+moduleName;

            components.add(CommonComponents.EMPTY);
            for (Module.Type type : module.types) {
                components.add(Component.translatable("item.modifiers." + type.getName()).withStyle(ChatFormatting.GRAY));

                if (I18n.exists(modulePath + "." + type.getName() + ".effects")) {
                    components.add(CommonComponents.space().append(Component.translatable(modulePath + "." + type.getName() + ".effects").withStyle(ChatFormatting.DARK_GREEN)));
                } else {
                    components.add(CommonComponents.space().append(Component.translatable(modulePath + ".effects").withStyle(ChatFormatting.DARK_GREEN)));
                }
            }
        }

        if (Screen.hasShiftDown() && tag.contains("user")) {
            /*components.add(Component.translatable(
                    "tooltip.simpleradio.receiver_user",
                    tag.getUUID("user")
            ).withStyle(ChatFormatting.DARK_GRAY));*/
        }

        super.appendHoverText(stack, level, components, tooltip);
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int $$3, boolean $$4) {
        CompoundTag tag = stack.getOrCreateTag();

        if (!tag.contains("type"))
            tag.putString("type", CommonSimpleRadio.id("range").toString());

        super.inventoryTick(stack, level, entity, $$3, $$4);
    }
}

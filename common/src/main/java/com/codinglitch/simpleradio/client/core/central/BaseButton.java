package com.codinglitch.simpleradio.client.core.central;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;

public class BaseButton extends AbstractButton {
    public final int iconX;
    public final int iconY;
    public int hoverIconX = -1;
    public int hoverIconY = -1;
    public int selectedIconX = -1;
    public int selectedIconY = -1;

    protected Component tooltip;

    public boolean selected;

    private final ResourceLocation texture;
    private final Runnable onPress;

    public BaseButton(int x, int y, int width, int height) {
        this(x, y, width, height, 0, 0, null, CommonComponents.EMPTY, null);
    }

    public BaseButton(int x, int y, int width, int height, int iconX, int iconY) {
        this(x, y, width, height, iconX, iconY, null, CommonComponents.EMPTY, null);
    }

    public BaseButton(int x, int y, int width, int height, int iconX, int iconY, ResourceLocation texture) {
        this(x, y, width, height, iconX, iconY, texture, CommonComponents.EMPTY, null);
    }

    public BaseButton(int x, int y, int width, int height, int iconX, int iconY, ResourceLocation texture, Component component) {
        this(x, y, width, height, iconX, iconY, texture, component, null);
    }

    public BaseButton(int x, int y, int width, int height, int iconX, int iconY, ResourceLocation texture, Component component, Runnable onPress) {
        super(x, y, width, height, component);

        this.iconX = iconX;
        this.iconY = iconY;
        this.texture = texture;
        this.onPress = onPress;
    }

    @Override
    public void onPress() {
        if (onPress != null) {
            onPress.run();
        }
    }

    public ResourceLocation getTexture() {
        return texture;
    }

    public void blit(PoseStack graphics, int iconX, int iconY) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShaderTexture(0, this.getTexture());
        super.blit(graphics, this.x, this.y, iconX, iconY, this.width, this.height);
    }

    @Override
    public void render(PoseStack stack, int x, int y, float $$3) {
        super.render(stack, x, y, $$3);
    }

    @Override
    public void renderButton(PoseStack stack, int mouseX, int mouseY, float delta) {
        int x = this.iconX;
        int y = this.iconY;
        if (selected && (selectedIconX != -1 && selectedIconY != -1)) {
            x = selectedIconX;
            y = selectedIconY;
        } else if (this.isHoveredOrFocused() && (hoverIconX != -1 && hoverIconY != -1)) {
            x = hoverIconX;
            y = hoverIconY;
        }

        this.blit(stack, x, y);
    }

    @Override
    protected MutableComponent createNarrationMessage() {
        return (MutableComponent) this.getMessage();
    }

    @Override
    public void updateNarration(NarrationElementOutput output) {
        this.defaultButtonNarrationText(output);
    }

    public void setTooltip(Component tooltip) {
        this.tooltip = tooltip;
    }
}

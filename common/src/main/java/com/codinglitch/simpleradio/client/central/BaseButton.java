package com.codinglitch.simpleradio.client.central;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

public class BaseButton extends AbstractButton {
    public ResourceLocation sprite;
    public ResourceLocation hoverSprite;
    public ResourceLocation selectedSprite;

    public boolean selected;
    private final Runnable onPress;

    public BaseButton(int x, int y, int width, int height) {
        this(x, y, width, height, null, CommonComponents.EMPTY, null);
    }

    public BaseButton(int x, int y, int width, int height, ResourceLocation sprite) {
        this(x, y, width, height, sprite, CommonComponents.EMPTY, null);
    }

    public BaseButton(int x, int y, int width, int height, ResourceLocation sprite, Component component) {
        this(x, y, width, height, sprite, component, null);
    }

    public BaseButton(int x, int y, int width, int height, ResourceLocation sprite, Component component, Runnable onPress) {
        super(x, y, width, height, component);

        this.sprite = sprite;
        this.onPress = onPress;
    }

    @Override
    public void onPress() {
        if (onPress != null) {
            onPress.run();
        }
    }

    public ResourceLocation getTexture() {
        ResourceLocation sprite = this.sprite;
        if (selected && this.selectedSprite != null) {
            sprite = this.selectedSprite;
        } else if (this.isHoveredOrFocused() && this.hoverSprite != null) {
            sprite = this.hoverSprite;
        }
        return sprite;
    }

    public void blit(GuiGraphics graphics, int iconX, int iconY) {
        graphics.blit(this.getTexture(), this.getX(), this.getY(), iconX, iconY, this.width, this.height);
    }

    @Override
    public void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
        this.blit(graphics, 0, 0);
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput output) {
        this.defaultButtonNarrationText(output);
    }
}

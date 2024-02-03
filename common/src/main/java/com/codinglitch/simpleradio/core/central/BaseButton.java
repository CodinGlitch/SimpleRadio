package com.codinglitch.simpleradio.core.central;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

public class BaseButton extends AbstractButton {
    private final int iconX;
    private final int iconY;
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

    @Override
    public void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
        graphics.blit(this.getTexture(), this.getX(), this.getY(), this.iconX, this.iconY, this.width, this.height);
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput output) {
        this.defaultButtonNarrationText(output);
    }
}

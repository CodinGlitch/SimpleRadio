package com.codinglitch.simpleradio.client.central;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

public class BaseButton extends AbstractButton {
    public final int iconX;
    public final int iconY;
    public int hoverIconX = -1;
    public int hoverIconY = -1;
    public int selectedIconX = -1;
    public int selectedIconY = -1;

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

    public void blit(GuiGraphics graphics, int iconX, int iconY) {
        graphics.blit(this.getTexture(), this.getX(), this.getY(), iconX, iconY, this.width, this.height);
    }

    @Override
    public void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
        int x = this.iconX;
        int y = this.iconY;
        if (selected && (selectedIconX != -1 && selectedIconY != -1)) {
            x = selectedIconX;
            y = selectedIconY;
        } else if (this.isHoveredOrFocused() && (hoverIconX != -1 && hoverIconY != -1)) {
            x = hoverIconX;
            y = hoverIconY;
        }

        this.blit(graphics, x, y);
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput output) {
        this.defaultButtonNarrationText(output);
    }
}

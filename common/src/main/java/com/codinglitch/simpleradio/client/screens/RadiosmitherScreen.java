package com.codinglitch.simpleradio.client.screens;

import com.codinglitch.simpleradio.CommonSimpleRadio;
import com.codinglitch.simpleradio.client.central.BaseButton;
import com.codinglitch.simpleradio.core.central.Receiving;
import com.codinglitch.simpleradio.core.networking.packets.ServerboundRadioUpdatePacket;
import com.codinglitch.simpleradio.core.registry.menus.RadiosmitherMenu;
import com.codinglitch.simpleradio.core.central.Frequency;
import com.codinglitch.simpleradio.platform.ClientServices;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

import java.awt.*;

public class RadiosmitherScreen extends AbstractContainerScreen<RadiosmitherMenu> {

    // -- Resources -- \\
    private static final ResourceLocation TEXTURE = CommonSimpleRadio.id("textures/gui/container/radiosmither.png");

    private static final Component AM = Component.translatable("screen.simpleradio.radiosmither.am");
    private static final Component FM = Component.translatable("screen.simpleradio.radiosmither.fm");
    private static final Component AM_DESCRIPTION = Component.translatable("screen.simpleradio.radiosmither.am_description");
    private static final Component FM_DESCRIPTION = Component.translatable("screen.simpleradio.radiosmither.fm_description");

    // -- Constants -- \\
    private static final int INCREMENT_THRESHOLD = 10;

    // -- Fields -- \\

    public ModulationButton FM_BUTTON;
    public ModulationButton AM_BUTTON;

    public FrequencyButton INCREASE_BUTTON;
    public FrequencyButton DECREASE_BUTTON;

    public BaseButton APPLY_BUTTON;

    public String frequency = "";
    public Frequency.Modulation modulation;

    protected int holdingFor = 0;
    protected int increment = 0;

    public RadiosmitherScreen(RadiosmitherMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
    }

    private void reloadFrequency() {
        if (!frequency.isEmpty()) return;

        ItemStack tinkering = menu.getTinkering();
        if (tinkering != null && tinkering.getItem() instanceof Receiving) {
            CompoundTag tag = tinkering.getOrCreateTag();
            frequency = tag.getString("frequency");
            modulation = Frequency.modulationOf(tag.getString("modulation"));

            if (modulation != null) {
                if (modulation == Frequency.Modulation.FREQUENCY) {
                    FM_BUTTON.selected(true);
                } else if (modulation == Frequency.Modulation.AMPLITUDE) {
                    AM_BUTTON.selected(true);
                }
            }
        }
    }

    protected void incrementFrequency() { incrementFrequency(1); }
    protected void incrementFrequency(int increment) {
        if (!frequency.isEmpty()) {
            frequency = Frequency.incrementFrequency(frequency, increment);
        }
    }

    @Override
    protected void containerTick() {
        super.containerTick();

        reloadFrequency();

        if (increment != 0) holdingFor++;

        if (holdingFor > INCREMENT_THRESHOLD)
            incrementFrequency(increment * (1 + Math.round(holdingFor / 5f)));
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float delta, int mouseX, int mouseY) {
        int x = (this.width - this.imageWidth) / 2;
        int y = (this.height - this.imageHeight) / 2;
        graphics.blit(TEXTURE, x, y, 0, 0, this.imageWidth, this.imageHeight);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
        super.render(graphics, mouseX, mouseY, delta);
        renderTooltip(graphics, mouseX, mouseY);
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        super.renderLabels(graphics, mouseX, mouseY);

        ItemStack tinkering = this.menu.getTinkering();
        if (tinkering != null && tinkering.getItem() instanceof Receiving) {
            if (!frequency.isEmpty() && modulation != null) {
                graphics.drawString(this.font,
                        Component.literal(frequency)
                                .append(modulation == Frequency.Modulation.FREQUENCY ? FM : AM), 94, 50, Color.DARK_GRAY.getRGB(), false
                );
            }
        }
    }

    @Override
    public boolean mouseReleased(double $$0, double $$1, int $$2) {
        if (INCREASE_BUTTON.isHoveredOrFocused())
            INCREASE_BUTTON.onReleased();
        else if (DECREASE_BUTTON.isHoveredOrFocused())
            DECREASE_BUTTON.onReleased();

        return super.mouseReleased($$0, $$1, $$2);
    }

    @Override
    protected void init() {
        super.init();

        this.AM_BUTTON = new ModulationButton(this.leftPos + 89, this.topPos + 23, false, () -> {
            AM_BUTTON.selected(true);
            FM_BUTTON.selected(false);

            modulation = Frequency.Modulation.AMPLITUDE;
        });

        this.FM_BUTTON = new ModulationButton(this.leftPos + 126, this.topPos + 23, true, () -> {
            FM_BUTTON.selected(true);
            AM_BUTTON.selected(false);

            modulation = Frequency.Modulation.FREQUENCY;
        });

        this.INCREASE_BUTTON = new FrequencyButton(this.leftPos + 143, this.topPos + 45, true, this);
        this.DECREASE_BUTTON = new FrequencyButton(this.leftPos + 143, this.topPos + 54, false, this);

        this.APPLY_BUTTON = new BaseButton(
                this.leftPos + 15, this.topPos + 25,
                34, 34,
                88, 166,
                TEXTURE, CommonComponents.EMPTY, () -> {
                    if (this.frequency == null || this.modulation == null) return;
                    ClientServices.NETWORKING.sendToServer(new ServerboundRadioUpdatePacket(this.frequency, this.modulation));
        });

        this.addRenderableWidget(AM_BUTTON);
        this.addRenderableWidget(FM_BUTTON);

        this.addRenderableWidget(INCREASE_BUTTON);
        this.addRenderableWidget(DECREASE_BUTTON);

        this.addRenderableWidget(APPLY_BUTTON);
    }

    public static class FrequencyButton extends BaseButton {
        private final boolean isIncrease;
        private final RadiosmitherScreen screen;

        public FrequencyButton(int x, int y, boolean isIncrease, RadiosmitherScreen screen) {
            super(x, y, 18, 9, 70, isIncrease ? 166 : 175, TEXTURE);
            this.selectedIconX = this.iconX;
            this.selectedIconY = isIncrease ? 184 : 193;
            this.hoverIconX = this.iconX;
            this.hoverIconY = isIncrease ? 202 : 211;

            this.screen = screen;
            this.isIncrease = isIncrease;
        }

        @Override
        public void onPress() {
            this.selected = true;

            screen.increment = isIncrease ? 1 : -1;
            screen.holdingFor = 0;
            screen.incrementFrequency(screen.increment);
        }

        public void onReleased() {
            this.selected = false;
            this.setFocused(false);

            screen.increment = 0;
            screen.holdingFor = 0;
        }
    }

    public static class ModulationButton extends BaseButton {
        private boolean isFM;

        public ModulationButton(int x, int y, boolean isFM, Runnable onPress) {
            super(x, y, 35, 18, isFM ? 0 : 35, 166, TEXTURE, CommonComponents.EMPTY, onPress);
            this.selectedIconX = this.iconX;
            this.selectedIconY = 184;
            this.hoverIconX = this.iconX;
            this.hoverIconY = 202;

            this.isFM = isFM;

            this.setTooltip(Tooltip.create(isFM ? FM_DESCRIPTION : AM_DESCRIPTION, null));
        }

        public void selected(boolean selected) {
            this.selected = selected;
        }
    }
}

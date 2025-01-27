package com.codinglitch.simpleradio.client.screens;

import com.codinglitch.simpleradio.CommonSimpleRadio;
import com.codinglitch.simpleradio.client.central.BaseButton;
import com.codinglitch.simpleradio.core.central.Frequencing;
import com.codinglitch.simpleradio.core.networking.packets.ServerboundRadioUpdatePacket;
import com.codinglitch.simpleradio.core.registry.menus.RadiosmitherMenu;
import com.codinglitch.simpleradio.core.central.Frequency;
import com.codinglitch.simpleradio.platform.ClientServices;
import com.codinglitch.simpleradio.radio.RadioRouter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerListener;
import net.minecraft.world.item.ItemStack;

import java.awt.*;

public class RadiosmitherScreen extends AbstractContainerScreen<RadiosmitherMenu> implements ContainerListener {

    // -- Resources -- \\
    public static final ResourceLocation FM_NORMAL_SPRITE = CommonSimpleRadio.id("container/radiosmither/fm/normal");
    public static final ResourceLocation FM_SELECTED_SPRITE = CommonSimpleRadio.id("container/radiosmither/fm/selected");
    public static final ResourceLocation FM_HIGHLIGHTED_SPRITE = CommonSimpleRadio.id("container/radiosmither/fm/highlighted");
    public static final ResourceLocation AM_NORMAL_SPRITE = CommonSimpleRadio.id("container/radiosmither/am/normal");
    public static final ResourceLocation AM_SELECTED_SPRITE = CommonSimpleRadio.id("container/radiosmither/am/selected");
    public static final ResourceLocation AM_HIGHLIGHTED_SPRITE = CommonSimpleRadio.id("container/radiosmither/am/highlighted");

    public static final ResourceLocation KNOB_NORMAL_SPRITE = CommonSimpleRadio.id("container/radiosmither/knob/normal");
    public static final ResourceLocation KNOB_SCROLLED_SPRITE = CommonSimpleRadio.id("container/radiosmither/knob/scrolled");
    public static final ResourceLocation KNOB_HIGHLIGHTED_SPRITE = CommonSimpleRadio.id("container/radiosmither/knob/highlighted");

    public static final ResourceLocation APPLY_NORMAL_SPRITE = CommonSimpleRadio.id("container/radiosmither/button/normal");
    public static final ResourceLocation APPLY_SELECTED_SPRITE = CommonSimpleRadio.id("container/radiosmither/button/selected");
    public static final ResourceLocation APPLY_HIGHLIGHTED_SPRITE = CommonSimpleRadio.id("container/radiosmither/button/highlighted");

    private static final ResourceLocation AM_SPRITE = CommonSimpleRadio.id("textures/gui/sprites/container/radiosmither/waves/am.png");
    private static final ResourceLocation FM_SPRITE = CommonSimpleRadio.id("textures/gui/sprites/container/radiosmither/waves/fm.png");

    private static final ResourceLocation TEXTURE = CommonSimpleRadio.id("textures/gui/container/radiosmither.png");

    private static final Component AM = Component.translatable("screen.simpleradio.radiosmither.am");
    private static final Component FM = Component.translatable("screen.simpleradio.radiosmither.fm");
    private static final Component AM_DESCRIPTION = Component.translatable("screen.simpleradio.radiosmither.am_description");
    private static final Component FM_DESCRIPTION = Component.translatable("screen.simpleradio.radiosmither.fm_description");

    // -- Constants -- \\
    private static final int INCREMENT_THRESHOLD = 5;

    // -- Fields -- \\
    public ModulationButton FM_BUTTON;
    public ModulationButton AM_BUTTON;

    public KnobButton KNOB;

    public BaseButton APPLY_BUTTON;

    public EditBox FREQUENCY;

    protected String lastValidFrequency = Frequency.DEFAULT_FREQUENCY;
    public Frequency.Modulation modulation;

    protected int holdingFor = 0;
    protected int increment = 0;

    protected float time = 0;

    public RadiosmitherScreen(RadiosmitherMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        this.imageHeight = 183;
        this.titleLabelY = 6;
        this.inventoryLabelY = this.imageHeight - 94;
    }

    private void updateFrequency(String input) {
        if (Frequency.check(input)) {
            this.lastValidFrequency = input;
        }
    }

    private void setModulation(Frequency.Modulation modulation) {
        this.modulation = modulation;

        if (modulation != null) {
            if (modulation == Frequency.Modulation.FREQUENCY) {
                FM_BUTTON.selected(true);
                AM_BUTTON.selected(false);
            } else if (modulation == Frequency.Modulation.AMPLITUDE) {
                AM_BUTTON.selected(true);
                FM_BUTTON.selected(false);
            }
        } else {
            FM_BUTTON.selected(false);
            AM_BUTTON.selected(false);
        }
    }

    protected void incrementFrequency() { incrementFrequency(1); }
    protected void incrementFrequency(int increment) {
        String freq = this.FREQUENCY.getValue();
        if (!freq.isEmpty()) {
            this.FREQUENCY.setValue(Frequency.incrementFrequency(freq, increment));
        }
    }

    @Override
    protected void containerTick() {
        super.containerTick();

        ItemStack tinkering = this.menu.getTinkering();
        if (tinkering != null && tinkering.getItem() instanceof Frequencing) {
            if (!FREQUENCY.isFocused() && !Frequency.check(FREQUENCY.getValue())) {
                if (!Frequency.check(lastValidFrequency)) lastValidFrequency = Frequency.DEFAULT_FREQUENCY;
                FREQUENCY.setValue(lastValidFrequency);
            }
        } else {
            FREQUENCY.setValue("");
        }


        if (this.minecraft != null) {
            int y = (int)(this.minecraft.mouseHandler.ypos() * (double)this.minecraft.getWindow().getGuiScaledHeight() / (double)this.minecraft.getWindow().getScreenHeight());

            // Incrementing
            if (KNOB.selected) {
                int centerY = KNOB.getY() + (KNOB.getHeight()/2);
                increment = (centerY - y) / 5;
            } else {
                increment = 0;
            }

            if (increment != 0) {
                holdingFor++;
            }

            if (holdingFor > INCREMENT_THRESHOLD)
                incrementFrequency(increment * (1 + Math.round(holdingFor / 5f)));
        }
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float delta, int mouseX, int mousey) {
        int x = (this.width - imageWidth) / 2;
        int y = (this.height - imageHeight) / 2;
        graphics.blit(TEXTURE, x, y, 0, 0, imageWidth, imageHeight);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
        super.render(graphics, mouseX, mouseY, delta);
        renderTooltip(graphics, mouseX, mouseY);

        if (modulation != null) {
            this.time = (this.time + delta * 0.1f) % 5;

            int x = (int) ((time/5) * 142);

            switch (modulation) {
                case AMPLITUDE -> {
                    graphics.blit(AM_SPRITE, this.leftPos + 9, this.topPos + 46, x, 0, 142 - x, 31, 142, 31);
                    graphics.blit(AM_SPRITE, (this.leftPos + 151) - x, this.topPos + 46, 0, 0, x, 31, 142 , 31);
                }
                case FREQUENCY -> {
                    graphics.blit(FM_SPRITE, this.leftPos + 9, this.topPos + 45, x, 0, 142 - x, 34, 142, 34);
                    graphics.blit(FM_SPRITE, (this.leftPos + 151) - x, this.topPos + 45, 0, 0, x, 34, 142, 34);
                }
            }
        }

        this.FREQUENCY.render(graphics, mouseX, mouseY, delta);
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        super.renderLabels(graphics, mouseX, mouseY);
    }

    @Override
    public boolean mouseReleased(double $$0, double $$1, int $$2) {
        if (KNOB.isHoveredOrFocused()) {
            KNOB.onReleased();
        }

        if (APPLY_BUTTON.isHoveredOrFocused()) {
            APPLY_BUTTON.selected = false;
            APPLY_BUTTON.setFocused(false);
        }

        return super.mouseReleased($$0, $$1, $$2);
    }

    @Override
    protected void init() {
        super.init();

        this.FREQUENCY = new EditBox(this.font, this.leftPos + 90, this.topPos + 28, 50, 12, Component.translatable("screen.simpleradio.radiosmither.frequency"));
        this.FREQUENCY.setTextColor(-1);
        this.FREQUENCY.setTextColorUneditable(-1);
        this.FREQUENCY.setCanLoseFocus(true);
        this.FREQUENCY.setBordered(false);
        this.FREQUENCY.setMaxLength(Frequency.FREQUENCY_DIGITS + 1);
        this.FREQUENCY.setResponder(this::updateFrequency);
        this.FREQUENCY.setValue("");
        this.FREQUENCY.setFilter(s -> s.isEmpty() || s.matches("^\\d[.\\d]*$"));
        this.lastValidFrequency = Frequency.DEFAULT_FREQUENCY;
        this.addWidget(FREQUENCY);
        this.FREQUENCY.setEditable(this.menu.getSlot(0).hasItem());

        this.AM_BUTTON = new ModulationButton(this.leftPos + 8, this.topPos + 23, false, () -> {
            AM_BUTTON.selected(true);
            FM_BUTTON.selected(false);

            modulation = Frequency.Modulation.AMPLITUDE;
        });

        this.FM_BUTTON = new ModulationButton(this.leftPos + 46, this.topPos + 23, true, () -> {
            FM_BUTTON.selected(true);
            AM_BUTTON.selected(false);

            modulation = Frequency.Modulation.FREQUENCY;
        });

        this.KNOB = new KnobButton(this.leftPos + 154, this.topPos + 44);

        this.APPLY_BUTTON = new BaseButton(
                this.leftPos + 153, this.topPos + 24,
                15, 16,
                APPLY_NORMAL_SPRITE, CommonComponents.EMPTY, () -> {
                    APPLY_BUTTON.selected = true;

                    if (this.FREQUENCY.getValue().isEmpty() || this.modulation == null) return;
                    ClientServices.NETWORKING.sendToServer(new ServerboundRadioUpdatePacket(this.FREQUENCY.getValue(), this.modulation));
        });
        this.APPLY_BUTTON.hoverSprite = APPLY_HIGHLIGHTED_SPRITE;
        this.APPLY_BUTTON.selectedSprite = APPLY_SELECTED_SPRITE;

        this.addRenderableWidget(AM_BUTTON);
        this.addRenderableWidget(FM_BUTTON);

        this.addRenderableWidget(KNOB);

        this.addRenderableWidget(APPLY_BUTTON);

        this.menu.addSlotListener(this);
    }

    @Override
    public void removed() {
        super.removed();
        this.menu.removeSlotListener(this);
    }

    @Override
    public void slotChanged(AbstractContainerMenu abstractContainerMenu, int slot, ItemStack stack) {
        if (slot == 0) {
            if (!stack.isEmpty() && stack.getItem() instanceof Frequencing frequencing) {
                Frequency frequency = frequencing.getFrequency(stack);
                String numbers = frequency.frequency;
                this.lastValidFrequency = numbers;

                this.FREQUENCY.setValue(numbers);
                this.FREQUENCY.setEditable(true);
                this.setFocused(this.FREQUENCY);

                setModulation(frequency.modulation);
            } else {
                this.FREQUENCY.setValue("");
                this.FREQUENCY.setEditable(false);

                setModulation(null);
            }
        }
    }

    @Override
    public void dataChanged(AbstractContainerMenu abstractContainerMenu, int i, int i1) {

    }


    public class KnobButton extends BaseButton {
        public KnobButton(int x, int y) {
            super(x, y, 16, 37, KNOB_NORMAL_SPRITE);
            this.hoverSprite = KNOB_HIGHLIGHTED_SPRITE;
        }

        @Override
        public ResourceLocation getTexture() {
            if (this.selected) {
                if (increment != 0) {
                    return holdingFor % 2 == 0 ? KNOB_NORMAL_SPRITE : KNOB_SCROLLED_SPRITE;
                } else {
                    return KNOB_NORMAL_SPRITE;
                }
            }

            return super.getTexture();
        }

        @Override
        public void onPress() {
            this.selected = true;

            RadiosmitherScreen.this.holdingFor = 0;

            if (RadiosmitherScreen.this.minecraft != null) {
                int y = (int)(RadiosmitherScreen.this.minecraft.mouseHandler.ypos() * (double)RadiosmitherScreen.this.minecraft.getWindow().getGuiScaledHeight() / (double)RadiosmitherScreen.this.minecraft.getWindow().getScreenHeight());

                if (y < (this.getY() + this.height*0.25)) {
                    incrementFrequency(1);
                } else if (y > (this.getY() + this.height*0.75)) {
                    incrementFrequency(-1);
                }
            }
        }

        public void onReleased() {
            this.selected = false;
            this.setFocused(false);

            RadiosmitherScreen.this.increment = 0;
            RadiosmitherScreen.this.holdingFor = 0;
        }
    }

    public static class ModulationButton extends BaseButton {
        public ModulationButton(int x, int y, boolean isFM, Runnable onPress) {
            super(x, y, 35, 18, isFM ? FM_NORMAL_SPRITE : AM_NORMAL_SPRITE, CommonComponents.EMPTY, onPress);
            this.hoverSprite = isFM ? FM_HIGHLIGHTED_SPRITE : AM_HIGHLIGHTED_SPRITE;
            this.selectedSprite = isFM ? FM_SELECTED_SPRITE : AM_SELECTED_SPRITE;

            this.setTooltip(Tooltip.create(isFM ? FM_DESCRIPTION : AM_DESCRIPTION, null));
        }

        public void selected(boolean selected) {
            this.selected = selected;
        }
    }
}

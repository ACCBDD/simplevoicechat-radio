package com.accbdd.simplevoiceradio.screen;

import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

import com.accbdd.simplevoiceradio.networking.NetworkingManager;
import com.accbdd.simplevoiceradio.networking.packet.RadioConfigurePacket;
import com.accbdd.simplevoiceradio.radio.Frequency;
import com.mojang.blaze3d.vertex.PoseStack;

public class RadioConfigureScreen extends Screen {
    private static final Component TITLE = Component.translatable("gui.simplevoiceradio.screen.radio_configure_screen_title");

    private final int screenWidth, screenHeight;
    private int leftPos, topPos;
    private ItemStack radio;
    private String frequency;

    protected int holdingFor = 0;
    protected int increment = 0;

    public RadioConfigureScreen() {
        super(TITLE);
        
        this.screenWidth = 20;
        this.screenHeight = 40;
    }

    @Override
    protected void init() {
        super.init();

        this.leftPos = (this.width - this.screenWidth) / 2;
        this.topPos = (this.height - this.screenHeight) / 2;

        if(this.minecraft == null) return;
        this.radio = this.minecraft.player.getMainHandItem();
        CompoundTag tag = radio.getOrCreateTag();
        if (!tag.contains("frequency") || tag.getString("frequency").isEmpty())
            tag.putString("frequency", "001.00");
        this.frequency = tag.getString("frequency");

        addRenderableWidget(
            new ChangeButton(this.leftPos, this.topPos, true, this)
        );
        addRenderableWidget(
            new ChangeButton(this.leftPos, this.topPos + 20, false, this)
        );
    }

    @Override
    public void tick() {
        super.tick();

        if (increment != 0) holdingFor++;

        if (holdingFor > 10)
            incrementFrequency(increment * (1 + Math.round(holdingFor / 5f)));
    }

    @Override
    public void render(PoseStack pose, int mouseX, int mouseY, float partialTicks) {
        renderBackground(pose);
        super.render(pose, mouseX, mouseY, partialTicks);
        this.font.draw(pose, this.frequency + " FM", this.leftPos+23, this.topPos+17, 0xFFFFFF);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public void onClose() {
        NetworkingManager.sendToServer(new RadioConfigurePacket(Integer.parseInt(frequency.replaceAll("[.]", ""))));
        super.onClose();
    }

    protected void incrementFrequency(int increment) {
        if(!frequency.isEmpty()) {
            frequency = Frequency.incrementFrequency(frequency, increment);
        }
    }

    public static class ChangeButton extends AbstractButton {
        private boolean isIncrement;
        private RadioConfigureScreen screen;

        private ChangeButton(int x, int y, boolean isIncrement, RadioConfigureScreen screen) {
            super(x, y, 20, 20, Component.literal(isIncrement ? "UP" : "DN"));
            this.isIncrement = isIncrement;
            this.screen = screen;
        }

        @Override
        public void updateNarration(NarrationElementOutput output) {
            this.defaultButtonNarrationText(output);
        }

        @Override
        public void onPress() {
            screen.increment = this.isIncrement ? 1 : -1;
            screen.holdingFor = 0;
            screen.incrementFrequency(screen.increment);
        }

        @Override
        public void onRelease(double p_93669_, double p_93670_) {
            super.onRelease(p_93669_, p_93670_);

            screen.increment = 0;
            screen.holdingFor = 0;
        }
    }
}

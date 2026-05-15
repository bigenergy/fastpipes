package com.piglinmine.fastpipes.screen.widget;

import com.piglinmine.fastpipes.FastPipes;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

public class IconButton extends Button {
    private static final ResourceLocation RESOURCE = ResourceLocation.fromNamespaceAndPath(FastPipes.MOD_ID, "textures/gui/extractor_attachment.png");

    private final IconButtonPreset preset;
    private int overlayTexX;
    private int overlayTexY;

    public IconButton(int x, int y, IconButtonPreset preset, int overlayTexX, int overlayTexY, Component text, OnPress onPress) {
        super(x, y, preset.getWidth(), preset.getHeight(), text, onPress, DEFAULT_NARRATION);

        this.preset = preset;
        this.overlayTexX = overlayTexX;
        this.overlayTexY = overlayTexY;
    }

    public void setOverlayTexX(int overlayTexX) {
        this.overlayTexX = overlayTexX;
    }

    @Override
    public void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        int y = preset.getYTexNormal();
        if (!active) {
            y = preset.getYTexDisabled();
        } else if (isHovered) {
            y = preset.getYTexHover();
        }

        graphics.blit(RESOURCE, this.getX(), this.getY(), preset.getXTex(), y, this.width, this.height, 256, 256);

        // Fiddling with -1 to remove the blue border
        graphics.blit(RESOURCE, this.getX() + 1, this.getY() + 1, overlayTexX + 1, overlayTexY + 1, this.width - 2, this.height - 2, 256, 256);
    }
} 
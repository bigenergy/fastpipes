package com.piglinmine.fastpipes.screen.widget;

import com.piglinmine.fastpipes.FastPipes;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

// TODO 1.21.11: AbstractButton now requires renderContents(GuiGraphics,int,int,float) as abstract method;
// renderWidget cannot be overridden anymore (was made final). Visual fidelity is broken — stubbed for compile.
public class IconButton extends Button {
    private static final Identifier RESOURCE = Identifier.fromNamespaceAndPath(FastPipes.MOD_ID, "textures/gui/extractor_attachment.png");

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
    protected void renderContents(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        // TODO 1.21.11: original renderWidget logic stubbed; no overlay rendering. blit signature also changed.
    }
}

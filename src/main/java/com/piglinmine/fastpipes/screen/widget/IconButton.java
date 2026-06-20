package com.piglinmine.fastpipes.screen.widget;

import com.piglinmine.fastpipes.FastPipes;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

/**
 * 1.21.11 port notes:
 *  - Button.renderWidget was made final; we override the new abstract renderContents(...) instead.
 *  - GuiGraphics.blit no longer takes a bare (Identifier, ints...) overload — every textured blit
 *    now requires a RenderPipeline as the first arg and uses the
 *    (pipeline, atlas, x, y, u, v, w, h, texW, texH) signature.
 */
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
        int yTex = preset.getYTexNormal();
        if (!active) {
            yTex = preset.getYTexDisabled();
        } else if (isHoveredOrFocused()) {
            yTex = preset.getYTexHover();
        }

        // Button background frame
        graphics.blit(
            RenderPipelines.GUI_TEXTURED,
            RESOURCE,
            this.getX(), this.getY(),
            (float) preset.getXTex(), (float) yTex,
            this.width, this.height,
            256, 256
        );

        // Inner icon overlay (the -1/+1 trims the blue focus border, matching master)
        graphics.blit(
            RenderPipelines.GUI_TEXTURED,
            RESOURCE,
            this.getX() + 1, this.getY() + 1,
            (float) (overlayTexX + 1), (float) (overlayTexY + 1),
            this.width - 2, this.height - 2,
            256, 256
        );
    }
}

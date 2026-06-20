package com.piglinmine.fastpipes.render;

import net.neoforged.neoforge.fluids.FluidStack;

import javax.annotation.Nonnull;

// TODO 1.21.11: BufferUploader, Tesselator.begin(...), DefaultVertexFormat and the
// RenderSystem.setShaderColor/setShaderTexture immediate-mode pipeline used by this
// renderer were removed/relocated. The old JEI-derived FluidStackRenderer logic
// (drawTextureWithMasking + tiled sprite drawing) is preserved only as commented
// intent; render(...) is now a no-op. Needs porting to the new GuiGraphics/Tesselator API.
public class FluidRenderer {
    public static final FluidRenderer INSTANCE = new FluidRenderer(1000, 16, 16, 16);

    private final int capacityMb;
    private final int width;
    private final int height;
    private final int minHeight;

    public FluidRenderer(int capacityMb, int width, int height, int minHeight) {
        this.capacityMb = capacityMb;
        this.width = width;
        this.height = height;
        this.minHeight = minHeight;
    }

    public void render(final int xPosition, final int yPosition, @Nonnull FluidStack fluidStack) {
        // TODO 1.21.11: no-op stub. Re-implement against the new client rendering API.
    }
}

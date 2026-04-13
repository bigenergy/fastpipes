package com.piglinmine.fastpipes.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import com.piglinmine.fastpipes.menu.BaseContainerMenu;
import com.piglinmine.fastpipes.menu.slot.FluidFilterSlot;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraftforge.client.extensions.common.IClientFluidTypeExtensions;
import net.minecraftforge.fluids.FluidStack;

public abstract class BaseScreen<T extends BaseContainerMenu> extends AbstractContainerScreen<T> {
    public BaseScreen(T screenContainer, Inventory inv, Component title) {
        super(screenContainer, inv, title);
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTicks, int mouseX, int mouseY) {
        for (FluidFilterSlot slot : menu.getFluidSlots()) {
            FluidStack stack = slot.getFluidInventory().getFluid(slot.getSlotIndex());
            if (stack.isEmpty()) continue;
            renderFluidInSlot(graphics, leftPos + slot.x, topPos + slot.y, stack);
        }
    }

    private void renderFluidInSlot(GuiGraphics graphics, int x, int y, FluidStack fluid) {
        IClientFluidTypeExtensions ext = IClientFluidTypeExtensions.of(fluid.getFluid());
        ResourceLocation texture = ext.getStillTexture(fluid);
        if (texture == null) return;

        TextureAtlasSprite sprite = Minecraft.getInstance()
            .getTextureAtlas(InventoryMenu.BLOCK_ATLAS).apply(texture);

        // Apply fluid tint color (e.g. water is gray base texture + blue tint)
        int color = ext.getTintColor(fluid);
        float a = ((color >> 24) & 0xFF) / 255.0F;
        float r = ((color >> 16) & 0xFF) / 255.0F;
        float g = ((color >> 8) & 0xFF) / 255.0F;
        float b = (color & 0xFF) / 255.0F;
        if (a == 0) a = 1.0F; // default to fully opaque

        RenderSystem.setShaderColor(r, g, b, a);
        graphics.blit(x, y, 0, 16, 16, sprite);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F); // reset color
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        super.renderLabels(graphics, mouseX, mouseY);
        for (FluidFilterSlot slot : menu.getFluidSlots()) {
            FluidStack stack = slot.getFluidInventory().getFluid(slot.getSlotIndex());
            if (stack.isEmpty()) continue;
            if (!isHovering(slot.x, slot.y, 17, 17, mouseX, mouseY)) continue;
            graphics.renderTooltip(font, stack.getDisplayName(), mouseX - leftPos, mouseY - topPos);
        }
    }
} 
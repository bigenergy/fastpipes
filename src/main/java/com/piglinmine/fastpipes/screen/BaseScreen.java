package com.piglinmine.fastpipes.screen;

import com.piglinmine.fastpipes.menu.BaseContainerMenu;
import com.piglinmine.fastpipes.menu.slot.FluidFilterSlot;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.InventoryMenu;
import net.neoforged.neoforge.client.extensions.common.IClientFluidTypeExtensions;
import net.neoforged.neoforge.fluids.FluidStack;

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
        graphics.blitSprite(RenderType::guiTextured, sprite, x + 1, y + 1, 16, 16);
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        super.renderLabels(graphics, mouseX, mouseY);
        for (FluidFilterSlot slot : menu.getFluidSlots()) {
            FluidStack stack = slot.getFluidInventory().getFluid(slot.getSlotIndex());
            if (stack.isEmpty()) continue;
            if (!isHovering(slot.x, slot.y, 17, 17, mouseX, mouseY)) continue;
            graphics.renderTooltip(font, stack.getHoverName(), mouseX - leftPos, mouseY - topPos);
        }
    }
} 
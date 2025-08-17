package com.piglinmine.fastpipes.screen;

import com.piglinmine.fastpipes.menu.BaseContainerMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public abstract class BaseScreen<T extends BaseContainerMenu> extends AbstractContainerScreen<T> {
    public BaseScreen(T screenContainer, Inventory inv, Component title) {
        super(screenContainer, inv, title);
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTicks, int mouseX, int mouseY) {
        // TODO: Implement fluid rendering when FluidRenderer is available
        // for (FluidFilterSlot slot : menu.getFluidSlots()) {
        //     FluidStack stack = slot.getFluidInventory().getFluid(slot.getSlotIndex());
        //     if (stack.isEmpty()) {
        //         continue;
        //     }
        //     FluidRenderer.INSTANCE.render(leftPos + slot.x, topPos + slot.y, stack);
        // }
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        // TODO: Implement fluid tooltip rendering when FluidRenderer is available
        // for (FluidFilterSlot slot : menu.getFluidSlots()) {
        //     FluidStack stack = slot.getFluidInventory().getFluid(slot.getSlotIndex());
        //     if (stack.isEmpty()) {
        //         continue;
        //     }
        //     if (!isHovering(slot.x, slot.y, 17, 17, mouseX, mouseY)) {
        //         continue;
        //     }
        //     graphics.renderTooltip(font, stack.getHoverName(), mouseX - leftPos, mouseY - topPos);
        // }
    }
} 
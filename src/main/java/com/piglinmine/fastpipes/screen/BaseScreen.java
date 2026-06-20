package com.piglinmine.fastpipes.screen;

import com.piglinmine.fastpipes.menu.BaseContainerMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

// TODO 1.21.11: Fluid rendering and tooltip rendering APIs changed massively (TextureAtlasSprite blit overloads removed,
// renderTooltip signature changed, IClientFluidTypeExtensions still works but blit no longer accepts sprite directly).
// Aggressive stub — fluid slot display is broken; only super.renderBg / super.renderLabels remain.
public abstract class BaseScreen<T extends BaseContainerMenu> extends AbstractContainerScreen<T> {
    public BaseScreen(T screenContainer, Inventory inv, Component title) {
        super(screenContainer, inv, title);
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTicks, int mouseX, int mouseY) {
        // TODO 1.21.11: fluid slot sprite rendering stubbed — blit(int,int,int,int,int,TextureAtlasSprite) removed
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        super.renderLabels(graphics, mouseX, mouseY);
        // TODO 1.21.11: fluid slot hover tooltip stubbed — renderTooltip(Font,Component,int,int) signature changed
    }
}

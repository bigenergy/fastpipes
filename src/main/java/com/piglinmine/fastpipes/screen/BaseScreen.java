package com.piglinmine.fastpipes.screen;

import com.piglinmine.fastpipes.menu.BaseContainerMenu;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

// MC 26.1.2 / NeoForge 26.1.2.76: renderBg/renderLabels were removed.
// extractBackground / extractLabels are the new hooks (using GuiGraphicsExtractor).
public abstract class BaseScreen<T extends BaseContainerMenu> extends AbstractContainerScreen<T> {
    public BaseScreen(T screenContainer, Inventory inv, Component title, int imageWidth, int imageHeight) {
        super(screenContainer, inv, title, imageWidth, imageHeight);
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTicks) {
        super.extractBackground(graphics, mouseX, mouseY, partialTicks);
        // Fluid-slot custom sprite rendering not yet ported (TextureAtlasSprite blit overload removed in 26.x).
    }

    @Override
    protected void extractLabels(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
        super.extractLabels(graphics, mouseX, mouseY);
        // Fluid-slot hover tooltip not yet ported.
    }
}

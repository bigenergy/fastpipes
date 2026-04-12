package com.piglinmine.fastpipes.barrel;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public class TieredBarrelScreen extends AbstractContainerScreen<TieredBarrelContainerMenu> {

    public TieredBarrelScreen(TieredBarrelContainerMenu menu, Inventory playerInv, Component title) {
        super(menu, playerInv, title);
        BarrelTier tier = menu.getTier();
        this.imageWidth = 176;
        this.imageHeight = 114 + tier.getRows() * 18;
        this.inventoryLabelY = this.imageHeight - 94;
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(graphics, mouseX, mouseY, partialTick);
        super.render(graphics, mouseX, mouseY, partialTick);
        this.renderTooltip(graphics, mouseX, mouseY);
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        int x = this.leftPos;
        int y = this.topPos;

        // Draw main panel background (MC gray)
        graphics.fill(x, y, x + imageWidth, y + imageHeight, 0xFFC6C6C6);

        // 3D border
        graphics.fill(x + 1, y + 1, x + imageWidth - 1, y + 2, 0xFFFFFFFF);
        graphics.fill(x + 1, y + 1, x + 2, y + imageHeight - 1, 0xFFFFFFFF);
        graphics.fill(x + imageWidth - 2, y + 1, x + imageWidth - 1, y + imageHeight - 1, 0xFF555555);
        graphics.fill(x + 1, y + imageHeight - 2, x + imageWidth - 1, y + imageHeight - 1, 0xFF555555);

        // Draw slot backgrounds
        for (var slot : menu.slots) {
            drawSlotBg(graphics, x + slot.x - 1, y + slot.y - 1);
        }
    }

    private void drawSlotBg(GuiGraphics graphics, int sx, int sy) {
        graphics.fill(sx, sy, sx + 18, sy + 1, 0xFF373737);
        graphics.fill(sx, sy, sx + 1, sy + 18, 0xFF373737);
        graphics.fill(sx + 17, sy + 1, sx + 18, sy + 18, 0xFFFFFFFF);
        graphics.fill(sx, sy + 17, sx + 18, sy + 18, 0xFFFFFFFF);
        graphics.fill(sx + 1, sy + 1, sx + 17, sy + 17, 0xFF8B8B8B);
    }
}

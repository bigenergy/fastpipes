package com.piglinmine.fastpipes.screen;

import com.piglinmine.fastpipes.menu.TerminalContainerMenu;
import com.piglinmine.fastpipes.network.FastPipesNetwork;
import com.piglinmine.fastpipes.network.message.TerminalExtractMessage;
import com.piglinmine.fastpipes.network.message.TerminalInsertMessage;
import com.piglinmine.fastpipes.network.message.TerminalSearchMessage;
import com.piglinmine.fastpipes.network.message.TerminalSortMessage;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public class TerminalScreen extends BaseScreen<TerminalContainerMenu> {
    private static final int GRID_X = 8;
    private static final int GRID_Y = 35;
    private static final int SLOT_SIZE = 18;

    private EditBox searchBox;
    private String statusMessage = "";
    private int statusTicks = 0;

    public TerminalScreen(TerminalContainerMenu container, Inventory inv, Component title) {
        super(container, inv, title);
        this.imageWidth = 195;
        this.imageHeight = 286;
    }

    @Override
    protected void init() {
        super.init();
        this.titleLabelX = 8;
        this.titleLabelY = 6;
        this.inventoryLabelX = 8;
        this.inventoryLabelY = 194;

        // Search box below title
        searchBox = new EditBox(this.font, this.leftPos + 8, this.topPos + 19, 153, 12, Component.empty());
        searchBox.setMaxLength(50);
        searchBox.setBordered(true);
        searchBox.setTextColor(0xFFFFFF);
        searchBox.setTextColorUneditable(0xA0A0A0);
        searchBox.setHint(Component.translatable("gui.fastpipes.terminal.search_hint").withStyle(net.minecraft.ChatFormatting.DARK_GRAY));
        searchBox.setResponder(text -> {
            menu.setSearchText(text);
            menu.setScrollOffset(0);
            FastPipesNetwork.sendToServer(new TerminalSearchMessage(text));
        });
        this.addRenderableWidget(searchBox);
    }

    @Override
    public void resize(net.minecraft.client.Minecraft mc, int width, int height) {
        String text = searchBox != null ? searchBox.getValue() : "";
        super.resize(mc, width, height);
        searchBox.setValue(text);
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTicks, int mouseX, int mouseY) {
        super.renderBg(graphics, partialTicks, mouseX, mouseY);

        int x = this.leftPos;
        int y = this.topPos;

        // Draw background panel
        graphics.fill(x, y, x + imageWidth, y + imageHeight, 0xFFC6C6C6);
        graphics.fill(x + 1, y + 1, x + imageWidth - 1, y + 2, 0xFFFFFFFF);
        graphics.fill(x + 1, y + 1, x + 2, y + imageHeight - 1, 0xFFFFFFFF);
        graphics.fill(x + imageWidth - 2, y + 1, x + imageWidth - 1, y + imageHeight - 1, 0xFF555555);
        graphics.fill(x + 1, y + imageHeight - 2, x + imageWidth - 1, y + imageHeight - 1, 0xFF555555);

        // Draw slot backgrounds for crafting grid and player inventory
        for (var slot : menu.slots) {
            drawSlotBackground(graphics, x + slot.x - 1, y + slot.y - 1);
        }

        // Draw network grid slots (vanilla style - same as inventory slots)
        for (int i = 0; i < TerminalContainerMenu.GRID_SIZE; i++) {
            int col = i % TerminalContainerMenu.GRID_COLS;
            int row = i / TerminalContainerMenu.GRID_COLS;
            int slotX = x + GRID_X + col * SLOT_SIZE;
            int slotY = y + GRID_Y + row * SLOT_SIZE;
            drawSlotBackground(graphics, slotX, slotY);
        }

        // Draw network items in grid
        List<ItemStack> visible = menu.getVisibleItems();
        for (int i = 0; i < visible.size(); i++) {
            int col = i % TerminalContainerMenu.GRID_COLS;
            int row = i / TerminalContainerMenu.GRID_COLS;
            int slotX = x + GRID_X + col * SLOT_SIZE + 1;
            int slotY = y + GRID_Y + row * SLOT_SIZE + 1;

            ItemStack stack = visible.get(i);
            graphics.renderItem(stack, slotX, slotY);

            // Render count
            if (stack.getCount() > 1) {
                String countStr = formatCount(stack.getCount());
                float scale = 0.75f;
                int scaledWidth = (int)(font.width(countStr) * scale);
                graphics.pose().pushPose();
                graphics.pose().translate(slotX + 16 - scaledWidth, slotY + 8, 200);
                graphics.pose().scale(scale, scale, 1.0f);
                graphics.drawString(font, countStr, 0, 0, 0xFFFFFF, true);
                graphics.pose().popPose();
            }
        }

        // Hover highlight on network grid
        int gridMouseX = mouseX - x - GRID_X;
        int gridMouseY = mouseY - y - GRID_Y;
        if (gridMouseX >= 0 && gridMouseY >= 0) {
            int hCol = gridMouseX / SLOT_SIZE;
            int hRow = gridMouseY / SLOT_SIZE;
            if (hCol < TerminalContainerMenu.GRID_COLS && hRow < TerminalContainerMenu.GRID_ROWS) {
                int idx = hRow * TerminalContainerMenu.GRID_COLS + hCol;
                List<ItemStack> vis = menu.getVisibleItems();
                if (idx < vis.size()) {
                    int hx = x + GRID_X + hCol * SLOT_SIZE + 1;
                    int hy = y + GRID_Y + hRow * SLOT_SIZE + 1;
                    graphics.fill(hx, hy, hx + 16, hy + 16, 0x80FFFFFF);
                }
            }
        }

        // Scrollbar
        int scrollBarX = x + GRID_X + TerminalContainerMenu.GRID_COLS * SLOT_SIZE + 2;
        int scrollBarY = y + GRID_Y + 1;
        int scrollBarHeight = TerminalContainerMenu.GRID_ROWS * SLOT_SIZE - 2;
        // Scrollbar track (inset)
        graphics.fill(scrollBarX, scrollBarY - 1, scrollBarX + 8, scrollBarY, 0xFF373737);
        graphics.fill(scrollBarX, scrollBarY - 1, scrollBarX + 1, scrollBarY + scrollBarHeight + 1, 0xFF373737);
        graphics.fill(scrollBarX + 7, scrollBarY, scrollBarX + 8, scrollBarY + scrollBarHeight + 1, 0xFFFFFFFF);
        graphics.fill(scrollBarX + 1, scrollBarY + scrollBarHeight, scrollBarX + 8, scrollBarY + scrollBarHeight + 1, 0xFFFFFFFF);
        graphics.fill(scrollBarX + 1, scrollBarY, scrollBarX + 7, scrollBarY + scrollBarHeight, 0xFF8B8B8B);

        int maxScroll = menu.getMaxScrollOffset();
        if (maxScroll > 0) {
            int thumbHeight = Math.max(10, scrollBarHeight * TerminalContainerMenu.GRID_ROWS /
                ((menu.getFilteredItems().size() + TerminalContainerMenu.GRID_COLS - 1) / TerminalContainerMenu.GRID_COLS));
            int thumbY = scrollBarY + (scrollBarHeight - thumbHeight) * menu.getScrollOffset() / maxScroll;
            graphics.fill(scrollBarX + 1, thumbY, scrollBarX + 7, thumbY + thumbHeight, 0xFFC6C6C6);
            // Thumb 3D effect
            graphics.fill(scrollBarX + 1, thumbY, scrollBarX + 6, thumbY + 1, 0xFFFFFFFF);
            graphics.fill(scrollBarX + 1, thumbY, scrollBarX + 2, thumbY + thumbHeight - 1, 0xFFFFFFFF);
            graphics.fill(scrollBarX + 6, thumbY + 1, scrollBarX + 7, thumbY + thumbHeight, 0xFF555555);
            graphics.fill(scrollBarX + 2, thumbY + thumbHeight - 1, scrollBarX + 7, thumbY + thumbHeight, 0xFF555555);
        }

        // Sort buttons (right of search bar)
        int sortBtnX = x + 163;
        int sortBtnY = y + 18;
        TerminalContainerMenu.SortMode currentSort = menu.getSortMode();
        drawSortButton(graphics, sortBtnX, sortBtnY, currentSort, mouseX, mouseY);

        // Crafting arrow between grid and result
        graphics.drawString(font, "=>", x + 68, y + 158, 0xFF404040, false);
    }

    private void drawSortButton(GuiGraphics graphics, int bx, int by, TerminalContainerMenu.SortMode mode, int mouseX, int mouseY) {
        int bw = 24;
        int bh = 13;
        boolean hovered = mouseX >= bx && mouseX < bx + bw && mouseY >= by && mouseY < by + bh;

        // Button background
        if (hovered) {
            graphics.fill(bx, by, bx + bw, by + bh, 0xFFAAAAAA);
        } else {
            graphics.fill(bx, by, bx + bw, by + bh, 0xFFC6C6C6);
        }
        // 3D border
        graphics.fill(bx, by, bx + bw, by + 1, 0xFFFFFFFF);
        graphics.fill(bx, by, bx + 1, by + bh, 0xFFFFFFFF);
        graphics.fill(bx + bw - 1, by, bx + bw, by + bh, 0xFF555555);
        graphics.fill(bx, by + bh - 1, bx + bw, by + bh, 0xFF555555);

        // Icon text
        String icon = switch (mode) {
            case NAME -> "Az";
            case COUNT -> "#";
            case MOD -> "@";
        };

        int tw = font.width(icon);
        graphics.drawString(font, icon, bx + (bw - tw) / 2, by + 3, 0xFF404040, false);
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        graphics.drawString(font, title, titleLabelX, titleLabelY, 4210752, false);
        graphics.drawString(font, Component.translatable("container.crafting"), 8, 126, 4210752, false);
        graphics.drawString(font, Component.translatable("container.inventory"), inventoryLabelX, inventoryLabelY, 4210752, false);

        // Status message — centered over network grid with dark background
        if (statusTicks > 0) {
            statusTicks--;
            int sw = font.width(statusMessage);
            int sx = (imageWidth - sw) / 2;
            int sy = GRID_Y + (TerminalContainerMenu.GRID_ROWS * SLOT_SIZE) / 2 - 5;
            int pad = 4;
            graphics.fill(sx - pad, sy - pad, sx + sw + pad, sy + font.lineHeight + pad, 0xE0000000);
            graphics.drawString(font, statusMessage, sx, sy, 0xFF5555, false);
        }

        // Sort button tooltip
        int sortBtnX = 163;
        int sortBtnY = 18;
        int relMouseX = mouseX - leftPos;
        int relMouseY = mouseY - topPos;
        if (relMouseX >= sortBtnX && relMouseX < sortBtnX + 24 && relMouseY >= sortBtnY && relMouseY < sortBtnY + 13) {
            TerminalContainerMenu.SortMode mode = menu.getSortMode();
            Component sortName = Component.translatable("gui.fastpipes.terminal.sort." + mode.name().toLowerCase());
            graphics.renderTooltip(font, sortName, relMouseX, relMouseY);
        }

        // Tooltip for hovered grid item
        int gridMouseX = mouseX - leftPos - GRID_X;
        int gridMouseY = mouseY - topPos - GRID_Y;
        if (gridMouseX >= 0 && gridMouseY >= 0) {
            int col = gridMouseX / SLOT_SIZE;
            int row = gridMouseY / SLOT_SIZE;
            if (col < TerminalContainerMenu.GRID_COLS && row < TerminalContainerMenu.GRID_ROWS) {
                int idx = row * TerminalContainerMenu.GRID_COLS + col;
                List<ItemStack> visible = menu.getVisibleItems();
                if (idx < visible.size()) {
                    ItemStack stack = visible.get(idx);
                    List<Component> tooltip = Screen.getTooltipFromItem(this.minecraft, stack);
                    tooltip.add(Component.translatable("gui.fastpipes.terminal.in_network", String.format("%,d", stack.getCount()))
                        .withStyle(net.minecraft.ChatFormatting.GRAY));
                    graphics.renderComponentTooltip(font, tooltip, mouseX - leftPos, mouseY - topPos);
                }
            }
        }
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(graphics);
        super.render(graphics, mouseX, mouseY, partialTick);
        this.renderTooltip(graphics, mouseX, mouseY);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        // Sort button click
        int sortBtnX = leftPos + 163;
        int sortBtnY = topPos + 18;
        if (mouseX >= sortBtnX && mouseX < sortBtnX + 24 && mouseY >= sortBtnY && mouseY < sortBtnY + 13) {
            menu.cycleSortMode();
            FastPipesNetwork.sendToServer(new TerminalSortMessage(menu.getSortMode().ordinal()));
            return true;
        }

        double gridX = mouseX - leftPos - GRID_X;
        double gridY = mouseY - topPos - GRID_Y;
        if (gridX >= 0 && gridY >= 0) {
            int col = (int) (gridX / SLOT_SIZE);
            int row = (int) (gridY / SLOT_SIZE);
            if (col < TerminalContainerMenu.GRID_COLS && row < TerminalContainerMenu.GRID_ROWS) {
                // Insert carried item into network
                ItemStack carried = menu.getCarried();
                if (!carried.isEmpty() && button == 0) {
                    FastPipesNetwork.sendToServer(new TerminalInsertMessage(false));
                    return true;
                }

                int idx = row * TerminalContainerMenu.GRID_COLS + col;
                List<ItemStack> visible = menu.getVisibleItems();
                if (idx < visible.size()) {
                    ItemStack clicked = visible.get(idx);
                    if (button == 0) {
                        int amount = Math.min(clicked.getCount(), clicked.getMaxStackSize());
                        FastPipesNetwork.sendToServer(new TerminalExtractMessage(clicked, amount, true));
                    } else if (button == 1) {
                        int amount = Math.max(1, Math.min(clicked.getCount(), clicked.getMaxStackSize()) / 2);
                        FastPipesNetwork.sendToServer(new TerminalExtractMessage(clicked, amount, true));
                    }
                    return true;
                }
                return true;
            }
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        menu.setScrollOffset(menu.getScrollOffset() - (int) delta);
        return true;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (searchBox.isFocused()) {
            if (keyCode == 256) { // Escape
                searchBox.setFocused(false);
                return true;
            }
            return searchBox.keyPressed(keyCode, scanCode, modifiers);
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean charTyped(char c, int modifiers) {
        if (searchBox.isFocused()) {
            return searchBox.charTyped(c, modifiers);
        }
        return super.charTyped(c, modifiers);
    }

    public void showStatus(String message) {
        this.statusMessage = message;
        this.statusTicks = 60;
    }

    private String formatCount(int count) {
        if (count >= 1_000_000) {
            int m = count / 1_000_000;
            int rem = (count % 1_000_000) / 100_000;
            if (m >= 10 || rem == 0) return m + "M";
            return m + "." + rem + "M";
        } else if (count >= 1_000) {
            int k = count / 1_000;
            int rem = (count % 1_000) / 100;
            if (k >= 10 || rem == 0) return k + "K";
            return k + "." + rem + "K";
        }
        return String.valueOf(count);
    }

    private void drawSlotBackground(GuiGraphics graphics, int sx, int sy) {
        graphics.fill(sx, sy, sx + 18, sy + 1, 0xFF373737);
        graphics.fill(sx, sy, sx + 1, sy + 18, 0xFF373737);
        graphics.fill(sx + 17, sy + 1, sx + 18, sy + 18, 0xFFFFFFFF);
        graphics.fill(sx, sy + 17, sx + 18, sy + 18, 0xFFFFFFFF);
        graphics.fill(sx + 1, sy + 1, sx + 17, sy + 17, 0xFF8B8B8B);
    }
}

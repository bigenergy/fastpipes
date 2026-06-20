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

    // 1.21.11: GuiGraphics.text() now silently skips rendering when ARGB.alpha(color) == 0
    // (see GuiGraphics#text). The legacy 0xRRGGBB constants (e.g. 4210752 == 0x00404040 dark gray
    // and 0x00FFFFFF white) all had implicit alpha 0 and are now invisible. Vanilla 1.21.11 uses
    // -12566464 (== 0xFF404040 signed). Mirror that with explicit full-alpha constants.
    private static final int LABEL_COLOR = 0xFF404040;        // was 4210752 (alpha 0 → invisible)
    private static final int WHITE_OPAQUE = 0xFFFFFFFF;       // was 0xFFFFFF (alpha 0 → invisible)
    private static final int STATUS_RED_OPAQUE = 0xFFFF5555;  // was 0xFF5555 (alpha 0 → invisible)

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
        // 1.21.11: GuiGraphics.text skips alpha-0 colors, so EditBox text colors must include
        // full alpha or they render invisibly (search box appeared as just a black bar).
        searchBox.setTextColor(WHITE_OPAQUE);
        searchBox.setTextColorUneditable(0xFFA0A0A0);
        searchBox.setHint(Component.translatable("gui.fastpipes.terminal.search_hint").withStyle(net.minecraft.ChatFormatting.DARK_GRAY));
        searchBox.setResponder(text -> {
            menu.setSearchText(text);
            menu.setScrollOffset(0);
            FastPipesNetwork.sendToServer(new TerminalSearchMessage(text));
        });
        this.addRenderableWidget(searchBox);
    }

    @Override
    public void resize(int width, int height) {
        // TODO 1.21.11: Screen.resize signature changed to (int, int); no longer takes Minecraft.
        String text = searchBox != null ? searchBox.getValue() : "";
        super.resize(width, height);
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
                // TODO 1.21.11: pose() returns Matrix3x2fStack; pushPose/popPose→pushMatrix/popMatrix, translate/scale args changed.
                // Count rendering simplified without pose transformations.
                String countStr = formatCount(stack.getCount());
                graphics.drawString(font, countStr, slotX + 16 - font.width(countStr), slotY + 8, WHITE_OPAQUE, true);
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
        graphics.drawString(font, title, titleLabelX, titleLabelY, LABEL_COLOR, false);
        graphics.drawString(font, Component.translatable("container.crafting"), 8, 126, LABEL_COLOR, false);
        graphics.drawString(font, Component.translatable("container.inventory"), inventoryLabelX, inventoryLabelY, LABEL_COLOR, false);

        // Status message — centered over network grid with dark background
        if (statusTicks > 0) {
            statusTicks--;
            int sw = font.width(statusMessage);
            int sx = (imageWidth - sw) / 2;
            int sy = GRID_Y + (TerminalContainerMenu.GRID_ROWS * SLOT_SIZE) / 2 - 5;
            int pad = 4;
            graphics.fill(sx - pad, sy - pad, sx + sw + pad, sy + font.lineHeight + pad, 0xE0000000);
            graphics.drawString(font, statusMessage, sx, sy, STATUS_RED_OPAQUE, false);
        }

        // Sort button tooltip
        int sortBtnX = 163;
        int sortBtnY = 18;
        int relMouseX = mouseX - leftPos;
        int relMouseY = mouseY - topPos;
        if (relMouseX >= sortBtnX && relMouseX < sortBtnX + 24 && relMouseY >= sortBtnY && relMouseY < sortBtnY + 13) {
            Component sortLabel = Component.translatable("gui.fastpipes.terminal.sort." + menu.getSortMode().name().toLowerCase());
            graphics.setTooltipForNextFrame(font, sortLabel, mouseX, mouseY);
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
                    if (!stack.isEmpty()) {
                        graphics.setTooltipForNextFrame(font, stack, mouseX, mouseY);
                    }
                }
            }
        }
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        // 1.21.11: Screen.renderWithTooltipAndSubtitles (outer driver) ALREADY calls
        // renderBackground() before render() and advances the stratum, so we MUST NOT call
        // renderBackground() again here — doing so re-fills the panel on the SAME stratum as
        // the labels that get drawn during super.render → renderContents → renderLabels, which
        // can hide them. AbstractContainerScreen.render() in 1.21.11 splits into
        //   renderContents (translates pose, calls renderBg via renderBackground chain is handled
        //                   externally; calls renderLabels in local coords)
        //   renderCarriedItem
        //   renderSnapbackItem
        // It no longer calls renderTooltip itself, so we set the hovered-slot tooltip via the
        // explicit renderTooltip() call below. mouseX/mouseY here are SCREEN-absolute (NOT
        // translated), which is what setTooltipForNextFrame expects since the tooltip is drawn
        // by renderDeferredElements after the pose stack has been popped.
        super.render(graphics, mouseX, mouseY, partialTick);
        this.renderTooltip(graphics, mouseX, mouseY);
    }

    // 1.21.11: mouseClicked now takes a MouseButtonEvent + a doubleClick boolean. Search box
    // input is dispatched automatically via the framework because searchBox is a registered
    // child widget; this override handles network-grid clicks and the sort-button click.

    @Override
    public boolean mouseClicked(net.minecraft.client.input.MouseButtonEvent event, boolean doubleClick) {
        double mouseX = event.x();
        double mouseY = event.y();
        int button = event.button();

        // Sort button click
        int sortBtnX = leftPos + 163;
        int sortBtnY = topPos + 18;
        if (mouseX >= sortBtnX && mouseX < sortBtnX + 24 && mouseY >= sortBtnY && mouseY < sortBtnY + 13) {
            if (button == 0 || button == 1) {
                TerminalContainerMenu.SortMode[] modes = TerminalContainerMenu.SortMode.values();
                TerminalContainerMenu.SortMode current = menu.getSortMode();
                TerminalContainerMenu.SortMode next = modes[(current.ordinal() + 1) % modes.length];
                menu.setSortMode(next);
                FastPipesNetwork.sendToServer(new TerminalSortMessage(next.ordinal()));
                return true;
            }
        }

        // Network grid click. The grid is NOT a vanilla slot, so vanilla mouseClicked would
        // treat any click here as a "drop outside" — we must intercept first.
        int gridMouseX = (int) (mouseX - leftPos - GRID_X);
        int gridMouseY = (int) (mouseY - topPos - GRID_Y);
        boolean inGrid = false;
        int gridIdx = -1;
        if (gridMouseX >= 0 && gridMouseY >= 0) {
            int col = gridMouseX / SLOT_SIZE;
            int row = gridMouseY / SLOT_SIZE;
            if (col < TerminalContainerMenu.GRID_COLS && row < TerminalContainerMenu.GRID_ROWS) {
                inGrid = true;
                gridIdx = row * TerminalContainerMenu.GRID_COLS + col;
            }
        }

        if (inGrid) {
            ItemStack carried = menu.getCarried();
            // PRIORITY 1: if the cursor is holding an item, ANY left/right click on the grid
            // inserts it into the network. This makes terminals behave like other storage GUIs
            // where dropping an item over the listing inserts it. Previously this only worked
            // via shift+click on a player slot, which is unintuitive.
            if (!carried.isEmpty() && (button == 0 || button == 1)) {
                FastPipesNetwork.sendToServer(new TerminalInsertMessage(button == 0));
                return true;
            }

            // PRIORITY 2: cursor empty AND there's a network item under the cursor → extract.
            List<ItemStack> visible = menu.getVisibleItems();
            if (gridIdx >= 0 && gridIdx < visible.size()) {
                ItemStack stack = visible.get(gridIdx);
                if (!stack.isEmpty()) {
                    com.mojang.blaze3d.platform.Window window = net.minecraft.client.Minecraft.getInstance().getWindow();
                    boolean shift = com.mojang.blaze3d.platform.InputConstants.isKeyDown(window, com.mojang.blaze3d.platform.InputConstants.KEY_LSHIFT)
                        || com.mojang.blaze3d.platform.InputConstants.isKeyDown(window, com.mojang.blaze3d.platform.InputConstants.KEY_RSHIFT);
                    int amount;
                    if (button == 1) {
                        // Right-click: half stack
                        amount = Math.min(stack.getCount(), Math.max(1, stack.getMaxStackSize() / 2));
                    } else {
                        // Left-click: full stack (capped at maxStackSize)
                        amount = Math.min(stack.getCount(), stack.getMaxStackSize());
                    }
                    // shift = into inventory, otherwise onto cursor
                    FastPipesNetwork.sendToServer(new TerminalExtractMessage(stack.copy(), amount, !shift));
                    return true;
                }
            }

            // Click on empty grid cell with empty cursor → swallow so vanilla doesn't drop.
            return true;
        }

        return super.mouseClicked(event, doubleClick);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        menu.setScrollOffset(menu.getScrollOffset() - (int) scrollY);
        return true;
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

package com.piglinmine.fastpipes.screen;

import com.piglinmine.fastpipes.FastPipes;
import com.piglinmine.fastpipes.menu.ExtractorAttachmentContainerMenu;
import com.piglinmine.fastpipes.network.pipe.attachment.extractor.BlacklistWhitelist;
import com.piglinmine.fastpipes.network.pipe.attachment.extractor.ExtractorAttachment;
import com.piglinmine.fastpipes.network.pipe.attachment.extractor.RedstoneMode;
import com.piglinmine.fastpipes.network.pipe.attachment.extractor.RoutingMode;
import com.piglinmine.fastpipes.menu.slot.FilterSlot;
import com.piglinmine.fastpipes.menu.slot.FluidFilterSlot;
import com.piglinmine.fastpipes.screen.widget.IconButton;
import com.piglinmine.fastpipes.screen.widget.IconButtonPreset;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.fluids.FluidStack;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class ExtractorAttachmentScreen extends BaseScreen<ExtractorAttachmentContainerMenu> {
    private static final ResourceLocation RESOURCE = ResourceLocation.fromNamespaceAndPath(FastPipes.MOD_ID, "textures/gui/extractor_attachment.png");
    private static final ResourceLocation TAG_OVERLAY = ResourceLocation.fromNamespaceAndPath(FastPipes.MOD_ID, "textures/gui/tag_overlay.png");

    private final List<Component> tooltip = new ArrayList<>();

    private Button redstoneModeButton;
    private Button blacklistWhitelistButton;
    @Nullable
    private Button routingModeButton;
    @Nullable
    private Button exactModeButton;
    @Nullable
    private Button plusButton;
    @Nullable
    private Button minusButton;

    // Filter editor modal
    private int editingSlotIndex = -1;
    private EditBox filterStringBox;
    private Button editorSubmitButton;
    private Button editorCancelButton;
    private String tagsDisplay = "";
    private int tagsScrollOffset = 0;
    private String[] tagsLines = new String[0];

    public ExtractorAttachmentScreen(ExtractorAttachmentContainerMenu container, Inventory inv, Component title) {
        super(container, inv, title);

        this.imageWidth = 176;
        this.imageHeight = 193;
    }

    @Override
    protected void init() {
        super.init();
        this.titleLabelX = 7;
        this.inventoryLabelX = 7;

        redstoneModeButton = this.addRenderableWidget(new IconButton(
            this.leftPos + 32,
            this.topPos + 76,
            IconButtonPreset.NORMAL,
            getRedstoneModeX(menu.getRedstoneMode()),
            61,
            getRedstoneModeText(menu.getRedstoneMode()),
            btn -> setRedstoneMode((IconButton) btn, menu.getRedstoneMode().next())
        ));

        redstoneModeButton.active = menu.getExtractorAttachmentType().getCanSetRedstoneMode();

        blacklistWhitelistButton = this.addRenderableWidget(new IconButton(
            this.leftPos + 55,
            this.topPos + 76,
            IconButtonPreset.NORMAL,
            getBlacklistWhitelistX(menu.getBlacklistWhitelist()),
            82,
            getBlacklistWhitelistText(menu.getBlacklistWhitelist()),
            btn -> setBlacklistWhitelist((IconButton) btn, menu.getBlacklistWhitelist().next())
        ));

        blacklistWhitelistButton.active = menu.getExtractorAttachmentType().getCanSetWhitelistBlacklist();

        exactModeButton = this.addRenderableWidget(new IconButton(
            this.leftPos + 78,
            this.topPos + 76,
            IconButtonPreset.NORMAL,
            getExactModeX(menu.isExactMode()),
            103,
            getExactModeText(menu.isExactMode()),
            btn -> setExactMode((IconButton) btn, !menu.isExactMode())
        ));

        exactModeButton.active = menu.getExtractorAttachmentType().getCanSetExactMode();

        if (!menu.isFluidMode()) {
            routingModeButton = this.addRenderableWidget(new IconButton(
                this.leftPos + 101,
                this.topPos + 76,
                IconButtonPreset.NORMAL,
                getRoutingModeX(menu.getRoutingMode()),
                194,
                getRoutingModeText(menu.getRoutingMode()),
                btn -> setRoutingMode((IconButton) btn, menu.getRoutingMode().next())
            ));

            routingModeButton.active = menu.getExtractorAttachmentType().getCanSetWhitelistBlacklist();

            plusButton = this.addRenderableWidget(new IconButton(
                this.leftPos + 124,
                this.topPos + 76 - 3,
                IconButtonPreset.SMALL,
                198,
                19,
                Component.literal("+"),
                btn -> updateStackSize(1)
            ));

            minusButton = this.addRenderableWidget(new IconButton(
                this.leftPos + 124,
                this.topPos + 76 + 14 - 3,
                IconButtonPreset.SMALL,
                198,
                34,
                Component.literal("-"),
                btn -> updateStackSize(-1)
            ));

            minusButton.active = menu.getStackSize() > 0;
            plusButton.active = menu.getStackSize() < menu.getExtractorAttachmentType().getItemsToExtract();
        }
    }

    private void updateStackSize(int amount) {
        if (hasShiftDown()) {
            amount *= 4;
        }

        int newAmount = menu.getStackSize() + amount;
        if (newAmount < 0) {
            newAmount = 0;
        }

        if (newAmount > menu.getExtractorAttachmentType().getItemsToExtract()) {
            newAmount = menu.getExtractorAttachmentType().getItemsToExtract();
        }

        minusButton.active = newAmount > 0;
        plusButton.active = newAmount < menu.getExtractorAttachmentType().getItemsToExtract();

        menu.setStackSize(newAmount);
    }

    private int getRedstoneModeX(RedstoneMode redstoneMode) {
        switch (redstoneMode) {
            case IGNORED:
                return 219;
            case HIGH:
                return 177;
            case LOW:
                return 198;
            default:
                return 0;
        }
    }

    private MutableComponent getRedstoneModeText(RedstoneMode redstoneMode) {
        return Component.translatable("misc.fastpipes.redstone_mode." + redstoneMode.toString().toLowerCase());
    }

    private void setRedstoneMode(IconButton button, RedstoneMode redstoneMode) {
        button.setMessage(getRedstoneModeText(redstoneMode));
        button.setOverlayTexX(getRedstoneModeX(redstoneMode));

        menu.setRedstoneMode(redstoneMode);
    }

    private int getBlacklistWhitelistX(BlacklistWhitelist blacklistWhitelist) {
        switch (blacklistWhitelist) {
            case BLACKLIST:
                return 198;
            case WHITELIST:
                return 177;
            default:
                return 0;
        }
    }

    private MutableComponent getBlacklistWhitelistText(BlacklistWhitelist blacklistWhitelist) {
        return Component.translatable("misc.fastpipes.mode." + blacklistWhitelist.toString().toLowerCase());
    }

    private void setBlacklistWhitelist(IconButton button, BlacklistWhitelist blacklistWhitelist) {
        button.setMessage(getBlacklistWhitelistText(blacklistWhitelist));
        button.setOverlayTexX(getBlacklistWhitelistX(blacklistWhitelist));

        menu.setBlacklistWhitelist(blacklistWhitelist);
    }

    private int getRoutingModeX(RoutingMode routingMode) {
        switch (routingMode) {
            case NEAREST:
                return 0;
            case FURTHEST:
                return 21;
            case RANDOM:
                return 42;
            case ROUND_ROBIN:
                return 63;
            default:
                return 0;
        }
    }

    private MutableComponent getRoutingModeText(RoutingMode routingMode) {
        return Component.translatable("misc.fastpipes.routing_mode." + routingMode.toString().toLowerCase());
    }

    private void setRoutingMode(IconButton button, RoutingMode routingMode) {
        button.setMessage(getRoutingModeText(routingMode));
        button.setOverlayTexX(getRoutingModeX(routingMode));

        menu.setRoutingMode(routingMode);
    }

    private int getExactModeX(boolean exactMode) {
        return exactMode ? 177 : 198;
    }

    private MutableComponent getExactModeText(boolean exactMode) {
        return Component.translatable("misc.fastpipes.exact_mode." + (exactMode ? "on" : "off"));
    }

    private void setExactMode(IconButton button, boolean exactMode) {
        button.setMessage(getExactModeText(exactMode));
        button.setOverlayTexX(getExactModeX(exactMode));

        menu.setExactMode(exactMode);
    }

    @Override
    protected void slotClicked(Slot slot, int slotId, int mouseButton, ClickType type) {
        if (slot instanceof FilterSlot || slot instanceof FluidFilterSlot) {
            if (mouseButton == 1) {
                // Right-click: open editor for existing filter entry
                openFilterEditor(slot);
                return;
            }
            // Left-click: normal insert/clear behavior
            super.slotClicked(slot, slotId, mouseButton, type);
            return;
        }
        super.slotClicked(slot, slotId, mouseButton, type);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (editingSlotIndex >= 0) {
            if (filterStringBox != null && filterStringBox.mouseClicked(mouseX, mouseY, button)) return true;
            if (editorSubmitButton != null && editorSubmitButton.mouseClicked(mouseX, mouseY, button)) return true;
            if (editorCancelButton != null && editorCancelButton.mouseClicked(mouseX, mouseY, button)) return true;

            // Check if clicked on a tag line in the tags area
            if (tagsLines.length > 0 && filterStringBox != null) {
                int panelW = 220;
                int panelX = (this.width - panelW) / 2;
                int panelY = (this.height - 170) / 2;
                int tagsAreaTop = panelY + 62;
                int tagsAreaLeft = panelX + 10;
                int tagsAreaRight = panelX + panelW - 10;
                int maxVisibleLines = 76 / 10;

                if (mouseX >= tagsAreaLeft && mouseX <= tagsAreaRight && mouseY >= tagsAreaTop) {
                    int lineIndex = (int) ((mouseY - tagsAreaTop - 2) / 10) + tagsScrollOffset;
                    if (lineIndex >= 0 && lineIndex < tagsLines.length) {
                        filterStringBox.setValue(tagsLines[lineIndex]);
                        return true;
                    }
                }
            }

            return true; // consume all clicks while editing
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (editingSlotIndex >= 0) {
            if (keyCode == 256) { // Escape
                closeFilterEditor();
                return true;
            }
            if (keyCode == 257) { // Enter
                submitFilterEdit();
                return true;
            }
            if (filterStringBox != null && filterStringBox.keyPressed(keyCode, scanCode, modifiers)) return true;
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean charTyped(char codePoint, int modifiers) {
        if (editingSlotIndex >= 0) {
            if (filterStringBox != null) return filterStringBox.charTyped(codePoint, modifiers);
            return true;
        }
        return super.charTyped(codePoint, modifiers);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        if (editingSlotIndex >= 0 && tagsLines.length > 0) {
            int panelW = 220;
            int panelH = 170;
            int tagsAreaHeight = 76; // tagsAreaBottom - tagsAreaTop
            int maxVisibleLines = tagsAreaHeight / 10;
            if (scrollY < 0) {
                tagsScrollOffset = Math.min(tagsScrollOffset + 1, Math.max(0, tagsLines.length - maxVisibleLines));
            } else if (scrollY > 0) {
                tagsScrollOffset = Math.max(tagsScrollOffset - 1, 0);
            }
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
    }

    private void openFilterEditor(Slot slot) {
        this.editingSlotIndex = slot.getSlotIndex();

        String currentValue = "";
        String override = menu.getTagOverride(slot.getSlotIndex());

        if (!override.isEmpty()) {
            currentValue = override;
            tagsDisplay = "";
        } else if (slot instanceof FilterSlot) {
            ItemStack stack = slot.getItem();
            if (!stack.isEmpty()) {
                currentValue = BuiltInRegistries.ITEM.getKey(stack.getItem()).toString();
                StringBuilder sb = new StringBuilder();
                stack.getTags().forEach(tag -> {
                    if (sb.length() > 0) sb.append("\n");
                    sb.append("#").append(tag.location());
                });
                tagsDisplay = sb.toString();
            } else {
                tagsDisplay = "";
            }
        } else if (slot instanceof FluidFilterSlot fluidSlot) {
            FluidStack fluid = fluidSlot.getFluidInventory().getFluid(fluidSlot.getSlotIndex());
            if (!fluid.isEmpty()) {
                currentValue = BuiltInRegistries.FLUID.getKey(fluid.getFluid()).toString();
                StringBuilder sb = new StringBuilder();
                fluid.getFluid().builtInRegistryHolder().tags().forEach(tag -> {
                    if (sb.length() > 0) sb.append("\n");
                    sb.append("#").append(tag.location());
                });
                tagsDisplay = sb.toString();
            } else {
                tagsDisplay = "";
            }
        }

        tagsLines = tagsDisplay.isEmpty() ? new String[0] : tagsDisplay.split("\n");
        tagsScrollOffset = 0;

        int panelW = 220;
        int panelH = 170;
        int panelX = (this.width - panelW) / 2;
        int panelY = (this.height - panelH) / 2;

        filterStringBox = new EditBox(font, panelX + 35, panelY + 25, 175, 16, Component.empty());
        filterStringBox.setMaxLength(256);
        filterStringBox.setValue(currentValue);
        filterStringBox.setFocused(true);
        addWidget(filterStringBox);

        editorSubmitButton = Button.builder(
            Component.translatable("gui.fastpipes.filter_editor.submit"),
            btn -> submitFilterEdit()
        ).bounds(panelX + 115, panelY + 145, 95, 20).build();
        addWidget(editorSubmitButton);

        editorCancelButton = Button.builder(
            Component.translatable("gui.fastpipes.filter_editor.cancel"),
            btn -> closeFilterEditor()
        ).bounds(panelX + 10, panelY + 145, 95, 20).build();
        addWidget(editorCancelButton);
    }

    private void submitFilterEdit() {
        if (editingSlotIndex < 0) return;
        String value = filterStringBox.getValue().trim();
        menu.setTagOverride(editingSlotIndex, value.startsWith("#") ? value : "");
        // If it's an item/fluid ID (not a tag), also send it via the same message
        if (!value.isEmpty() && !value.startsWith("#")) {
            // Send as item/fluid ID - server will resolve and set the slot
            com.piglinmine.fastpipes.network.FastPipesNetwork.sendToServer(
                new com.piglinmine.fastpipes.network.message.UpdateFilterEntryMessage(
                    menu.getPos(), menu.getDirection(), editingSlotIndex, value));
        } else if (value.isEmpty()) {
            // Clear the slot
            com.piglinmine.fastpipes.network.FastPipesNetwork.sendToServer(
                new com.piglinmine.fastpipes.network.message.UpdateFilterEntryMessage(
                    menu.getPos(), menu.getDirection(), editingSlotIndex, ""));
        }
        closeFilterEditor();
    }

    private void closeFilterEditor() {
        if (filterStringBox != null) {
            removeWidget(filterStringBox);
            filterStringBox = null;
        }
        if (editorSubmitButton != null) {
            removeWidget(editorSubmitButton);
            editorSubmitButton = null;
        }
        if (editorCancelButton != null) {
            removeWidget(editorCancelButton);
            editorCancelButton = null;
        }
        editingSlotIndex = -1;
        tagsDisplay = "";
        tagsLines = new String[0];
        tagsScrollOffset = 0;
    }

    private void renderFilterEditor(GuiGraphics graphics, int mouseX, int mouseY) {
        var pose = graphics.pose();
        pose.pushPose();
        pose.translate(0, 0, 300); // Render above item slots

        // Dark overlay
        graphics.fill(0, 0, this.width, this.height, 0x80000000);

        int panelW = 220;
        int panelH = 170;
        int panelX = (this.width - panelW) / 2;
        int panelY = (this.height - panelH) / 2;

        // Panel background (Minecraft style)
        graphics.fill(panelX, panelY, panelX + panelW, panelY + panelH, 0xFFC6C6C6);
        // Border
        graphics.fill(panelX, panelY, panelX + panelW, panelY + 2, 0xFFFFFFFF);
        graphics.fill(panelX, panelY, panelX + 2, panelY + panelH, 0xFFFFFFFF);
        graphics.fill(panelX + panelW - 2, panelY, panelX + panelW, panelY + panelH, 0xFF555555);
        graphics.fill(panelX, panelY + panelH - 2, panelX + panelW, panelY + panelH, 0xFF555555);

        // Item/Tag label
        graphics.drawString(font, Component.translatable("gui.fastpipes.filter_editor.item_tag"), panelX + 10, panelY + 10, 0x404040, false);

        // Preview icon
        String val = filterStringBox != null ? filterStringBox.getValue().trim() : "";
        if (!val.isEmpty() && !val.startsWith("#")) {
            var rl = net.minecraft.resources.ResourceLocation.tryParse(val);
            if (rl != null) {
                if (menu.isFluidMode()) {
                    var fluid = BuiltInRegistries.FLUID.get(rl);
                    if (fluid != net.minecraft.world.level.material.Fluids.EMPTY) {
                        graphics.drawString(font, fluid.defaultFluidState().createLegacyBlock().getBlock().getName(), panelX + 10, panelY + 28, 0x404040, false);
                    }
                } else {
                    var item = BuiltInRegistries.ITEM.get(rl);
                    if (item != net.minecraft.world.item.Items.AIR) {
                        graphics.renderItem(new ItemStack(item), panelX + 12, panelY + 23);
                    }
                }
            }
        }

        // EditBox
        if (filterStringBox != null) filterStringBox.render(graphics, mouseX, mouseY, 0);

        // Tags label
        graphics.drawString(font, Component.translatable("gui.fastpipes.filter_editor.tags"), panelX + 10, panelY + 50, 0x404040, false);

        // Tags display area (dark background, scrollable)
        int tagsAreaTop = panelY + 62;
        int tagsAreaBottom = panelY + 138;
        int tagsAreaHeight = tagsAreaBottom - tagsAreaTop;
        graphics.fill(panelX + 10, tagsAreaTop, panelX + panelW - 10, tagsAreaBottom, 0xFF000000);

        int maxVisibleLines = tagsAreaHeight / 10;
        if (tagsLines.length > 0) {
            for (int i = tagsScrollOffset; i < tagsLines.length && (i - tagsScrollOffset) < maxVisibleLines; i++) {
                int tagY = tagsAreaTop + 2 + (i - tagsScrollOffset) * 10;
                // Highlight on hover
                boolean hovered = mouseX >= panelX + 10 && mouseX <= panelX + panelW - 10
                    && mouseY >= tagY && mouseY < tagY + 10;
                if (hovered) {
                    graphics.fill(panelX + 11, tagY - 1, panelX + panelW - 11, tagY + 9, 0x40FFFFFF);
                }
                graphics.drawString(font, tagsLines[i], panelX + 14, tagY, hovered ? 0xFFFF55 : 0x55FF55, false);
            }
            // Scroll indicator
            if (tagsLines.length > maxVisibleLines) {
                String indicator = "▲▼ " + (tagsScrollOffset + 1) + "-" + Math.min(tagsScrollOffset + maxVisibleLines, tagsLines.length) + "/" + tagsLines.length;
                graphics.drawString(font, indicator, panelX + panelW - 10 - font.width(indicator), tagsAreaBottom + 1, 0x808080, false);
            }
        } else if (val.startsWith("#")) {
            graphics.drawString(font, val, panelX + 14, tagsAreaTop + 2, 0xFFFF55, false);
        }

        // Buttons
        if (editorSubmitButton != null) editorSubmitButton.render(graphics, mouseX, mouseY, 0);
        if (editorCancelButton != null) editorCancelButton.render(graphics, mouseX, mouseY, 0);

        pose.popPose();
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(graphics, mouseX, mouseY, partialTick);
        // When editor is open, pass fake mouse coords to prevent hover effects on underlying UI
        if (editingSlotIndex >= 0) {
            super.render(graphics, -1, -1, partialTick);
            renderFilterEditor(graphics, mouseX, mouseY);
        } else {
            super.render(graphics, mouseX, mouseY, partialTick);
            this.renderTooltip(graphics, mouseX, mouseY);
        }
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {

        if (!menu.isFluidMode()) {
            font.drawInBatch("" + menu.getStackSize(), 139, 83, 4210752, false, graphics.pose().last().pose(), graphics.bufferSource(), net.minecraft.client.gui.Font.DisplayMode.NORMAL, 0, 15728880);
        }

        // Don't show tooltips when editor is open
        if (editingSlotIndex >= 0) {
            super.renderLabels(graphics, mouseX, mouseY);
            return;
        }

        tooltip.clear();

        if (blacklistWhitelistButton.isHovered()) {
            tooltip.add(Component.translatable("misc.fastpipes.mode"));
            tooltip.add(getBlacklistWhitelistText(menu.getBlacklistWhitelist()).withStyle(ChatFormatting.GRAY));
        } else if (redstoneModeButton.isHovered()) {
            tooltip.add(Component.translatable("misc.fastpipes.redstone_mode"));
            tooltip.add(getRedstoneModeText(menu.getRedstoneMode()).withStyle(ChatFormatting.GRAY));
        } else if (routingModeButton != null && routingModeButton.isHovered()) {
            tooltip.add(Component.translatable("misc.fastpipes.routing_mode"));
            tooltip.add(getRoutingModeText(menu.getRoutingMode()).withStyle(ChatFormatting.GRAY));
        } else if (exactModeButton.isHovered()) {
            tooltip.add(Component.translatable("misc.fastpipes.exact_mode"));
            tooltip.add(getExactModeText(menu.isExactMode()).withStyle(ChatFormatting.GRAY));
        }

        if (!tooltip.isEmpty()) {
            graphics.renderComponentTooltip(font, tooltip, mouseX - leftPos, mouseY - topPos);
        }

        super.renderLabels(graphics, mouseX, mouseY);
    }

    @Override
    protected void renderBg(@NotNull GuiGraphics graphics, float partialTicks, int mouseX, int mouseY) {
        int i = (this.width - this.imageWidth) / 2;
        int j = (this.height - this.imageHeight) / 2;
        graphics.blit(RESOURCE, i, j, 0, 0, this.imageWidth, this.imageHeight);

        int x = 43;
        int y = 18;
        for (int filterSlotId = 1; filterSlotId <= ExtractorAttachment.MAX_FILTER_SLOTS; ++filterSlotId) {
            if (filterSlotId > menu.getExtractorAttachmentType().getFilterSlots()) {
                graphics.blit(RESOURCE, i + x, j + y, 198, 0, 18, 18);
            }

            if (filterSlotId % 5 == 0) {
                x = 43;
                y += 18;
            } else {
                x += 18;
            }
        }

        // Render tag override icon on filter slots
        x = 43;
        y = 18;
        for (int slotIdx = 0; slotIdx < menu.getExtractorAttachmentType().getFilterSlots(); ++slotIdx) {
            String override = menu.getTagOverride(slotIdx);
            if (!override.isEmpty()) {
                graphics.blit(TAG_OVERLAY, i + x, j + y, 0, 0, 16, 16, 16, 16);
            }

            if ((slotIdx + 1) % 5 == 0) {
                x = 43;
                y += 18;
            } else {
                x += 18;
            }
        }

        super.renderBg(graphics, partialTicks, mouseX, mouseY);
    }
} 
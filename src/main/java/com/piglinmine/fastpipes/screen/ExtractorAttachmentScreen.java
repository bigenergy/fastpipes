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
import net.minecraft.resources.Identifier;
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
    private static final Identifier RESOURCE = Identifier.fromNamespaceAndPath(FastPipes.MOD_ID, "textures/gui/extractor_attachment.png");
    private static final Identifier TAG_OVERLAY = Identifier.fromNamespaceAndPath(FastPipes.MOD_ID, "textures/gui/tag_overlay.png");

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
        // Mode buttons row sits at y=76..94 (76 + 18 = 94 for NORMAL preset; the +/- minus button
        // extends a few pixels above/below). Default inventoryLabelY = imageHeight - 94 = 99 would
        // overlap. Push the "Inventory" label down to clear the buttons.
        this.inventoryLabelY = 100;

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
        // 1.21.11: hasShiftDown() removed from Screen — query window state directly via InputConstants.
        com.mojang.blaze3d.platform.Window window = net.minecraft.client.Minecraft.getInstance().getWindow();
        boolean shift = com.mojang.blaze3d.platform.InputConstants.isKeyDown(window, com.mojang.blaze3d.platform.InputConstants.KEY_LSHIFT)
            || com.mojang.blaze3d.platform.InputConstants.isKeyDown(window, com.mojang.blaze3d.platform.InputConstants.KEY_RSHIFT);
        if (shift) {
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

    // TODO 1.21.11: mouseClicked/keyPressed/charTyped signatures changed to use MouseButtonEvent/KeyEvent/CharacterEvent.
    // Filter editor input handling is broken until the new event-based overrides are wired up.

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
        // TODO 1.21.11: graphics.pose() now returns Matrix3x2fStack with pushMatrix/popMatrix/translate(Matrix3x2f);
        // Registry.get returns Optional<Holder.Reference<T>>; renderTooltip/blit signatures changed.
        // Filter editor modal is aggressively stubbed for compile.
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
            // 1.21.11: GuiGraphics.text skips colors with alpha 0. The legacy 4210752 (0x00404040)
            // had implicit alpha 0 and rendered nothing; vanilla now uses 0xFF404040 (signed -12566464).
            graphics.drawString(font, "" + menu.getStackSize(), 139, 83, 0xFF404040, false);
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
            // 1.21.11: setTooltipForNextFrame schedules a deferred render that runs AFTER the
            // (leftPos, topPos) pose translation has been popped (see GuiGraphics.renderDeferredElements
            // / setTooltipForNextFrameInternal — the closure captures xo/yo at submit time, runs at
            // identity pose on the next-stratum). So we must pass SCREEN-absolute mouse coords here;
            // subtracting leftPos/topPos produced top-left rendering.
            graphics.setComponentTooltipForNextFrame(font, tooltip, mouseX, mouseY);
        }

        super.renderLabels(graphics, mouseX, mouseY);
    }

    @Override
    protected void renderBg(@NotNull GuiGraphics graphics, float partialTicks, int mouseX, int mouseY) {
        int i = (this.width - this.imageWidth) / 2;
        int j = (this.height - this.imageHeight) / 2;
        graphics.blit(net.minecraft.client.renderer.RenderPipelines.GUI_TEXTURED,
            RESOURCE, i, j, 0f, 0f, this.imageWidth, this.imageHeight, 256, 256);

        int x = 43;
        int y = 18;
        for (int filterSlotId = 1; filterSlotId <= ExtractorAttachment.MAX_FILTER_SLOTS; ++filterSlotId) {
            if (filterSlotId > menu.getExtractorAttachmentType().getFilterSlots()) {
                // Locked filter slot overlay. Master 1.21.1 uses UV (198, 0) — the gray "disabled" slot
                // sprite. The previous (240, 0) hit the +/- button icons region instead.
                graphics.blit(net.minecraft.client.renderer.RenderPipelines.GUI_TEXTURED,
                    RESOURCE, i + x, j + y, 198f, 0f, 18, 18, 256, 256);
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
                graphics.blit(
                    net.minecraft.client.renderer.RenderPipelines.GUI_TEXTURED,
                    TAG_OVERLAY, i + x, j + y, 0f, 0f, 16, 16, 16, 16);
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
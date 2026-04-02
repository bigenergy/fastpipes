package com.piglinmine.fastpipes.screen;

import com.piglinmine.fastpipes.FastPipes;
import com.piglinmine.fastpipes.menu.InserterAttachmentContainerMenu;
import com.piglinmine.fastpipes.network.pipe.attachment.extractor.BlacklistWhitelist;
import com.piglinmine.fastpipes.network.pipe.attachment.extractor.RedstoneMode;
import com.piglinmine.fastpipes.network.pipe.attachment.inserter.InserterAttachment;
import com.piglinmine.fastpipes.screen.widget.IconButton;
import com.piglinmine.fastpipes.screen.widget.IconButtonPreset;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class InserterAttachmentScreen extends BaseScreen<InserterAttachmentContainerMenu> {
    // Reuse extractor texture — same layout (filter slots + mode buttons)
    private static final ResourceLocation RESOURCE = ResourceLocation.fromNamespaceAndPath(FastPipes.MOD_ID, "textures/gui/extractor_attachment.png");

    private final List<Component> tooltip = new ArrayList<>();

    private IconButton redstoneModeButton;
    private IconButton blacklistWhitelistButton;
    @Nullable
    private IconButton exactModeButton;

    public InserterAttachmentScreen(InserterAttachmentContainerMenu container, Inventory inv, Component title) {
        super(container, inv, title);
        this.imageWidth = 176;
        this.imageHeight = 193;
    }

    @Override
    protected void init() {
        super.init();

        redstoneModeButton = this.addRenderableWidget(new IconButton(
            this.leftPos + 32, this.topPos + 76,
            IconButtonPreset.NORMAL,
            getRedstoneModeX(menu.getRedstoneMode()), 61,
            getRedstoneModeText(menu.getRedstoneMode()),
            btn -> setRedstoneMode((IconButton) btn, menu.getRedstoneMode().next())
        ));
        redstoneModeButton.active = menu.getInserterAttachmentType().getCanSetRedstoneMode();

        blacklistWhitelistButton = this.addRenderableWidget(new IconButton(
            this.leftPos + 55, this.topPos + 76,
            IconButtonPreset.NORMAL,
            getBlacklistWhitelistX(menu.getBlacklistWhitelist()), 82,
            getBlacklistWhitelistText(menu.getBlacklistWhitelist()),
            btn -> setBlacklistWhitelist((IconButton) btn, menu.getBlacklistWhitelist().next())
        ));
        blacklistWhitelistButton.active = menu.getInserterAttachmentType().getCanSetWhitelistBlacklist();

        exactModeButton = this.addRenderableWidget(new IconButton(
            this.leftPos + 78, this.topPos + 76,
            IconButtonPreset.NORMAL,
            getExactModeX(menu.isExactMode()), 103,
            getExactModeText(menu.isExactMode()),
            btn -> setExactMode((IconButton) btn, !menu.isExactMode())
        ));
        exactModeButton.active = menu.getInserterAttachmentType().getCanSetExactMode();
    }

    private int getRedstoneModeX(RedstoneMode mode) {
        switch (mode) {
            case IGNORED: return 219;
            case HIGH:    return 177;
            case LOW:     return 198;
            default:      return 0;
        }
    }

    private MutableComponent getRedstoneModeText(RedstoneMode mode) {
        return Component.translatable("misc.fastpipes.redstone_mode." + mode.toString().toLowerCase());
    }

    private void setRedstoneMode(IconButton button, RedstoneMode mode) {
        button.setMessage(getRedstoneModeText(mode));
        button.setOverlayTexX(getRedstoneModeX(mode));
        menu.setRedstoneMode(mode);
    }

    private int getBlacklistWhitelistX(BlacklistWhitelist bw) {
        return bw == BlacklistWhitelist.BLACKLIST ? 198 : 177;
    }

    private MutableComponent getBlacklistWhitelistText(BlacklistWhitelist bw) {
        return Component.translatable("misc.fastpipes.mode." + bw.toString().toLowerCase());
    }

    private void setBlacklistWhitelist(IconButton button, BlacklistWhitelist bw) {
        button.setMessage(getBlacklistWhitelistText(bw));
        button.setOverlayTexX(getBlacklistWhitelistX(bw));
        menu.setBlacklistWhitelist(bw);
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
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(graphics, mouseX, mouseY, partialTick);
        super.render(graphics, mouseX, mouseY, partialTick);
        this.renderTooltip(graphics, mouseX, mouseY);
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        font.drawInBatch(title.getString(), 7, 7, 4210752, false, graphics.pose().last().pose(), graphics.bufferSource(), net.minecraft.client.gui.Font.DisplayMode.NORMAL, 0, 15728880);
        font.drawInBatch(I18n.get("container.inventory"), 7, 103 - 4, 4210752, false, graphics.pose().last().pose(), graphics.bufferSource(), net.minecraft.client.gui.Font.DisplayMode.NORMAL, 0, 15728880);

        tooltip.clear();

        if (blacklistWhitelistButton.isHovered()) {
            tooltip.add(Component.translatable("misc.fastpipes.mode"));
            tooltip.add(getBlacklistWhitelistText(menu.getBlacklistWhitelist()).withStyle(ChatFormatting.GRAY));
        } else if (redstoneModeButton.isHovered()) {
            tooltip.add(Component.translatable("misc.fastpipes.redstone_mode"));
            tooltip.add(getRedstoneModeText(menu.getRedstoneMode()).withStyle(ChatFormatting.GRAY));
        } else if (exactModeButton != null && exactModeButton.isHovered()) {
            tooltip.add(Component.translatable("misc.fastpipes.exact_mode"));
            tooltip.add(getExactModeText(menu.isExactMode()).withStyle(ChatFormatting.GRAY));
        }

        if (!tooltip.isEmpty()) {
            graphics.renderComponentTooltip(font, tooltip, mouseX - leftPos, mouseY - topPos);
        }

        super.renderLabels(graphics, mouseX, mouseY);
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTicks, int mouseX, int mouseY) {
        int i = (this.width - this.imageWidth) / 2;
        int j = (this.height - this.imageHeight) / 2;
        graphics.blit(RESOURCE, i, j, 0, 0, this.imageWidth, this.imageHeight);

        int x = 43;
        int y = 18;
        for (int filterSlotId = 1; filterSlotId <= InserterAttachment.MAX_FILTER_SLOTS; ++filterSlotId) {
            if (filterSlotId > menu.getInserterAttachmentType().getFilterSlots()) {
                graphics.blit(RESOURCE, i + x, j + y, 198, 0, 18, 18);
            }
            if (filterSlotId % 5 == 0) { x = 43; y += 18; } else { x += 18; }
        }

        super.renderBg(graphics, partialTicks, mouseX, mouseY);
    }
}

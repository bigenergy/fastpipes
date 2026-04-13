package com.piglinmine.fastpipes.screen;

import com.piglinmine.fastpipes.FastPipes;
import com.piglinmine.fastpipes.menu.SensorAttachmentContainerMenu;
import com.piglinmine.fastpipes.network.pipe.attachment.extractor.BlacklistWhitelist;
import com.piglinmine.fastpipes.screen.widget.IconButton;
import com.piglinmine.fastpipes.screen.widget.IconButtonPreset;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class SensorAttachmentScreen extends BaseScreen<SensorAttachmentContainerMenu> {
    // Reuse extractor texture — same layout (filter slots + mode buttons)
    private static final ResourceLocation RESOURCE = new ResourceLocation(FastPipes.MOD_ID, "textures/gui/extractor_attachment.png");

    private final List<Component> tooltip = new ArrayList<>();

    private IconButton blacklistWhitelistButton;
    @Nullable
    private IconButton exactModeButton;

    public SensorAttachmentScreen(SensorAttachmentContainerMenu container, Inventory inv, Component title) {
        super(container, inv, title);
        this.imageWidth = 176;
        this.imageHeight = 193;
    }

    @Override
    protected void init() {
        super.init();
        this.titleLabelX = 7;
        this.inventoryLabelX = 7;

        blacklistWhitelistButton = this.addRenderableWidget(new IconButton(
            this.leftPos + 55, this.topPos + 76,
            IconButtonPreset.NORMAL,
            getBlacklistWhitelistX(menu.getBlacklistWhitelist()), 82,
            getBlacklistWhitelistText(menu.getBlacklistWhitelist()),
            btn -> setBlacklistWhitelist((IconButton) btn, menu.getBlacklistWhitelist().next())
        ));

        exactModeButton = this.addRenderableWidget(new IconButton(
            this.leftPos + 78, this.topPos + 76,
            IconButtonPreset.NORMAL,
            getExactModeX(menu.isExactMode()), 103,
            getExactModeText(menu.isExactMode()),
            btn -> setExactMode((IconButton) btn, !menu.isExactMode())
        ));
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
        this.renderBackground(graphics);
        super.render(graphics, mouseX, mouseY, partialTick);
        this.renderTooltip(graphics, mouseX, mouseY);
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {

        tooltip.clear();

        if (blacklistWhitelistButton.isHovered()) {
            tooltip.add(Component.translatable("misc.fastpipes.mode"));
            tooltip.add(getBlacklistWhitelistText(menu.getBlacklistWhitelist()).withStyle(ChatFormatting.GRAY));
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
    protected void renderBg(@NotNull GuiGraphics graphics, float partialTicks, int mouseX, int mouseY) {
        int i = (this.width - this.imageWidth) / 2;
        int j = (this.height - this.imageHeight) / 2;
        graphics.blit(RESOURCE, i, j, 0, 0, this.imageWidth, this.imageHeight);

        // All 15 filter slots are always available for sensor attachment

        super.renderBg(graphics, partialTicks, mouseX, mouseY);
    }
}

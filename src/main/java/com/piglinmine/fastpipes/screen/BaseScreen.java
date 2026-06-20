package com.piglinmine.fastpipes.screen;

import com.piglinmine.fastpipes.menu.BaseContainerMenu;
import com.piglinmine.fastpipes.menu.slot.FluidFilterSlot;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.block.FluidModel;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.fluids.FluidStack;

import java.util.ArrayList;
import java.util.List;

// MC 26.1.2 / NeoForge 26.1.2.x: renderBg/renderLabels were removed.
// extractBackground / extractLabels are the new hooks (using GuiGraphicsExtractor).
// Fluid slot sprites are drawn via GuiGraphicsExtractor#blitSprite(RenderPipeline, TextureAtlasSprite, x, y, w, h, color).
// The legacy IClientFluidTypeExtensions.getStillTexture(FluidStack)/getTintColor(FluidStack) accessors were
// dropped in NeoForge 26.x — fluid sprites + tint are now sourced from the FluidModel registered for the
// FluidState (same mechanism used by FluidPipeBlockEntityRenderer for world rendering).
public abstract class BaseScreen<T extends BaseContainerMenu> extends AbstractContainerScreen<T> {
    public BaseScreen(T screenContainer, Inventory inv, Component title, int imageWidth, int imageHeight) {
        super(screenContainer, inv, title, imageWidth, imageHeight);
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTicks) {
        super.extractBackground(graphics, mouseX, mouseY, partialTicks);

        for (FluidFilterSlot slot : menu.getFluidSlots()) {
            FluidStack stack = slot.getFluidInventory().getFluid(slot.getSlotIndex());
            if (stack.isEmpty()) continue;
            renderFluidInSlot(graphics, leftPos + slot.x, topPos + slot.y, stack);
        }
    }

    private void renderFluidInSlot(GuiGraphicsExtractor graphics, int x, int y, FluidStack fluid) {
        FluidModel model = Minecraft.getInstance()
            .getModelManager()
            .getFluidStateModelSet()
            .get(fluid.getFluid().defaultFluidState());

        TextureAtlasSprite sprite = model.stillMaterial().sprite();
        if (sprite == null) return;

        // Tint comes from the FluidModel's tint source. We don't have a real BlockState/level
        // context here, so feed AIR — matches the in-pipe rendering path.
        int color;
        if (model.fluidTintSource() != null) {
            color = model.fluidTintSource().color(Blocks.AIR.defaultBlockState());
        } else {
            color = -1; // untinted (white)
        }
        if (((color >> 24) & 0xFF) == 0) {
            color |= 0xFF000000;
        }

        graphics.blitSprite(RenderPipelines.GUI_TEXTURED, sprite, x, y, 16, 16, color);
    }

    @Override
    protected void extractLabels(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
        super.extractLabels(graphics, mouseX, mouseY);

        for (FluidFilterSlot slot : menu.getFluidSlots()) {
            FluidStack stack = slot.getFluidInventory().getFluid(slot.getSlotIndex());
            if (stack.isEmpty()) continue;
            if (!isHovering(slot.x, slot.y, 17, 17, mouseX, mouseY)) continue;

            List<Component> lines = new ArrayList<>(2);
            lines.add(stack.getHoverName());
            if (stack.getAmount() > 0) {
                lines.add(Component.literal(stack.getAmount() + " mB").withStyle(ChatFormatting.GRAY));
            }
            // setComponentTooltipForNextFrame expects SCREEN-absolute coords (matches behaviour
            // documented in ExtractorAttachmentScreen.extractLabels).
            graphics.setComponentTooltipForNextFrame(font, lines, mouseX, mouseY);
        }
    }
}

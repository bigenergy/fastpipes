package com.piglinmine.fastpipes.network.pipe.attachment.inserter;

import com.piglinmine.fastpipes.block.FluidPipeBlock;
import com.piglinmine.fastpipes.block.ItemPipeBlock;
import com.piglinmine.fastpipes.network.pipe.Pipe;
import com.piglinmine.fastpipes.network.pipe.attachment.Attachment;
import com.piglinmine.fastpipes.network.pipe.attachment.AttachmentFactory;
import com.piglinmine.fastpipes.network.pipe.attachment.extractor.BlacklistWhitelist;
import com.piglinmine.fastpipes.network.pipe.attachment.extractor.RedstoneMode;
import com.piglinmine.fastpipes.util.DirectionUtil;
import com.piglinmine.fastpipes.util.StringUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;

import java.util.List;

public class InserterAttachmentFactory implements AttachmentFactory {
    private final InserterAttachmentType type;

    public InserterAttachmentFactory(InserterAttachmentType type) {
        this.type = type;
    }

    @Override
    public Attachment createFromNbt(Pipe pipe, CompoundTag tag) {
        Direction dir = DirectionUtil.safeGet((byte) tag.getInt("dir"));
        InserterAttachment attachment = new InserterAttachment(pipe, dir, type);

        if (tag.contains("rm")) attachment.setRedstoneMode(RedstoneMode.get(tag.getByte("rm")));
        if (tag.contains("bw")) attachment.setBlacklistWhitelist(BlacklistWhitelist.get(tag.getByte("bw")));
        if (tag.contains("exa")) attachment.setExactMode(tag.getBoolean("exa"));
        if (tag.contains("itemfilter")) {
            attachment.getItemFilter().deserializeNBT(pipe.getLevel().registryAccess(), tag.getCompound("itemfilter"));
        }
        if (tag.contains("fluidfilter")) {
            attachment.getFluidFilter().readFromNbt(tag.getCompound("fluidfilter"), pipe.getLevel().registryAccess());
        }

        return attachment;
    }

    @Override
    public Attachment create(Pipe pipe, Direction dir) {
        return new InserterAttachment(pipe, dir, type);
    }

    @Override
    public ResourceLocation getItemId() { return type.getItemId(); }

    @Override
    public ResourceLocation getId() { return type.getId(); }

    @Override
    public ResourceLocation getModelLocation() { return type.getModelLocation(); }

    @Override
    public void addInformation(List<Component> tooltip) {
        tooltip.add(Component.translatable("tooltip.fastpipes.inserter_attachment.hint").withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC));
        tooltip.add(Component.translatable("misc.fastpipes.tier", Component.translatable("enchantment.level." + type.getTier())).withStyle(ChatFormatting.YELLOW));

        tooltip.add(Component.translatable(
            "tooltip.fastpipes.inserter_attachment.priority",
            Component.literal(StringUtil.formatNumber(type.getPriority())).withStyle(ChatFormatting.WHITE)
        ).withStyle(ChatFormatting.GRAY));

        tooltip.add(Component.translatable(
            "tooltip.fastpipes.inserter_attachment.filter_slots",
            Component.literal("" + type.getFilterSlots()).withStyle(ChatFormatting.WHITE)
        ).withStyle(ChatFormatting.GRAY));

        addAbilityToInformation(tooltip, type.getCanSetRedstoneMode(), "misc.fastpipes.redstone_mode");
        addAbilityToInformation(tooltip, type.getCanSetWhitelistBlacklist(), "misc.fastpipes.mode");
        addAbilityToInformation(tooltip, type.getCanSetExactMode(), "misc.fastpipes.exact_mode");

        tooltip.add(Component.empty());
        tooltip.add(Component.translatable("tooltip.fastpipes.attachment.place").withStyle(ChatFormatting.DARK_GRAY));
        tooltip.add(Component.translatable("tooltip.fastpipes.attachment.remove").withStyle(ChatFormatting.DARK_GRAY));
    }

    private void addAbilityToInformation(List<Component> tooltip, boolean possible, String key) {
        tooltip.add(
            Component.literal(possible ? "✓ " : "❌ ").append(Component.translatable(key))
                .withStyle(possible ? ChatFormatting.GREEN : ChatFormatting.RED)
        );
    }

    @Override
    public boolean canPlaceOnPipe(Block pipe) {
        return pipe instanceof ItemPipeBlock
            || pipe instanceof FluidPipeBlock;
    }
}

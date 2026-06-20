package com.piglinmine.fastpipes.network.pipe.attachment.extractor;

import com.piglinmine.fastpipes.block.FluidPipeBlock;
import com.piglinmine.fastpipes.block.ItemPipeBlock;
import com.piglinmine.fastpipes.network.pipe.Pipe;
import com.piglinmine.fastpipes.network.pipe.attachment.Attachment;
import com.piglinmine.fastpipes.network.pipe.attachment.AttachmentFactory;
import com.piglinmine.fastpipes.util.DirectionUtil;
import com.piglinmine.fastpipes.util.StringUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.Block;

import java.util.List;

public class ExtractorAttachmentFactory implements AttachmentFactory {
    private final ExtractorAttachmentType type;

    public ExtractorAttachmentFactory(ExtractorAttachmentType type) {
        this.type = type;
    }

    @Override
    public Attachment createFromNbt(Pipe pipe, CompoundTag tag) {
        Direction dir = DirectionUtil.safeGet((byte) tag.getIntOr("dir", 0));

        ExtractorAttachment attachment = new ExtractorAttachment(pipe, dir, type);

        if (tag.contains("itemfilter")) {
            com.piglinmine.fastpipes.util.ItemStackSerialization.loadItemStackHandler(
                pipe.getLevel().registryAccess(),
                attachment.getItemFilter(),
                tag.getCompoundOrEmpty("itemfilter"));
        }

        if (tag.contains("rm")) {
            attachment.setRedstoneMode(RedstoneMode.get(tag.getByteOr("rm", (byte) 0)));
        }

        if (tag.contains("bw")) {
            attachment.setBlacklistWhitelist(BlacklistWhitelist.get(tag.getByteOr("bw", (byte) 0)));
        }

        if (tag.contains("rr")) {
            attachment.setRoundRobinIndex(tag.getIntOr("rr", 0));
        }

        if (tag.contains("routingm")) {
            attachment.setRoutingMode(RoutingMode.get(tag.getByteOr("routingm", (byte) 0)));
        }

        if (tag.contains("stacksi")) {
            attachment.setStackSize(tag.getIntOr("stacksi", 0));
        }

        if (tag.contains("exa")) {
            attachment.setExactMode(tag.getBooleanOr("exa", false));
        }

        if (tag.contains("fluidfilter")) {
            attachment.getFluidFilter().readFromNbt(tag.getCompoundOrEmpty("fluidfilter"), pipe.getLevel().registryAccess());
        }

        if (tag.contains("tagov")) {
            CompoundTag overrides = tag.getCompoundOrEmpty("tagov");
            for (int i = 0; i < ExtractorAttachment.MAX_FILTER_SLOTS; i++) {
                String key = "s" + i;
                if (overrides.contains(key)) {
                    // TODO 1.21.11: CompoundTag.getString now returns Optional<String>
                    attachment.setTagOverride(i, overrides.getString(key).orElse(""));
                }
            }
        }

        return attachment;
    }

    @Override
    public Attachment create(Pipe pipe, Direction dir) {
        return new ExtractorAttachment(pipe, dir, type);
    }

    @Override
    public Identifier getItemId() {
        return type.getItemId();
    }

    @Override
    public Identifier getId() {
        return type.getId();
    }

    @Override
    public Identifier getModelLocation() {
        return type.getModelLocation();
    }

    @Override
    public void addInformation(List<Component> tooltip) {
        tooltip.add(Component.translatable("tooltip.fastpipes.extractor_attachment.hint").withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC));
        tooltip.add(Component.translatable("misc.fastpipes.tier", Component.translatable("enchantment.level." + type.getTier())).withStyle(ChatFormatting.YELLOW));

        Component itemsToExtract = Component.literal(StringUtil.formatNumber(type.getItemsToExtract()) + " ")
            .append(Component.translatable("misc.fastpipes.item" + (type.getItemsToExtract() == 1 ? "" : "s")))
            .withStyle(ChatFormatting.WHITE);

        float itemSecondsInterval = type.getItemTickInterval() / 20F;
        Component itemTickInterval = Component.literal(StringUtil.formatNumber(itemSecondsInterval) + " ")
            .append(Component.translatable("misc.fastpipes.second" + (itemSecondsInterval == 1 ? "" : "s")))
            .withStyle(ChatFormatting.WHITE);

        tooltip.add(Component.translatable(
            "tooltip.fastpipes.extractor_attachment.item_extraction_rate",
            itemsToExtract,
            itemTickInterval
        ).withStyle(ChatFormatting.GRAY));

        Component fluidsToExtract = Component.literal(StringUtil.formatNumber(type.getFluidsToExtract()) + " mB")
            .withStyle(ChatFormatting.WHITE);

        float fluidSecondsInterval = type.getFluidTickInterval() / 20F;
        Component fluidTickInterval = Component.literal(StringUtil.formatNumber(fluidSecondsInterval) + " ")
            .append(Component.translatable("misc.fastpipes.second" + (fluidSecondsInterval == 1 ? "" : "s")))
            .withStyle(ChatFormatting.WHITE);

        tooltip.add(Component.translatable(
            "tooltip.fastpipes.extractor_attachment.fluid_extraction_rate",
            fluidsToExtract,
            fluidTickInterval
        ).withStyle(ChatFormatting.GRAY));

        tooltip.add(Component.translatable(
            "tooltip.fastpipes.extractor_attachment.filter_slots",
            Component.literal("" + type.getFilterSlots()).withStyle(ChatFormatting.WHITE)
        ).withStyle(ChatFormatting.GRAY));

        addAbilityToInformation(tooltip, type.getCanSetRedstoneMode(), "misc.fastpipes.redstone_mode");
        addAbilityToInformation(tooltip, type.getCanSetWhitelistBlacklist(), "misc.fastpipes.mode");
        addAbilityToInformation(tooltip, type.getCanSetRoutingMode(), "misc.fastpipes.routing_mode");
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
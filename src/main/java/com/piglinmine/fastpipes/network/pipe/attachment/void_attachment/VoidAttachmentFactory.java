package com.piglinmine.fastpipes.network.pipe.attachment.void_attachment;

import com.piglinmine.fastpipes.block.FluidPipeBlock;
import com.piglinmine.fastpipes.block.ItemPipeBlock;
import com.piglinmine.fastpipes.network.pipe.Pipe;
import com.piglinmine.fastpipes.network.pipe.attachment.Attachment;
import com.piglinmine.fastpipes.network.pipe.attachment.AttachmentFactory;
import com.piglinmine.fastpipes.network.pipe.attachment.extractor.BlacklistWhitelist;
import com.piglinmine.fastpipes.util.DirectionUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;

import java.util.List;

public class VoidAttachmentFactory implements AttachmentFactory {
    private final VoidAttachmentType type;

    public VoidAttachmentFactory(VoidAttachmentType type) {
        this.type = type;
    }

    @Override
    public Attachment createFromNbt(Pipe pipe, CompoundTag tag) {
        Direction dir = DirectionUtil.safeGet((byte) tag.getInt("dir"));
        VoidAttachment attachment = new VoidAttachment(pipe, dir, type);

        if (tag.contains("bw")) {
            attachment.setBlacklistWhitelist(BlacklistWhitelist.get(tag.getByte("bw")));
        }
        if (tag.contains("exa")) {
            attachment.setExactMode(tag.getBoolean("exa"));
        }
        if (tag.contains("itemfilter")) {
            attachment.getItemFilter().deserializeNBT(tag.getCompound("itemfilter"));
        }
        if (tag.contains("fluidfilter")) {
            attachment.getFluidFilter().readFromNbt(tag.getCompound("fluidfilter"));
        }

        return attachment;
    }

    @Override
    public Attachment create(Pipe pipe, Direction dir) {
        return new VoidAttachment(pipe, dir, type);
    }

    @Override
    public ResourceLocation getItemId() { return type.getItemId(); }

    @Override
    public ResourceLocation getId() { return type.getId(); }

    @Override
    public ResourceLocation getModelLocation() { return type.getModelLocation(); }

    @Override
    public void addInformation(List<Component> tooltip) {
        tooltip.add(Component.translatable("tooltip.fastpipes.void_attachment.hint").withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC));
        tooltip.add(Component.translatable("tooltip.fastpipes.void_attachment.priority").withStyle(ChatFormatting.DARK_RED));

        tooltip.add(Component.empty());
        tooltip.add(Component.translatable("tooltip.fastpipes.attachment.place").withStyle(ChatFormatting.DARK_GRAY));
        tooltip.add(Component.translatable("tooltip.fastpipes.attachment.remove").withStyle(ChatFormatting.DARK_GRAY));
    }

    @Override
    public boolean canPlaceOnPipe(Block pipe) {
        return pipe instanceof ItemPipeBlock
            || pipe instanceof FluidPipeBlock;
    }
}

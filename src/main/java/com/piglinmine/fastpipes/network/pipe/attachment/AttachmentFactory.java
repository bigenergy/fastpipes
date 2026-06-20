package com.piglinmine.fastpipes.network.pipe.attachment;

import com.piglinmine.fastpipes.network.pipe.Pipe;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.Block;

import java.util.List;

public interface AttachmentFactory {
    Attachment createFromNbt(Pipe pipe, CompoundTag tag);

    Attachment create(Pipe pipe, Direction dir);

    Identifier getItemId();

    Identifier getId();

    Identifier getModelLocation();

    void addInformation(List<Component> tooltip);

    boolean canPlaceOnPipe(Block pipe);
} 
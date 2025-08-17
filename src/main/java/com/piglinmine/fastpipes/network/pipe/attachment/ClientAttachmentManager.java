package com.piglinmine.fastpipes.network.pipe.attachment;

import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class ClientAttachmentManager implements AttachmentManager {
    private final ResourceLocation[] attachmentState = new ResourceLocation[Direction.values().length];
    private final ItemStack[] pickBlocks = new ItemStack[Direction.values().length];

    public ClientAttachmentManager() {
        for (int i = 0; i < pickBlocks.length; i++) {
            pickBlocks[i] = ItemStack.EMPTY;
        }
    }

    @Override
    public ResourceLocation[] getState() {
        return attachmentState;
    }

    @Override
    public boolean hasAttachment(Direction dir) {
        return attachmentState[dir.ordinal()] != null;
    }

    @Override
    public void openAttachmentContainer(Direction dir, ServerPlayer player) {
        throw new RuntimeException("Shouldn't be called on the client");
    }

    @Nonnull
    @Override
    public ItemStack getPickBlock(Direction dir) {
        return pickBlocks[dir.ordinal()];
    }

    @Override
    @Nullable
    public Attachment getAttachment(Direction dir) {
        return null; // Client doesn't have actual attachment instances
    }

    @Override
    public void writeUpdate(CompoundTag tag) {
        throw new RuntimeException("Shouldn't be called on the client");
    }

    @Override
    public void readUpdate(@Nullable CompoundTag tag) {
        if (tag == null) {
            // Clear all attachments
            for (int i = 0; i < attachmentState.length; i++) {
                attachmentState[i] = null;
                pickBlocks[i] = ItemStack.EMPTY;
            }
            return;
        }

        for (Direction dir : Direction.values()) {
            String attachmentKey = "attch_" + dir.ordinal();
            String pickBlockKey = "pb_" + dir.ordinal();

            if (tag.contains(attachmentKey)) {
                attachmentState[dir.ordinal()] = ResourceLocation.parse(tag.getString(attachmentKey));
                
                if (tag.contains(pickBlockKey)) {
                    // TODO: Use proper HolderLookup.Provider when available
                    // pickBlocks[dir.ordinal()] = ItemStack.parseOptional(registries, tag.getCompound(pickBlockKey));
                    pickBlocks[dir.ordinal()] = ItemStack.EMPTY; // Placeholder
                }
            } else {
                attachmentState[dir.ordinal()] = null;
                pickBlocks[dir.ordinal()] = ItemStack.EMPTY;
            }
        }
    }
} 
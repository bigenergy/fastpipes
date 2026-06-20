package com.piglinmine.fastpipes.network.pipe.attachment;

import net.minecraft.client.Minecraft;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class ClientAttachmentManager implements AttachmentManager {
    private final Identifier[] attachmentState = new Identifier[Direction.values().length];
    private final ItemStack[] pickBlocks = new ItemStack[Direction.values().length];

    public ClientAttachmentManager() {
        for (int i = 0; i < pickBlocks.length; i++) {
            pickBlocks[i] = ItemStack.EMPTY;
        }
    }

    @Override
    public Identifier[] getState() {
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
        // No-op on client. NeoForge 1.21.11's BlockSnapshot.create() (invoked when the
        // client predicts a block break) calls saveWithFullMetadata which routes through
        // this path. We can't throw — that crashes the game. Attachment state on the
        // client is sync-only (it gets pushed from server via readUpdate), so there's
        // nothing to write here.
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
                // TODO 1.21.11: CompoundTag.getString now returns Optional<String>
                attachmentState[dir.ordinal()] = Identifier.parse(tag.getString(attachmentKey).orElse(""));

                if (tag.contains(pickBlockKey)) {
                    // TODO 1.21.11: ItemStack.parseOptional was removed; using OPTIONAL_CODEC via helper
                    pickBlocks[dir.ordinal()] = com.piglinmine.fastpipes.util.ItemStackSerialization.parseOptional(
                        Minecraft.getInstance().level.registryAccess(),
                        tag.getCompoundOrEmpty(pickBlockKey)
                    );
                }
            } else {
                attachmentState[dir.ordinal()] = null;
                pickBlocks[dir.ordinal()] = ItemStack.EMPTY;
            }
        }
    }
} 
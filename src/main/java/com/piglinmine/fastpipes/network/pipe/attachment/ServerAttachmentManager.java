package com.piglinmine.fastpipes.network.pipe.attachment;

import com.mojang.serialization.DataResult;
import com.piglinmine.fastpipes.network.pipe.Pipe;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class ServerAttachmentManager implements AttachmentManager {
    private static final Logger LOGGER = LogManager.getLogger(ServerAttachmentManager.class);

    private final Map<Direction, Attachment> attachments = new HashMap<>();
    private final Identifier[] attachmentState = new Identifier[Direction.values().length];

    private final Pipe pipe;

    public ServerAttachmentManager(Pipe pipe) {
        this.pipe = pipe;
    }

    @Override
    public boolean hasAttachment(Direction dir) {
        return attachments.containsKey(dir);
    }

    @Override
    public void openAttachmentContainer(Direction dir, ServerPlayer player) {
        if (hasAttachment(dir)) {
            getAttachment(dir).openContainer(player);
        }
    }

    @Nonnull
    @Override
    public ItemStack getPickBlock(Direction dir) {
        throw new RuntimeException("Shouldn't be called on the server");
    }

    public void removeAttachmentAndScanGraph(Direction dir) {
        attachments.remove(dir);
        attachmentState[dir.ordinal()] = null;

        // Re-scan graph if the pipe has a network. A pipe may be temporarily networkless
        // (e.g. just placed, or in a transient rescan state for some mod-injected world);
        // skipping the scan is safe — the next tick / neighbor change will rescan.
        // Without this guard, the packet handler aborts mid-operation, leaving the client
        // out of sync (duplicate attachments, broken rendering).
        if (pipe.getNetwork() != null) {
            pipe.getNetwork().scanGraph(pipe.getLevel(), pipe.getPos());
        }
    }

    public void setAttachmentAndScanGraph(Direction dir, Attachment attachment) {
        setAttachment(dir, attachment);

        // Same guard as removeAttachmentAndScanGraph — see comment there.
        if (pipe.getNetwork() != null) {
            pipe.getNetwork().scanGraph(pipe.getLevel(), pipe.getPos());
        }
    }

    private void setAttachment(Direction dir, Attachment attachment) {
        attachments.put(dir, attachment);
        attachmentState[dir.ordinal()] = attachment.getId();
    }

    @Override
    @Nullable
    public Attachment getAttachment(Direction dir) {
        return attachments.get(dir);
    }

    public Collection<Attachment> getAttachments() {
        return attachments.values();
    }

    public CompoundTag writeToNbt(CompoundTag tag) {
        ListTag attch = new ListTag();
        getAttachments().forEach(a -> {
            CompoundTag attchTag = new CompoundTag();
            attchTag.putString("typ", a.getId().toString());
            attch.add(a.writeToNbt(attchTag));
        });
        tag.put("attch", attch);
        return tag;
    }

    public void readFromNbt(CompoundTag tag) {
        ListTag attch = tag.getListOrEmpty("attch");
        for (Tag item : attch) {
            if (!(item instanceof CompoundTag attchTag)) continue;

            String typ = attchTag.getStringOr("typ", "");
            AttachmentFactory factory = AttachmentRegistry.INSTANCE.getFactory(Identifier.parse(typ));
            if (factory != null) {
                Attachment attachment = factory.createFromNbt(pipe, attchTag);
                setAttachment(attachment.getDirection(), attachment);
            } else {
                LOGGER.warn("Attachment {} no longer exists", typ);
            }
        }
    }

    @Override
    public Identifier[] getState() {
        return attachmentState;
    }

    @Override
    public void writeUpdate(CompoundTag tag) {
        for (Direction dir : Direction.values()) {
            if (hasAttachment(dir)) {
                tag.putString("attch_" + dir.ordinal(), getAttachment(dir).getId().toString());
                ItemStack drop = getAttachment(dir).getDrop();
                // TODO 1.21.11: ItemStack.saveOptional was removed; use OPTIONAL_CODEC directly.
                // Use the registry-aware NbtOps so component-bearing items round-trip cleanly.
                var ops = pipe.getLevel().registryAccess().createSerializationContext(NbtOps.INSTANCE);
                DataResult<Tag> encoded = ItemStack.OPTIONAL_CODEC.encodeStart(ops, drop);
                Tag encodedTag = encoded.result().orElse(null);
                if (encodedTag != null) {
                    tag.put("pb_" + dir.ordinal(), encodedTag);
                }
            }
        }
    }

    @Override
    public void readUpdate(@Nullable CompoundTag tag) {
        throw new RuntimeException("Client-side only");
    }
} 
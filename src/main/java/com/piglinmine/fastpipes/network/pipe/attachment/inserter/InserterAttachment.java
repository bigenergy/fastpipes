package com.piglinmine.fastpipes.network.pipe.attachment.inserter;

import com.piglinmine.fastpipes.menu.InserterAttachmentMenuProvider;
import com.piglinmine.fastpipes.network.NetworkManager;
import com.piglinmine.fastpipes.network.pipe.Pipe;
import com.piglinmine.fastpipes.network.pipe.attachment.Attachment;
import com.piglinmine.fastpipes.network.pipe.attachment.extractor.BlacklistWhitelist;
import com.piglinmine.fastpipes.network.pipe.attachment.extractor.RedstoneMode;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.ItemStackHandler;

import javax.annotation.Nullable;

public class InserterAttachment extends Attachment {
    public static final int MAX_FILTER_SLOTS = 15;

    private final InserterAttachmentType type;
    private final ItemStackHandler itemFilter;

    private RedstoneMode redstoneMode = RedstoneMode.IGNORED;
    private BlacklistWhitelist blacklistWhitelist = BlacklistWhitelist.BLACKLIST;
    private boolean exactMode = false;

    public InserterAttachment(Pipe pipe, Direction direction, InserterAttachmentType type) {
        super(pipe, direction);
        this.type = type;
        this.itemFilter = createItemFilterInventory(this);
    }

    public static ItemStackHandler createItemFilterInventory(@Nullable InserterAttachment attachment) {
        return new ItemStackHandler(MAX_FILTER_SLOTS) {
            @Override
            protected void onContentsChanged(int slot) {
                super.onContentsChanged(slot);
                if (attachment != null) {
                    NetworkManager.get(attachment.pipe.getLevel()).setDirty();
                }
            }
        };
    }

    @Override
    public void update() {
        // Inserter is passive — it marks its side as a destination and filters items routed here.
    }

    @Override
    public boolean isItemDestinationProvider() {
        return true;
    }

    @Override
    public boolean canInsert(ItemStack stack) {
        if (!redstoneMode.isEnabled(pipe.getLevel(), pipe.getPos())) {
            return false;
        }

        // Check if any filter slots are filled
        boolean hasFilter = false;
        for (int i = 0; i < itemFilter.getSlots(); ++i) {
            if (!itemFilter.getStackInSlot(i).isEmpty()) {
                hasFilter = true;
                break;
            }
        }
        if (!hasFilter) {
            return true; // no filter = accept everything
        }

        if (blacklistWhitelist == BlacklistWhitelist.WHITELIST) {
            for (int i = 0; i < itemFilter.getSlots(); ++i) {
                ItemStack filtered = itemFilter.getStackInSlot(i);
                if (filtered.isEmpty()) continue;
                boolean equals = filtered.is(stack.getItem());
                if (exactMode) equals = equals && ItemStack.isSameItemSameComponents(filtered, stack);
                if (equals) return true;
            }
            return false;
        } else {
            for (int i = 0; i < itemFilter.getSlots(); ++i) {
                ItemStack filtered = itemFilter.getStackInSlot(i);
                if (filtered.isEmpty()) continue;
                boolean equals = filtered.is(stack.getItem());
                if (exactMode) equals = equals && ItemStack.isSameItemSameComponents(filtered, stack);
                if (equals) return false;
            }
            return true;
        }
    }

    @Override
    public int getInsertionPriority() {
        return type.getPriority();
    }

    @Override
    public ResourceLocation getId() {
        return type.getId();
    }

    @Override
    public ItemStack getDrop() {
        return new ItemStack(type.getItem());
    }

    @Override
    public void openContainer(ServerPlayer player) {
        super.openContainer(player);
        InserterAttachmentMenuProvider.open(pipe, this, player);
    }

    @Override
    public CompoundTag writeToNbt(CompoundTag tag) {
        tag.putByte("rm", (byte) redstoneMode.ordinal());
        tag.putByte("bw", (byte) blacklistWhitelist.ordinal());
        tag.putBoolean("exa", exactMode);
        tag.put("itemfilter", itemFilter.serializeNBT(pipe.getLevel().registryAccess()));
        return super.writeToNbt(tag);
    }

    public InserterAttachmentType getType() { return type; }
    public ItemStackHandler getItemFilter() { return itemFilter; }

    public RedstoneMode getRedstoneMode() { return redstoneMode; }
    public void setRedstoneMode(RedstoneMode mode) {
        if (!type.getCanSetRedstoneMode()) return;
        this.redstoneMode = mode;
    }

    public BlacklistWhitelist getBlacklistWhitelist() { return blacklistWhitelist; }
    public void setBlacklistWhitelist(BlacklistWhitelist bw) {
        if (!type.getCanSetWhitelistBlacklist()) return;
        this.blacklistWhitelist = bw;
    }

    public boolean isExactMode() { return exactMode; }
    public void setExactMode(boolean exactMode) {
        if (!type.getCanSetExactMode()) return;
        this.exactMode = exactMode;
    }
}

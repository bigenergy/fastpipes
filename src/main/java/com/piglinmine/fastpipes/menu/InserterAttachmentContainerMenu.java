package com.piglinmine.fastpipes.menu;

import com.piglinmine.fastpipes.FPipesContainerMenus;
import com.piglinmine.fastpipes.menu.slot.FilterSlot;
import com.piglinmine.fastpipes.network.FastPipesNetwork;
import com.piglinmine.fastpipes.network.message.*;
import com.piglinmine.fastpipes.network.pipe.attachment.extractor.BlacklistWhitelist;
import com.piglinmine.fastpipes.network.pipe.attachment.extractor.RedstoneMode;
import com.piglinmine.fastpipes.network.pipe.attachment.inserter.InserterAttachment;
import com.piglinmine.fastpipes.network.pipe.attachment.inserter.InserterAttachmentType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.ItemStackHandler;
import net.neoforged.neoforge.items.SlotItemHandler;

public class InserterAttachmentContainerMenu extends BaseContainerMenu {
    private final BlockPos pos;
    private final Direction dir;
    private final InserterAttachmentType inserterAttachmentType;

    private RedstoneMode redstoneMode;
    private BlacklistWhitelist blacklistWhitelist;
    private boolean exactMode;

    public InserterAttachmentContainerMenu(
        int windowId,
        Player player,
        BlockPos pos,
        Direction dir,
        RedstoneMode redstoneMode,
        BlacklistWhitelist blacklistWhitelist,
        boolean exactMode,
        InserterAttachmentType type,
        ItemStackHandler itemFilter) {
        super(FPipesContainerMenus.INSERTER_ATTACHMENT.get(), windowId, player);

        addPlayerInventory(8, 111);

        int x = 44;
        int y = 19;
        for (int i = 1; i <= type.getFilterSlots(); ++i) {
            addSlot(new FilterSlot(itemFilter, i - 1, x, y));

            if (i % 5 == 0) {
                x = 44;
                y += 18;
            } else {
                x += 18;
            }
        }

        this.pos = pos;
        this.dir = dir;
        this.inserterAttachmentType = type;
        this.redstoneMode = redstoneMode;
        this.blacklistWhitelist = blacklistWhitelist;
        this.exactMode = exactMode;
    }

    public InserterAttachmentContainerMenu(int windowId, Player player) {
        this(windowId, player, BlockPos.ZERO, Direction.NORTH,
            RedstoneMode.IGNORED, BlacklistWhitelist.BLACKLIST, false,
            InserterAttachmentType.BASIC, new ItemStackHandler(15));
    }

    public BlockPos getPos() { return pos; }
    public Direction getDirection() { return dir; }
    public InserterAttachmentType getInserterAttachmentType() { return inserterAttachmentType; }

    public RedstoneMode getRedstoneMode() { return redstoneMode; }
    public void setRedstoneMode(RedstoneMode redstoneMode) {
        this.redstoneMode = redstoneMode;
        FastPipesNetwork.sendToServer(new ChangeRedstoneModeMessage(pos, dir, redstoneMode.ordinal()));
    }

    public BlacklistWhitelist getBlacklistWhitelist() { return blacklistWhitelist; }
    public void setBlacklistWhitelist(BlacklistWhitelist blacklistWhitelist) {
        this.blacklistWhitelist = blacklistWhitelist;
        FastPipesNetwork.sendToServer(new ChangeBlacklistWhitelistMessage(pos, dir, blacklistWhitelist == BlacklistWhitelist.BLACKLIST));
    }

    public boolean isExactMode() { return exactMode; }
    public void setExactMode(boolean exactMode) {
        this.exactMode = exactMode;
        FastPipesNetwork.sendToServer(new ChangeExactModeMessage(pos, dir, exactMode));
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        Slot slot = slots.get(index);
        if (slot != null && slot.hasItem() && index < 9 * 4) {
            for (int i = 9 * 4; i < slots.size(); ++i) {
                Slot filterSlot = slots.get(i);
                if (filterSlot instanceof SlotItemHandler itemSlot && !itemSlot.hasItem()) {
                    ItemStack toInsert = slot.getItem().copy();
                    toInsert.setCount(1);

                    boolean foundExistingItem = false;
                    for (int j = 0; j < itemSlot.getItemHandler().getSlots(); ++j) {
                        if (ItemStack.matches(itemSlot.getItemHandler().getStackInSlot(j), toInsert)) {
                            foundExistingItem = true;
                            break;
                        }
                    }
                    if (!foundExistingItem) {
                        itemSlot.set(toInsert);
                    }
                    break;
                }
            }
        }
        return ItemStack.EMPTY;
    }
}

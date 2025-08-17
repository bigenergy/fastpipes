package com.piglinmine.fastpipes.menu;

import com.piglinmine.fastpipes.FPipesContainerMenus;
import com.piglinmine.fastpipes.inventory.fluid.FluidInventory;
import com.piglinmine.fastpipes.menu.slot.FilterSlot;
import com.piglinmine.fastpipes.menu.slot.FluidFilterSlot;
import com.piglinmine.fastpipes.network.FastPipesNetwork;
import com.piglinmine.fastpipes.network.message.*;
import com.piglinmine.fastpipes.network.pipe.attachment.extractor.BlacklistWhitelist;
import com.piglinmine.fastpipes.network.pipe.attachment.extractor.ExtractorAttachmentType;
import com.piglinmine.fastpipes.network.pipe.attachment.extractor.RedstoneMode;
import com.piglinmine.fastpipes.network.pipe.attachment.extractor.RoutingMode;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.ItemStackHandler;
import net.neoforged.neoforge.items.SlotItemHandler;

public class ExtractorAttachmentContainerMenu extends BaseContainerMenu {
    private final BlockPos pos;
    private final Direction dir;
    private final ExtractorAttachmentType extractorAttachmentType;
    private final boolean fluidMode;

    private RedstoneMode redstoneMode = RedstoneMode.IGNORED;
    private BlacklistWhitelist blacklistWhitelist = BlacklistWhitelist.BLACKLIST;
    private RoutingMode routingMode = RoutingMode.NEAREST;
    private int stackSize;
    private boolean exactMode;

    public ExtractorAttachmentContainerMenu(
        int windowId,
        Player player,
        BlockPos pos,
        Direction dir,
        RedstoneMode redstoneMode,
        BlacklistWhitelist blacklistWhitelist,
        RoutingMode routingMode,
        int stackSize,
        boolean exactMode,
        ExtractorAttachmentType type,
        ItemStackHandler itemFilter,
        FluidInventory fluidFilter,
        boolean fluidMode) {
        super(FPipesContainerMenus.EXTRACTOR_ATTACHMENT.get(), windowId, player);

        addPlayerInventory(8, 111);

        int x = 44;
        int y = 19;
        for (int i = 1; i <= type.getFilterSlots(); ++i) {
            if (fluidMode) {
                addSlot(new FluidFilterSlot(fluidFilter, i - 1, x, y));
            } else {
                addSlot(new FilterSlot(itemFilter, i - 1, x, y));
            }

            if (i % 5 == 0) {
                x = 44;
                y += 18;
            } else {
                x += 18;
            }
        }

        this.pos = pos;
        this.dir = dir;
        this.extractorAttachmentType = type;
        this.fluidMode = fluidMode;

        this.redstoneMode = redstoneMode;
        this.blacklistWhitelist = blacklistWhitelist;
        this.routingMode = routingMode;
        this.stackSize = stackSize;
        this.exactMode = exactMode;
    }

    // Client constructor for NeoForge
    public ExtractorAttachmentContainerMenu(int windowId, Player player) {
        this(windowId, player, BlockPos.ZERO, Direction.NORTH, 
             RedstoneMode.IGNORED, BlacklistWhitelist.BLACKLIST, RoutingMode.NEAREST,
             ExtractorAttachmentType.BASIC.getItemsToExtract(), false, ExtractorAttachmentType.BASIC,
             new ItemStackHandler(15), new FluidInventory(15), false);
    }

    public BlockPos getPos() {
        return pos;
    }

    public Direction getDirection() {
        return dir;
    }

    public boolean isFluidMode() {
        return fluidMode;
    }

    public ExtractorAttachmentType getExtractorAttachmentType() {
        return extractorAttachmentType;
    }

    public RedstoneMode getRedstoneMode() {
        return redstoneMode;
    }

    public void setRedstoneMode(RedstoneMode redstoneMode) {
        this.redstoneMode = redstoneMode;
        FastPipesNetwork.sendToServer(new ChangeRedstoneModeMessage(pos, dir, redstoneMode.ordinal()));
    }

    public BlacklistWhitelist getBlacklistWhitelist() {
        return blacklistWhitelist;
    }

    public void setBlacklistWhitelist(BlacklistWhitelist blacklistWhitelist) {
        this.blacklistWhitelist = blacklistWhitelist;
        FastPipesNetwork.sendToServer(new ChangeBlacklistWhitelistMessage(pos, dir, blacklistWhitelist == BlacklistWhitelist.BLACKLIST));
    }

    public RoutingMode getRoutingMode() {
        return routingMode;
    }

    public void setRoutingMode(RoutingMode routingMode) {
        this.routingMode = routingMode;
        FastPipesNetwork.sendToServer(new ChangeRoutingModeMessage(pos, dir, routingMode.ordinal()));
    }

    public int getStackSize() {
        return stackSize;
    }

    public void setStackSize(int stackSize) {
        this.stackSize = stackSize;
        FastPipesNetwork.sendToServer(new ChangeStackSizeMessage(pos, dir, stackSize));
    }

    public boolean isExactMode() {
        return exactMode;
    }

    public void setExactMode(boolean exactMode) {
        this.exactMode = exactMode;
        FastPipesNetwork.sendToServer(new ChangeExactModeMessage(pos, dir, exactMode));
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        Slot slot = slots.get(index);
        if (slot != null && slot.hasItem() && index < 9 * 4) {
            // Try to move item to filter slots
            for (int i = 9 * 4; i < slots.size(); ++i) {
                Slot filterSlot = slots.get(i);

                if (filterSlot instanceof FluidFilterSlot) {
                    FluidFilterSlot fluidSlot = (FluidFilterSlot) filterSlot;

                    if (fluidSlot.getFluidInventory().getFluid(fluidSlot.getSlotIndex()).isEmpty()) {
                        // TODO: Implement fluid extraction when FluidUtil is available
                        // FluidStack fluidStack = FluidUtil.getFluidContained(slot.getItem()).orElse(FluidStack.EMPTY);
                        // if (!fluidStack.isEmpty()) {
                        //     fluidSlot.getFluidInventory().setFluid(fluidSlot.getSlotIndex(), fluidStack);
                        //     break;
                        // }
                        break;
                    }
                } else if (filterSlot instanceof SlotItemHandler) {
                    SlotItemHandler itemSlot = (SlotItemHandler) filterSlot;

                    if (!itemSlot.hasItem()) {
                        ItemStack toInsert = slot.getItem().copy();
                        toInsert.setCount(1);

                        boolean foundExistingItem = false;

                        // Check if this item is already in the filter
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
        }

        return ItemStack.EMPTY;
    }
} 
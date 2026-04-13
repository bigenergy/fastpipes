package com.piglinmine.fastpipes.menu;

import com.piglinmine.fastpipes.FPipesContainerMenus;
import com.piglinmine.fastpipes.inventory.fluid.FluidInventory;
import com.piglinmine.fastpipes.menu.slot.FilterSlot;
import com.piglinmine.fastpipes.menu.slot.FluidFilterSlot;
import com.piglinmine.fastpipes.network.FastPipesNetwork;
import com.piglinmine.fastpipes.network.message.ChangeBlacklistWhitelistMessage;
import com.piglinmine.fastpipes.network.message.ChangeExactModeMessage;
import com.piglinmine.fastpipes.network.pipe.attachment.extractor.BlacklistWhitelist;
import com.piglinmine.fastpipes.network.pipe.attachment.sensor.SensorAttachment;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.items.SlotItemHandler;

public class SensorAttachmentContainerMenu extends BaseContainerMenu {
    private final BlockPos pos;
    private final Direction dir;
    private final boolean fluidMode;

    private BlacklistWhitelist blacklistWhitelist;
    private boolean exactMode;

    public SensorAttachmentContainerMenu(
        int windowId,
        Player player,
        BlockPos pos,
        Direction dir,
        BlacklistWhitelist blacklistWhitelist,
        boolean exactMode,
        ItemStackHandler itemFilter,
        FluidInventory fluidFilter,
        boolean fluidMode) {
        super(FPipesContainerMenus.SENSOR_ATTACHMENT.get(), windowId, player);

        addPlayerInventory(8, 111);

        int x = 44;
        int y = 19;
        for (int i = 1; i <= SensorAttachment.MAX_FILTER_SLOTS; ++i) {
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
        this.fluidMode = fluidMode;
        this.blacklistWhitelist = blacklistWhitelist;
        this.exactMode = exactMode;
    }

    public SensorAttachmentContainerMenu(int windowId, Player player) {
        this(windowId, player, BlockPos.ZERO, Direction.NORTH,
            BlacklistWhitelist.WHITELIST, false,
            new ItemStackHandler(SensorAttachment.MAX_FILTER_SLOTS),
            new FluidInventory(SensorAttachment.MAX_FILTER_SLOTS), false);
    }

    public BlockPos getPos() { return pos; }
    public Direction getDirection() { return dir; }
    public boolean isFluidMode() { return fluidMode; }

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

                if (filterSlot instanceof FluidFilterSlot fluidSlot) {
                    if (fluidSlot.getFluidInventory().getFluid(fluidSlot.getSlotIndex()).isEmpty()) {
                        FluidStack fluidStack = FluidUtil.getFluidContained(slot.getItem()).orElse(FluidStack.EMPTY);
                        if (!fluidStack.isEmpty()) {
                            fluidSlot.getFluidInventory().setFluid(fluidSlot.getSlotIndex(), fluidStack);
                            break;
                        }
                        break;
                    }
                } else if (filterSlot instanceof SlotItemHandler itemSlot) {
                    if (!itemSlot.hasItem()) {
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
        }
        return ItemStack.EMPTY;
    }
}

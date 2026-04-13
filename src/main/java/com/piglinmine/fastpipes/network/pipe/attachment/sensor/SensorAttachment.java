package com.piglinmine.fastpipes.network.pipe.attachment.sensor;

import com.piglinmine.fastpipes.inventory.fluid.FluidInventory;
import com.piglinmine.fastpipes.menu.SensorAttachmentMenuProvider;
import com.piglinmine.fastpipes.network.NetworkManager;
import com.piglinmine.fastpipes.network.fluid.FluidNetwork;
import com.piglinmine.fastpipes.network.pipe.Pipe;
import com.piglinmine.fastpipes.network.pipe.attachment.Attachment;
import com.piglinmine.fastpipes.network.pipe.attachment.extractor.BlacklistWhitelist;
import com.piglinmine.fastpipes.network.pipe.item.ItemPipe;
import com.piglinmine.fastpipes.network.pipe.transport.ItemTransport;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.items.ItemStackHandler;

import javax.annotation.Nullable;

public class SensorAttachment extends Attachment {
    public static final int MAX_FILTER_SLOTS = 15;
    public static final int SIGNAL_DURATION = 20; // 1 second

    private final SensorAttachmentType type;
    private final ItemStackHandler itemFilter;
    private final FluidInventory fluidFilter;

    private BlacklistWhitelist blacklistWhitelist = BlacklistWhitelist.WHITELIST;
    private boolean exactMode = false;
    private int signalTicks = 0;
    private boolean wasActive = false;

    public SensorAttachment(Pipe pipe, Direction direction, SensorAttachmentType type) {
        super(pipe, direction);
        this.type = type;
        this.itemFilter = createItemFilterInventory(this);
        this.fluidFilter = createFluidFilterInventory(this);
    }

    public static ItemStackHandler createItemFilterInventory(@Nullable SensorAttachment attachment) {
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

    public static FluidInventory createFluidFilterInventory(@Nullable SensorAttachment attachment) {
        return new FluidInventory(MAX_FILTER_SLOTS) {
            @Override
            protected void onContentsChanged() {
                super.onContentsChanged();
                if (attachment != null) {
                    NetworkManager.get(attachment.pipe.getLevel()).setDirty();
                }
            }
        };
    }

    public boolean isFluidMode() {
        return pipe.getNetwork() instanceof FluidNetwork;
    }

    @Override
    public void update() {
        if (pipe.getLevel() == null || pipe.getLevel().isClientSide()) {
            return;
        }

        boolean detected = false;

        if (isFluidMode()) {
            detected = detectFluid();
        } else if (pipe instanceof ItemPipe itemPipe) {
            detected = detectItem(itemPipe);
        }

        if (detected && signalTicks <= 0) {
            // Start emitting signal
            signalTicks = SIGNAL_DURATION;
            notifyRedstoneChange();
        }

        if (signalTicks > 0) {
            signalTicks--;
            if (signalTicks <= 0) {
                // Signal expired — notify neighbors
                notifyRedstoneChange();
            }
        }

        boolean isActive = signalTicks > 0;
        if (isActive != wasActive) {
            wasActive = isActive;
            NetworkManager.get(pipe.getLevel()).setDirty();
        }
    }

    private boolean detectItem(ItemPipe itemPipe) {
        for (ItemTransport transport : itemPipe.getTransports()) {
            if (matchesItem(transport.getStack())) {
                return true;
            }
        }
        return false;
    }

    private boolean detectFluid() {
        FluidNetwork fluidNetwork = (FluidNetwork) pipe.getNetwork();
        if (fluidNetwork == null) return false;

        FluidStack fluid = fluidNetwork.getFluidTank().getFluid();
        if (fluid.isEmpty()) return false;

        return matchesFluid(fluid);
    }

    private boolean matchesItem(ItemStack stack) {
        // Check if any filter slots are filled
        boolean hasFilter = false;
        for (int i = 0; i < itemFilter.getSlots(); ++i) {
            if (!itemFilter.getStackInSlot(i).isEmpty()) {
                hasFilter = true;
                break;
            }
        }
        if (!hasFilter) {
            return true; // no filter = detect everything
        }

        if (blacklistWhitelist == BlacklistWhitelist.WHITELIST) {
            for (int i = 0; i < itemFilter.getSlots(); ++i) {
                ItemStack filtered = itemFilter.getStackInSlot(i);
                if (filtered.isEmpty()) continue;
                boolean equals = filtered.is(stack.getItem());
                if (exactMode) equals = equals && ItemStack.isSameItemSameTags(filtered, stack);
                if (equals) return true;
            }
            return false;
        } else {
            for (int i = 0; i < itemFilter.getSlots(); ++i) {
                ItemStack filtered = itemFilter.getStackInSlot(i);
                if (filtered.isEmpty()) continue;
                boolean equals = filtered.is(stack.getItem());
                if (exactMode) equals = equals && ItemStack.isSameItemSameTags(filtered, stack);
                if (equals) return false;
            }
            return true;
        }
    }

    private boolean matchesFluid(FluidStack stack) {
        boolean hasFilter = false;
        for (int i = 0; i < fluidFilter.getSlots(); ++i) {
            if (!fluidFilter.getFluid(i).isEmpty()) {
                hasFilter = true;
                break;
            }
        }
        if (!hasFilter) {
            return true;
        }

        if (blacklistWhitelist == BlacklistWhitelist.WHITELIST) {
            for (int i = 0; i < fluidFilter.getSlots(); ++i) {
                FluidStack filtered = fluidFilter.getFluid(i);
                if (filtered.isEmpty()) continue;
                boolean equals = filtered.getFluid() == stack.getFluid();
                if (exactMode) equals = equals && filtered.isFluidEqual(stack);
                if (equals) return true;
            }
            return false;
        } else {
            for (int i = 0; i < fluidFilter.getSlots(); ++i) {
                FluidStack filtered = fluidFilter.getFluid(i);
                if (filtered.isEmpty()) continue;
                boolean equals = filtered.getFluid() == stack.getFluid();
                if (exactMode) equals = equals && filtered.isFluidEqual(stack);
                if (equals) return false;
            }
            return true;
        }
    }

    private void notifyRedstoneChange() {
        if (pipe.getLevel() != null && !pipe.getLevel().isClientSide()) {
            BlockPos pos = pipe.getPos();
            pipe.getLevel().updateNeighborsAt(pos, pipe.getLevel().getBlockState(pos).getBlock());
        }
    }

    public boolean isSignalActive() {
        return signalTicks > 0;
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
        SensorAttachmentMenuProvider.open(pipe, this, player);
    }

    @Override
    public CompoundTag writeToNbt(CompoundTag tag) {
        tag.putByte("bw", (byte) blacklistWhitelist.ordinal());
        tag.putBoolean("exa", exactMode);
        tag.putInt("sticks", signalTicks);
        tag.put("itemfilter", itemFilter.serializeNBT());
        tag.put("fluidfilter", fluidFilter.writeToNbt());
        return super.writeToNbt(tag);
    }

    public SensorAttachmentType getType() { return type; }
    public ItemStackHandler getItemFilter() { return itemFilter; }
    public FluidInventory getFluidFilter() { return fluidFilter; }

    public BlacklistWhitelist getBlacklistWhitelist() { return blacklistWhitelist; }
    public void setBlacklistWhitelist(BlacklistWhitelist bw) {
        this.blacklistWhitelist = bw;
    }

    public boolean isExactMode() { return exactMode; }
    public void setExactMode(boolean exactMode) {
        this.exactMode = exactMode;
    }
}

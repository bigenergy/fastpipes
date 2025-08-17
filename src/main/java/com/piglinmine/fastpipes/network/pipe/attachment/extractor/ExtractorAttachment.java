package com.piglinmine.fastpipes.network.pipe.attachment.extractor;

import com.piglinmine.fastpipes.inventory.fluid.FluidInventory;
import com.piglinmine.fastpipes.menu.ExtractorAttachmentMenuProvider;
import com.piglinmine.fastpipes.network.Network;
import com.piglinmine.fastpipes.network.NetworkManager;
import com.piglinmine.fastpipes.network.fluid.FluidNetwork;
import com.piglinmine.fastpipes.network.item.ItemNetwork;
import com.piglinmine.fastpipes.network.pipe.Destination;
import com.piglinmine.fastpipes.network.pipe.Pipe;
import com.piglinmine.fastpipes.network.pipe.attachment.Attachment;
import com.piglinmine.fastpipes.network.pipe.item.ItemPipe;
import com.piglinmine.fastpipes.network.pipe.transport.ItemTransport;
import com.piglinmine.fastpipes.network.pipe.transport.callback.ItemBounceBackTransportCallback;
import com.piglinmine.fastpipes.network.pipe.transport.callback.ItemInsertTransportCallback;
import com.piglinmine.fastpipes.network.pipe.transport.callback.ItemPipeGoneTransportCallback;
import com.piglinmine.fastpipes.routing.Path;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemStackHandler;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nullable;

public class ExtractorAttachment extends Attachment {
    public static final int MAX_FILTER_SLOTS = 15;
    private static final Logger LOGGER = LogManager.getLogger(ExtractorAttachment.class);
    private final ExtractorAttachmentType type;
    private final ItemStackHandler itemFilter;
    private final FluidInventory fluidFilter;

    private final ItemDestinationFinder itemDestinationFinder = new ItemDestinationFinder(this);

    private int ticks;
    private RedstoneMode redstoneMode = RedstoneMode.IGNORED;
    private BlacklistWhitelist blacklistWhitelist = BlacklistWhitelist.BLACKLIST;
    private RoutingMode routingMode = RoutingMode.NEAREST;
    private int stackSize;
    private boolean exactMode = true;

    public ExtractorAttachment(Pipe pipe, Direction direction, ExtractorAttachmentType type) {
        super(pipe, direction);

        this.type = type;
        this.stackSize = type.getItemsToExtract();
        this.itemFilter = createItemFilterInventory(this);
        this.fluidFilter = createFluidFilterInventory(this);
    }

    public static ItemStackHandler createItemFilterInventory(@Nullable ExtractorAttachment attachment) {
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

    public static FluidInventory createFluidFilterInventory(@Nullable ExtractorAttachment attachment) {
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
        Network network = pipe.getNetwork();

        int tickInterval = 0;
        if (network instanceof ItemNetwork) {
            tickInterval = type.getItemTickInterval();
        } else if (network instanceof FluidNetwork) {
            tickInterval = type.getFluidTickInterval();
        }

        if (tickInterval != 0 && (ticks++) % tickInterval != 0) {
            return;
        }

        if (!redstoneMode.isEnabled(pipe.getLevel(), pipe.getPos())) {
            return;
        }

        BlockPos destinationPos = pipe.getPos().relative(getDirection());

        BlockEntity blockEntity = pipe.getLevel().getBlockEntity(destinationPos);
        if (blockEntity == null) {
            return;
        }

        if (network instanceof ItemNetwork) {
            IItemHandler itemHandler = blockEntity.getLevel().getCapability(Capabilities.ItemHandler.BLOCK, destinationPos, getDirection().getOpposite());
            if (itemHandler != null) {
                update((ItemNetwork) network, destinationPos, itemHandler);
            }
        } else if (network instanceof FluidNetwork) {
            IFluidHandler fluidHandler = blockEntity.getLevel().getCapability(Capabilities.FluidHandler.BLOCK, destinationPos, getDirection().getOpposite());
            if (fluidHandler != null) {
                update((FluidNetwork) network, fluidHandler);
            }
        }
    }

    private void update(ItemNetwork network, BlockPos sourcePos, IItemHandler source) {
        if (stackSize == 0) {
            return;
        }

        Pair<Destination, Integer> destinationAndSourceSlot = findDestinationAndSourceSlot(sourcePos, source);
        if (destinationAndSourceSlot == null) {
            return;
        }

        Destination destination = destinationAndSourceSlot.getLeft();

        Path<BlockPos> path = network
            .getDestinationPathCache()
            .getPath(pipe.getPos(), destination);
        if (path == null) {
            LOGGER.error("No path found from " + pipe.getPos() + " to " + destination);
            return;
        }

        ItemStack extracted = source.extractItem(destinationAndSourceSlot.getRight(), stackSize, false);
        if (extracted.isEmpty()) {
            return;
        }

        BlockPos fromPos = pipe.getPos().relative(getDirection());

        ((ItemPipe) pipe).addTransport(new ItemTransport(
            extracted.copy(),
            fromPos,
            destination.getReceiver(),
            path.toQueue(),
            new ItemInsertTransportCallback(destination.getReceiver(), destination.getIncomingDirection(), extracted),
            new ItemBounceBackTransportCallback(destination.getReceiver(), sourcePos, extracted),
            new ItemPipeGoneTransportCallback(extracted)
        ));
    }

    private Pair<Destination, Integer> findDestinationAndSourceSlot(BlockPos sourcePos, IItemHandler source) {
        if (source.getSlots() <= 0) {
            return null;
        }

        int startIndex = 0;

        do {
            ItemStack slot = source.getStackInSlot(startIndex);
            if (slot.isEmpty() || !acceptsItem(slot)) {
                startIndex++;
                continue;
            }

            ItemStack extracted = source.extractItem(startIndex, stackSize, true);
            if (extracted.isEmpty()) {
                startIndex++;
                continue;
            }

            Destination destination = itemDestinationFinder.find(routingMode, sourcePos, extracted);
            if (destination == null) {
                startIndex++;
                continue;
            }

            return Pair.of(destination, startIndex);
        } while (startIndex < source.getSlots());

        return null;
    }

    private void update(FluidNetwork network, IFluidHandler source) {
        FluidStack drained = source.drain(type.getFluidsToExtract(), IFluidHandler.FluidAction.SIMULATE);
        if (drained.isEmpty()) {
            return;
        }

        if (!acceptsFluid(drained)) {
            return;
        }

        int filled = network.getFluidTank().fill(drained, IFluidHandler.FluidAction.SIMULATE);
        if (filled <= 0) {
            return;
        }

        int toDrain = Math.min(type.getFluidsToExtract(), filled);

        drained = source.drain(toDrain, IFluidHandler.FluidAction.EXECUTE);

        network.getFluidTank().fill(drained, IFluidHandler.FluidAction.EXECUTE);

        NetworkManager.get(pipe.getLevel()).setDirty();
    }

    private boolean acceptsItem(ItemStack stack) {
        if (blacklistWhitelist == BlacklistWhitelist.WHITELIST) {
            for (int i = 0; i < itemFilter.getSlots(); ++i) {
                ItemStack filtered = itemFilter.getStackInSlot(i);

                boolean equals = filtered.is(stack.getItem());
                if (exactMode) {
                    equals = equals && ItemStack.isSameItemSameComponents(filtered, stack);
                }

                if (equals) {
                    return true;
                }
            }

            return false;
        } else if (blacklistWhitelist == BlacklistWhitelist.BLACKLIST) {
            for (int i = 0; i < itemFilter.getSlots(); ++i) {
                ItemStack filtered = itemFilter.getStackInSlot(i);

                boolean equals = filtered.is(stack.getItem());
                if (exactMode) {
                    equals = equals && ItemStack.isSameItemSameComponents(filtered, stack);
                }

                if (equals) {
                    return false;
                }
            }

            return true;
        }

        return false;
    }

    private boolean acceptsFluid(FluidStack stack) {
        if (blacklistWhitelist == BlacklistWhitelist.WHITELIST) {
            for (int i = 0; i < fluidFilter.getSlots(); ++i) {
                FluidStack filtered = fluidFilter.getFluid(i);

                boolean equals = filtered.getFluid() == stack.getFluid();
                if (exactMode) {
                    equals = equals && FluidStack.isSameFluidSameComponents(filtered, stack);
                }

                if (equals) {
                    return true;
                }
            }

            return false;
        } else if (blacklistWhitelist == BlacklistWhitelist.BLACKLIST) {
            for (int i = 0; i < fluidFilter.getSlots(); ++i) {
                FluidStack filtered = fluidFilter.getFluid(i);

                boolean equals = filtered.getFluid() == stack.getFluid();
                if (exactMode) {
                    equals = equals && FluidStack.isSameFluidSameComponents(filtered, stack);
                }

                if (equals) {
                    return false;
                }
            }

            return true;
        }

        return false;
    }

    @Override
    public void openContainer(ServerPlayer player) {
        super.openContainer(player);

        ExtractorAttachmentMenuProvider.open(pipe, this, player);
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
    public CompoundTag writeToNbt(CompoundTag tag) {
        tag.putByte("rm", (byte) redstoneMode.ordinal());
        tag.put("itemfilter", itemFilter.serializeNBT(pipe.getLevel().registryAccess()));
        tag.putByte("bw", (byte) blacklistWhitelist.ordinal());
        tag.putInt("rr", itemDestinationFinder.getRoundRobinIndex());
        tag.putByte("routingm", (byte) routingMode.ordinal());
        tag.putInt("stacksi", stackSize);
        tag.putBoolean("exa", exactMode);
        tag.put("fluidfilter", fluidFilter.writeToNbt(pipe.getLevel().registryAccess()));

        return super.writeToNbt(tag);
    }

    public ExtractorAttachmentType getType() {
        return type;
    }

    public ItemStackHandler getItemFilter() {
        return itemFilter;
    }

    public FluidInventory getFluidFilter() {
        return fluidFilter;
    }

    public RedstoneMode getRedstoneMode() {
        return redstoneMode;
    }

    public void setRedstoneMode(RedstoneMode redstoneMode) {
        if (!type.getCanSetRedstoneMode()) {
            return;
        }

        this.redstoneMode = redstoneMode;
    }

    public BlacklistWhitelist getBlacklistWhitelist() {
        return blacklistWhitelist;
    }

    public void setBlacklistWhitelist(BlacklistWhitelist blacklistWhitelist) {
        if (!type.getCanSetWhitelistBlacklist()) {
            return;
        }

        this.blacklistWhitelist = blacklistWhitelist;
    }

    public RoutingMode getRoutingMode() {
        return routingMode;
    }

    public void setRoutingMode(RoutingMode routingMode) {
        if (!type.getCanSetRoutingMode()) {
            return;
        }

        this.routingMode = routingMode;
    }

    public int getStackSize() {
        return stackSize;
    }

    public void setStackSize(int stackSize) {
        if (stackSize < 0) {
            stackSize = 0;
        }

        if (stackSize > type.getItemsToExtract()) {
            stackSize = type.getItemsToExtract();
        }

        this.stackSize = stackSize;
    }

    public void setRoundRobinIndex(int roundRobinIndex) {
        itemDestinationFinder.setRoundRobinIndex(roundRobinIndex);
    }

    public boolean isExactMode() {
        return exactMode;
    }

    public void setExactMode(boolean exactMode) {
        if (!type.getCanSetExactMode()) {
            return;
        }

        this.exactMode = exactMode;
    }
} 
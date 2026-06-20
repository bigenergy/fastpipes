package com.piglinmine.fastpipes.network.pipe.transport.callback;

import com.piglinmine.fastpipes.FastPipes;
import com.piglinmine.fastpipes.network.Network;
import com.piglinmine.fastpipes.network.pipe.Destination;
import com.piglinmine.fastpipes.network.pipe.DestinationType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.Identifier;
import net.minecraft.world.Containers;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemHandlerHelper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nullable;

public class ItemBounceBackTransportCallback implements TransportCallback {
    public static final Identifier ID = Identifier.fromNamespaceAndPath(FastPipes.MOD_ID, "item_bounce_back");
    private static final Logger LOGGER = LogManager.getLogger(ItemBounceBackTransportCallback.class);

    private final BlockPos bounceBackItemHandlerPosition;
    private final Direction bounceBackDirection;
    private final ItemStack toInsert;

    public ItemBounceBackTransportCallback(BlockPos bounceBackItemHandlerPosition, Direction bounceBackDirection, ItemStack toInsert) {
        this.bounceBackItemHandlerPosition = bounceBackItemHandlerPosition;
        this.bounceBackDirection = bounceBackDirection;
        this.toInsert = toInsert;
    }

    @Nullable
    public static ItemBounceBackTransportCallback of(CompoundTag tag, HolderLookup.Provider registries) {
        BlockPos bounceBackItemHandlerPosition = BlockPos.of(tag.getLongOr("bbihpos", 0L));
        Direction bounceBackDirection = Direction.values()[tag.getIntOr("bbdir", 0)];
        ItemStack toInsert = com.piglinmine.fastpipes.util.ItemStackSerialization.parseOptional(registries, tag.getCompoundOrEmpty("s"));

        if (toInsert.isEmpty()) {
            LOGGER.warn("Item no longer exists");
            return null;
        }

        return new ItemBounceBackTransportCallback(bounceBackItemHandlerPosition, bounceBackDirection, toInsert);
    }

    @Override
    public void call(Network network, Level level, BlockPos currentPos, TransportCallback cancelCallback) {
        ItemStack remaining = toInsert.copy();

        // 1) Try original source first
        // TODO 1.21.11: Capabilities.Item.BLOCK now returns ResourceHandler<ItemResource>; wrap via IItemHandler.of()
        var capBounceSrc = level.getCapability(Capabilities.Item.BLOCK, bounceBackItemHandlerPosition, bounceBackDirection);
        IItemHandler sourceHandler = capBounceSrc == null ? null : IItemHandler.of(capBounceSrc);
        if (sourceHandler != null) {
            remaining = ItemHandlerHelper.insertItem(sourceHandler, remaining, false);
            if (remaining.isEmpty()) return;
        }

        // 2) Try any other inserter destination in the network (avoid losing items)
        if (network != null) {
            for (Destination dest : network.getDestinations(DestinationType.ITEM_HANDLER)) {
                if (remaining.isEmpty()) break;
                if (dest.getReceiver().equals(bounceBackItemHandlerPosition)) continue;
                // TODO 1.21.11: Capabilities.Item.BLOCK now returns ResourceHandler<ItemResource>; wrap via IItemHandler.of()
                var capBounceAlt = level.getCapability(Capabilities.Item.BLOCK, dest.getReceiver(), dest.getIncomingDirection().getOpposite());
                IItemHandler altHandler = capBounceAlt == null ? null : IItemHandler.of(capBounceAlt);
                if (altHandler == null) continue;
                remaining = ItemHandlerHelper.insertItem(altHandler, remaining, false);
            }
            if (remaining.isEmpty()) return;
        }

        // 3) Last resort — drop at current pipe position
        LOGGER.warn("No inventory in network can accept bounced item at {}; dropping {} at {}",
            bounceBackItemHandlerPosition, remaining, currentPos);
        Containers.dropItemStack(level, currentPos.getX(), currentPos.getY(), currentPos.getZ(), remaining);
    }

    @Override
    public Identifier getId() {
        return ID;
    }

    @Override
    public CompoundTag writeToNbt(CompoundTag tag) {
        tag.putLong("bbihpos", bounceBackItemHandlerPosition.asLong());
        tag.putInt("bbdir", bounceBackDirection.ordinal());
        tag.put("s", new CompoundTag()); // Placeholder - proper serialization needs HolderLookup.Provider
        return tag;
    }

    @Override
    public CompoundTag writeToNbt(CompoundTag tag, HolderLookup.Provider registries) {
        tag.putLong("bbihpos", bounceBackItemHandlerPosition.asLong());
        tag.putInt("bbdir", bounceBackDirection.ordinal());
        tag.put("s", com.piglinmine.fastpipes.util.ItemStackSerialization.saveOptional(registries, toInsert));
        return tag;
    }
} 
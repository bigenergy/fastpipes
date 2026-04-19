package com.piglinmine.fastpipes.network.pipe.transport.callback;
import com.piglinmine.fastpipes.util.CapabilityUtil;

import com.piglinmine.fastpipes.FastPipes;
import com.piglinmine.fastpipes.network.Network;
import com.piglinmine.fastpipes.network.pipe.Destination;
import com.piglinmine.fastpipes.network.pipe.DestinationType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Containers;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nullable;

public class ItemBounceBackTransportCallback implements TransportCallback {
    public static final ResourceLocation ID = new ResourceLocation(FastPipes.MOD_ID, "item_bounce_back");
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
    public static ItemBounceBackTransportCallback of(CompoundTag tag) {
        BlockPos bounceBackItemHandlerPosition = BlockPos.of(tag.getLong("bbihpos"));
        Direction bounceBackDirection = Direction.values()[tag.getInt("bbdir")];
        ItemStack toInsert = ItemStack.of(tag.getCompound("s"));

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
        IItemHandler sourceHandler = CapabilityUtil.getItemHandler(level, bounceBackItemHandlerPosition, bounceBackDirection);
        if (sourceHandler != null) {
            remaining = ItemHandlerHelper.insertItem(sourceHandler, remaining, false);
            if (remaining.isEmpty()) return;
        }

        // 2) Try any other inserter destination in the network (avoid losing items)
        if (network != null) {
            for (Destination dest : network.getDestinations(DestinationType.ITEM_HANDLER)) {
                if (remaining.isEmpty()) break;
                // Skip original source to avoid re-trying same full handler
                if (dest.getReceiver().equals(bounceBackItemHandlerPosition)) continue;
                IItemHandler altHandler = CapabilityUtil.getItemHandler(level, dest.getReceiver(), dest.getIncomingDirection().getOpposite());
                if (altHandler == null) continue;
                remaining = ItemHandlerHelper.insertItem(altHandler, remaining, false);
            }
            if (remaining.isEmpty()) return;
        }

        // 3) Last resort — drop at current pipe position (visible to player, recoverable)
        LOGGER.warn("No inventory in network can accept bounced item at {}; dropping {} at {}",
            bounceBackItemHandlerPosition, remaining, currentPos);
        Containers.dropItemStack(level, currentPos.getX(), currentPos.getY(), currentPos.getZ(), remaining);
    }

    @Override
    public ResourceLocation getId() {
        return ID;
    }

    @Override
    public CompoundTag writeToNbt(CompoundTag tag) {
        tag.putLong("bbihpos", bounceBackItemHandlerPosition.asLong());
        tag.putInt("bbdir", bounceBackDirection.ordinal());
        tag.put("s", toInsert.save(new CompoundTag()));
        return tag;
    }
} 
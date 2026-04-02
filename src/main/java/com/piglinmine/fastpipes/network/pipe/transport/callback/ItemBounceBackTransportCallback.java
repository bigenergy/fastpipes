package com.piglinmine.fastpipes.network.pipe.transport.callback;

import com.piglinmine.fastpipes.FastPipes;
import com.piglinmine.fastpipes.network.Network;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
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
    public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath(FastPipes.MOD_ID, "item_bounce_back");
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
        BlockPos bounceBackItemHandlerPosition = BlockPos.of(tag.getLong("bbihpos"));
        Direction bounceBackDirection = Direction.values()[tag.getInt("bbdir")];
        ItemStack toInsert = ItemStack.parseOptional(registries, tag.getCompound("s"));

        if (toInsert.isEmpty()) {
            LOGGER.warn("Item no longer exists");
            return null;
        }

        return new ItemBounceBackTransportCallback(bounceBackItemHandlerPosition, bounceBackDirection, toInsert);
    }

    @Override
    public void call(Network network, Level level, BlockPos currentPos, TransportCallback cancelCallback) {
        IItemHandler handler = level.getCapability(Capabilities.ItemHandler.BLOCK, bounceBackItemHandlerPosition, bounceBackDirection);
        if (handler != null) {
            ItemStack remainder = ItemHandlerHelper.insertItem(handler, toInsert, false);
            if (remainder.isEmpty()) {
                return;
            }
            // Source inventory is full — drop the remainder at the current pipe position
            Containers.dropItemStack(level, currentPos.getX(), currentPos.getY(), currentPos.getZ(), remainder);
        } else {
            // Source block is gone — drop at current pipe position
            LOGGER.warn("Bounce-back source is gone at {}, dropping item at {}", bounceBackItemHandlerPosition, currentPos);
            Containers.dropItemStack(level, currentPos.getX(), currentPos.getY(), currentPos.getZ(), toInsert);
        }
    }

    @Override
    public ResourceLocation getId() {
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
        tag.put("s", toInsert.saveOptional(registries));
        return tag;
    }
} 
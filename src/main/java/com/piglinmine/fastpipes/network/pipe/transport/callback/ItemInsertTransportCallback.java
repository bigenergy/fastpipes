package com.piglinmine.fastpipes.network.pipe.transport.callback;

import com.piglinmine.fastpipes.FastPipes;
import com.piglinmine.fastpipes.network.Network;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemHandlerHelper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nullable;

public class ItemInsertTransportCallback implements TransportCallback {
    public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath(FastPipes.MOD_ID, "item_insert");
    private static final Logger LOGGER = LogManager.getLogger(ItemInsertTransportCallback.class);
    
    private final BlockPos itemHandlerPosition;
    private final Direction incomingDirection;
    private final ItemStack toInsert;

    public ItemInsertTransportCallback(BlockPos itemHandlerPosition, Direction incomingDirection, ItemStack toInsert) {
        this.itemHandlerPosition = itemHandlerPosition;
        this.incomingDirection = incomingDirection;
        this.toInsert = toInsert;
    }

    @Nullable
    public static ItemInsertTransportCallback of(CompoundTag tag, HolderLookup.Provider registries) {
        BlockPos itemHandlerPosition = BlockPos.of(tag.getLong("ihpos"));
        ItemStack toInsert = ItemStack.parseOptional(registries, tag.getCompound("s"));
        Direction incomingDirection = Direction.values()[tag.getInt("incdir")];

        if (toInsert.isEmpty()) {
            LOGGER.warn("Item no longer exists");
            return null;
        }

        return new ItemInsertTransportCallback(itemHandlerPosition, incomingDirection, toInsert);
    }

    @Override
    public void call(Network network, Level level, BlockPos currentPos, TransportCallback cancelCallback) {
        BlockEntity blockEntity = level.getBlockEntity(itemHandlerPosition);
        if (blockEntity == null) {
            LOGGER.warn("Destination item handler is gone at " + itemHandlerPosition);
            cancelCallback.call(network, level, currentPos, cancelCallback);
            return;
        }

        // Use NeoForge Capabilities API instead of Forge CapabilityItemHandler
        IItemHandler itemHandler = level.getCapability(Capabilities.ItemHandler.BLOCK, itemHandlerPosition, incomingDirection.getOpposite());
        if (itemHandler == null) {
            LOGGER.warn("Destination item handler is no longer exposing a capability at " + itemHandlerPosition);
            cancelCallback.call(network, level, currentPos, cancelCallback);
            return;
        }

        // Try to insert the item (simulate first, then actually insert)
        if (ItemHandlerHelper.insertItem(itemHandler, toInsert, true).isEmpty()) {
            ItemHandlerHelper.insertItem(itemHandler, toInsert, false);
        } else {
            cancelCallback.call(network, level, currentPos, cancelCallback);
        }
    }

    @Override
    public ResourceLocation getId() {
        return ID;
    }

    @Override
    public CompoundTag writeToNbt(CompoundTag tag) {
        // Legacy method for compatibility - use empty CompoundTag as placeholder
        tag.putLong("ihpos", itemHandlerPosition.asLong());
        tag.put("s", new CompoundTag()); // Placeholder - proper serialization needs HolderLookup.Provider
        tag.putInt("incdir", incomingDirection.ordinal());
        return tag;
    }

    @Override
    public CompoundTag writeToNbt(CompoundTag tag, HolderLookup.Provider registries) {
        tag.putLong("ihpos", itemHandlerPosition.asLong());
        tag.put("s", toInsert.saveOptional(registries));
        tag.putInt("incdir", incomingDirection.ordinal());

        return tag;
    }
} 
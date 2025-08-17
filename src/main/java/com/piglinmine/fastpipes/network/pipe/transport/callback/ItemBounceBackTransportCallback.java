package com.piglinmine.fastpipes.network.pipe.transport.callback;

import com.piglinmine.fastpipes.FastPipes;
import com.piglinmine.fastpipes.network.Network;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Containers;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nullable;

public class ItemBounceBackTransportCallback implements TransportCallback {
    public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath(FastPipes.MOD_ID, "item_bounce_back");
    private static final Logger LOGGER = LogManager.getLogger(ItemBounceBackTransportCallback.class);
    
    private final BlockPos originalItemHandlerPosition;
    private final BlockPos bounceBackItemHandlerPosition;
    private final ItemStack toInsert;

    public ItemBounceBackTransportCallback(BlockPos originalItemHandlerPosition, BlockPos bounceBackItemHandlerPosition, ItemStack toInsert) {
        this.originalItemHandlerPosition = originalItemHandlerPosition;
        this.bounceBackItemHandlerPosition = bounceBackItemHandlerPosition;
        this.toInsert = toInsert;
    }

    @Nullable
    public static ItemBounceBackTransportCallback of(CompoundTag tag, HolderLookup.Provider registries) {
        BlockPos originalItemHandlerPosition = BlockPos.of(tag.getLong("oihpos"));
        BlockPos bounceBackItemHandlerPosition = BlockPos.of(tag.getLong("bbihpos"));
        ItemStack toInsert = ItemStack.parseOptional(registries, tag.getCompound("s"));

        if (toInsert.isEmpty()) {
            LOGGER.warn("Item no longer exists");
            return null;
        }

        return new ItemBounceBackTransportCallback(originalItemHandlerPosition, bounceBackItemHandlerPosition, toInsert);
    }

    @Override
    public void call(Network network, Level level, BlockPos currentPos, TransportCallback cancelCallback) {
        // TODO: Actually implement bounce back logic - for now just drop at original position
        Containers.dropItemStack(level, originalItemHandlerPosition.getX(), originalItemHandlerPosition.getY(), originalItemHandlerPosition.getZ(), toInsert);
    }

    @Override
    public ResourceLocation getId() {
        return ID;
    }

    @Override
    public CompoundTag writeToNbt(CompoundTag tag) {
        // Legacy method for compatibility - use empty CompoundTag as placeholder
        tag.putLong("oihpos", originalItemHandlerPosition.asLong());
        tag.putLong("bbihpos", bounceBackItemHandlerPosition.asLong());
        tag.put("s", new CompoundTag()); // Placeholder - proper serialization needs HolderLookup.Provider
        return tag;
    }

    @Override
    public CompoundTag writeToNbt(CompoundTag tag, HolderLookup.Provider registries) {
        tag.putLong("oihpos", originalItemHandlerPosition.asLong());
        tag.putLong("bbihpos", bounceBackItemHandlerPosition.asLong());
        tag.put("s", toInsert.saveOptional(registries));
        return tag;
    }
} 
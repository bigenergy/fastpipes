package com.piglinmine.fastpipes.network.pipe.transport.callback;

import com.piglinmine.fastpipes.FastPipes;
import com.piglinmine.fastpipes.network.Network;
import com.piglinmine.fastpipes.network.pipe.Destination;
import com.piglinmine.fastpipes.network.pipe.DestinationType;
import net.minecraft.core.BlockPos;
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

public class ItemPipeGoneTransportCallback implements TransportCallback {
    public static final Identifier ID = Identifier.fromNamespaceAndPath(FastPipes.MOD_ID, "item_pipe_gone");
    private static final Logger LOGGER = LogManager.getLogger(ItemPipeGoneTransportCallback.class);
    
    private final ItemStack stack;

    public ItemPipeGoneTransportCallback(ItemStack stack) {
        this.stack = stack;
    }

    @Nullable
    public static ItemPipeGoneTransportCallback of(CompoundTag tag, HolderLookup.Provider registries) {
        ItemStack stack = ItemStack.parseOptional(registries, tag.getCompound("s"));

        if (stack.isEmpty()) {
            LOGGER.warn("Item no longer exists");
            return null;
        }

        return new ItemPipeGoneTransportCallback(stack);
    }

    @Override
    public void call(Network network, Level level, BlockPos currentPos, TransportCallback cancelCallback) {
        ItemStack remaining = stack.copy();

        // Try inserting into any inventory in the network first
        if (network != null) {
            for (Destination dest : network.getDestinations(DestinationType.ITEM_HANDLER)) {
                if (remaining.isEmpty()) break;
                IItemHandler handler = level.getCapability(Capabilities.ItemHandler.BLOCK, dest.getReceiver(), dest.getIncomingDirection().getOpposite());
                if (handler == null) continue;
                remaining = ItemHandlerHelper.insertItem(handler, remaining, false);
            }
            if (remaining.isEmpty()) return;
        }

        // Last resort — drop at current position
        Containers.dropItemStack(level, currentPos.getX(), currentPos.getY(), currentPos.getZ(), remaining);
    }

    @Override
    public Identifier getId() {
        return ID;
    }

    @Override
    public CompoundTag writeToNbt(CompoundTag tag) {
        // Legacy method for compatibility - use empty CompoundTag as placeholder
        tag.put("s", new CompoundTag()); // Placeholder - proper serialization needs HolderLookup.Provider
        return tag;
    }

    @Override
    public CompoundTag writeToNbt(CompoundTag tag, HolderLookup.Provider registries) {
        tag.put("s", stack.saveOptional(registries));
        return tag;
    }
} 
package com.piglinmine.fastpipes.network.pipe.transport.callback;

import com.piglinmine.fastpipes.FastPipes;
import com.piglinmine.fastpipes.network.Network;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Containers;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nullable;

public class ItemPipeGoneTransportCallback implements TransportCallback {
    public static final ResourceLocation ID = new ResourceLocation(FastPipes.MOD_ID, "item_pipe_gone");
    private static final Logger LOGGER = LogManager.getLogger(ItemPipeGoneTransportCallback.class);
    
    private final ItemStack stack;

    public ItemPipeGoneTransportCallback(ItemStack stack) {
        this.stack = stack;
    }

    @Nullable
    public static ItemPipeGoneTransportCallback of(CompoundTag tag) {
        ItemStack stack = ItemStack.of(tag.getCompound("s"));

        if (stack.isEmpty()) {
            LOGGER.warn("Item no longer exists");
            return null;
        }

        return new ItemPipeGoneTransportCallback(stack);
    }

    @Override
    public void call(Network network, Level level, BlockPos currentPos, TransportCallback cancelCallback) {
        // Drop the item at the current position when a pipe is gone
        Containers.dropItemStack(level, currentPos.getX(), currentPos.getY(), currentPos.getZ(), stack);
    }

    @Override
    public ResourceLocation getId() {
        return ID;
    }

    @Override
    public CompoundTag writeToNbt(CompoundTag tag) {
        tag.put("s", stack.save(new CompoundTag()));
        return tag;
    }
} 
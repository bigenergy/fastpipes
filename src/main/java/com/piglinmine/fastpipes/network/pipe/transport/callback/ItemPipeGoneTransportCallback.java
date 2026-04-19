package com.piglinmine.fastpipes.network.pipe.transport.callback;

import com.piglinmine.fastpipes.FastPipes;
import com.piglinmine.fastpipes.network.Network;
import com.piglinmine.fastpipes.network.pipe.Destination;
import com.piglinmine.fastpipes.network.pipe.DestinationType;
import com.piglinmine.fastpipes.util.CapabilityUtil;
import net.minecraft.core.BlockPos;
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
        ItemStack remaining = stack.copy();

        // Try to insert into any available destination in the network before dropping
        if (network != null) {
            for (Destination dest : network.getDestinations(DestinationType.ITEM_HANDLER)) {
                if (remaining.isEmpty()) break;
                IItemHandler handler = CapabilityUtil.getItemHandler(level, dest.getReceiver(), dest.getIncomingDirection().getOpposite());
                if (handler == null) continue;
                remaining = ItemHandlerHelper.insertItem(handler, remaining, false);
            }
            if (remaining.isEmpty()) return;
        }

        // Last resort — drop at broken pipe position (visible to player, recoverable)
        LOGGER.warn("No inventory in network can accept orphaned item; dropping {} at {}", remaining, currentPos);
        Containers.dropItemStack(level, currentPos.getX(), currentPos.getY(), currentPos.getZ(), remaining);
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
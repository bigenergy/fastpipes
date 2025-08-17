package com.piglinmine.fastpipes.network.pipe.transport.callback;

import com.piglinmine.fastpipes.network.Network;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;

public interface TransportCallback {
    void call(Network network, Level level, BlockPos currentPos, TransportCallback cancelCallback);

    ResourceLocation getId();

    CompoundTag writeToNbt(CompoundTag tag);
    
    // New method for MC 1.21.1 with HolderLookup.Provider
    default CompoundTag writeToNbt(CompoundTag tag, HolderLookup.Provider registries) {
        return writeToNbt(tag);
    }
} 
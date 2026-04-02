package com.piglinmine.fastpipes.network;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;

public interface NetworkFactory {
    Network create(BlockPos pos);

    Network create(CompoundTag tag);

    default Network create(CompoundTag tag, HolderLookup.Provider provider) {
        return create(tag);
    }
} 
package com.piglinmine.fastpipes.network.pipe.transport.callback;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;

import javax.annotation.Nullable;

public interface TransportCallbackFactory {
    @Nullable
    TransportCallback create(CompoundTag tag, HolderLookup.Provider registries);
}

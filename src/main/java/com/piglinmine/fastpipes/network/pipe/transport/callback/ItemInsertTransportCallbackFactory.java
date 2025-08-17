package com.piglinmine.fastpipes.network.pipe.transport.callback;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;

import javax.annotation.Nullable;

public class ItemInsertTransportCallbackFactory implements TransportCallbackFactory {
    private final HolderLookup.Provider registries;

    public ItemInsertTransportCallbackFactory(HolderLookup.Provider registries) {
        this.registries = registries;
    }

    @Override
    @Nullable
    public TransportCallback create(CompoundTag tag) {
        return ItemInsertTransportCallback.of(tag, registries);
    }
} 
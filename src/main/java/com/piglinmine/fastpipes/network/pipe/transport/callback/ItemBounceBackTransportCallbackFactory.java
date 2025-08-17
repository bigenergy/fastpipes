package com.piglinmine.fastpipes.network.pipe.transport.callback;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;

import javax.annotation.Nullable;

public class ItemBounceBackTransportCallbackFactory implements TransportCallbackFactory {
    private final HolderLookup.Provider registries;

    public ItemBounceBackTransportCallbackFactory(HolderLookup.Provider registries) {
        this.registries = registries;
    }

    @Override
    @Nullable
    public TransportCallback create(CompoundTag tag) {
        return ItemBounceBackTransportCallback.of(tag, registries);
    }
} 
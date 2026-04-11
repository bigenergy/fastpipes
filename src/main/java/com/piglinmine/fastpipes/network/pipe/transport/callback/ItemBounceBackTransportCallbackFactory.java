package com.piglinmine.fastpipes.network.pipe.transport.callback;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;

import javax.annotation.Nullable;

public class ItemBounceBackTransportCallbackFactory implements TransportCallbackFactory {
    @Override
    @Nullable
    public TransportCallback create(CompoundTag tag, HolderLookup.Provider registries) {
        return ItemBounceBackTransportCallback.of(tag, registries);
    }
}

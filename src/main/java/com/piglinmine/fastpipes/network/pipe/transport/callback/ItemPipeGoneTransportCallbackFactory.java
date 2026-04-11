package com.piglinmine.fastpipes.network.pipe.transport.callback;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;

import javax.annotation.Nullable;

public class ItemPipeGoneTransportCallbackFactory implements TransportCallbackFactory {
    @Override
    @Nullable
    public TransportCallback create(CompoundTag tag, HolderLookup.Provider registries) {
        return ItemPipeGoneTransportCallback.of(tag, registries);
    }
}

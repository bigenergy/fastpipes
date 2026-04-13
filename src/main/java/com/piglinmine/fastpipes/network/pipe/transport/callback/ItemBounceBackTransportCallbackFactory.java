package com.piglinmine.fastpipes.network.pipe.transport.callback;

import net.minecraft.nbt.CompoundTag;

import javax.annotation.Nullable;

public class ItemBounceBackTransportCallbackFactory implements TransportCallbackFactory {
    @Override
    @Nullable
    public TransportCallback create(CompoundTag tag) {
        return ItemBounceBackTransportCallback.of(tag);
    }
}

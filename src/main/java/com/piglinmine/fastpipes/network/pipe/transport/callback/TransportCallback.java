package com.piglinmine.fastpipes.network.pipe.transport.callback;

import com.piglinmine.fastpipes.network.Network;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;

public interface TransportCallback {
    void call(Network network, Level level, BlockPos currentPos, TransportCallback cancelCallback);

    ResourceLocation getId();

    CompoundTag writeToNbt(CompoundTag tag);
} 
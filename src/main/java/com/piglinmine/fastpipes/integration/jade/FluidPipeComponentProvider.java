package com.piglinmine.fastpipes.integration.jade;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.config.IPluginConfig;

public enum FluidPipeComponentProvider implements IBlockComponentProvider {
    INSTANCE;

    @Override
    public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
        CompoundTag serverData = accessor.getServerData();

        if (serverData.contains("TransferRate")) {
            int transferRate = serverData.getIntOr("TransferRate", 0);
            int stored = serverData.getIntOr("FluidStored", 0);
            int capacity = serverData.getIntOr("FluidCapacity", 0);
            String fluidName = serverData.getStringOr("FluidName", "");

            tooltip.add(Component.translatable("jade.fastpipes.fluid_transfer_rate", transferRate));
            if (!fluidName.isEmpty() && stored > 0) {
                tooltip.add(Component.translatable("jade.fastpipes.fluid_stored", stored, capacity, fluidName));
            }
        }
    }

    @Override
    public Identifier getUid() {
        return FastPipesJadePlugin.FLUID_PIPE_INFO;
    }
}

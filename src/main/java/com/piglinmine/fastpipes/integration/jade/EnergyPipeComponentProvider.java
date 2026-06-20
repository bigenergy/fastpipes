package com.piglinmine.fastpipes.integration.jade;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.config.IPluginConfig;

public enum EnergyPipeComponentProvider implements IBlockComponentProvider {
    INSTANCE;

    @Override
    public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
        CompoundTag serverData = accessor.getServerData();

        if (serverData.contains("EnergyStored") && serverData.contains("MaxEnergyStored") && serverData.contains("TransferRate")) {
            int transferRate = serverData.getIntOr("TransferRate", 0);

            // Show transfer rate only (energy bar is shown automatically by Jade)
            tooltip.add(Component.translatable("jade.fastpipes.transfer_rate", transferRate));
        }
    }

    @Override
    public Identifier getUid() {
        return FastPipesJadePlugin.ENERGY_PIPE_INFO;
    }
}

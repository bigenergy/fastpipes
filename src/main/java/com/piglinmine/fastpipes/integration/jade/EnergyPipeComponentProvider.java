package com.piglinmine.fastpipes.integration.jade;

import com.piglinmine.fastpipes.blockentity.EnergyPipeBlockEntity;
import com.piglinmine.fastpipes.network.NetworkManager;
import com.piglinmine.fastpipes.network.pipe.Pipe;
import com.piglinmine.fastpipes.network.pipe.energy.EnergyPipe;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.IServerDataProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.config.IPluginConfig;

public enum EnergyPipeComponentProvider implements IBlockComponentProvider, IServerDataProvider<BlockAccessor> {
    INSTANCE;

    @Override
    public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
        CompoundTag serverData = accessor.getServerData();
        
        if (serverData.contains("EnergyStored") && serverData.contains("MaxEnergyStored") && serverData.contains("TransferRate")) {
            int transferRate = serverData.getInt("TransferRate");
            
            // Show transfer rate only (energy bar is shown automatically by Jade)
            tooltip.add(Component.translatable("jade.fastpipes.transfer_rate", transferRate));
        }
    }

    @Override
    public void appendServerData(CompoundTag data, BlockAccessor accessor) {
        if (accessor.getBlockEntity() instanceof EnergyPipeBlockEntity energyPipeBlockEntity) {
            // Get the pipe from the network manager
            NetworkManager networkManager = NetworkManager.get(accessor.getLevel());
            Pipe pipe = networkManager.getPipe(accessor.getPosition());
            
            if (pipe instanceof EnergyPipe energyPipe) {
                var energyStorage = energyPipe.getEnergyStorage();
                if (energyStorage != null) {
                    data.putInt("EnergyStored", energyStorage.getEnergyStored());
                    data.putInt("MaxEnergyStored", energyStorage.getMaxEnergyStored());
                    data.putInt("TransferRate", energyStorage.getEnergyPipeType().getTransferRate());
                } else {
                    // Fallback to client-side data if no network connection
                    data.putInt("EnergyStored", 0);
                    data.putInt("MaxEnergyStored", energyPipeBlockEntity.getEnergyPipeType().getCapacity());
                    data.putInt("TransferRate", energyPipeBlockEntity.getEnergyPipeType().getTransferRate());
                }
            }
        }
    }

    @Override
    public ResourceLocation getUid() {
        return FastPipesJadePlugin.ENERGY_PIPE_INFO;
    }
} 
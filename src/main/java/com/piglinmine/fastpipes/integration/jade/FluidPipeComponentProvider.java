package com.piglinmine.fastpipes.integration.jade;

import com.piglinmine.fastpipes.blockentity.FluidPipeBlockEntity;
import com.piglinmine.fastpipes.network.NetworkManager;
import com.piglinmine.fastpipes.network.fluid.FluidNetwork;
import com.piglinmine.fastpipes.network.pipe.Pipe;
import com.piglinmine.fastpipes.network.pipe.fluid.FluidPipe;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.fluids.FluidStack;

import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.IServerDataProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.config.IPluginConfig;

public enum FluidPipeComponentProvider implements IBlockComponentProvider, IServerDataProvider<BlockAccessor> {
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
    public void appendServerData(CompoundTag data, BlockAccessor accessor) {
        if (accessor.getBlockEntity() instanceof FluidPipeBlockEntity fluidPipeBlockEntity) {
            NetworkManager networkManager = NetworkManager.get(accessor.getLevel());
            Pipe pipe = networkManager.getPipe(accessor.getPosition());

            if (pipe instanceof FluidPipe fluidPipe && pipe.getNetwork() instanceof FluidNetwork fluidNetwork) {
                var tank = fluidNetwork.getFluidTank();
                FluidStack fluid = tank.getFluid();

                data.putInt("TransferRate", fluidPipe.getType().getTransferRate());
                data.putInt("FluidStored", tank.getFluidAmount());
                data.putInt("FluidCapacity", tank.getCapacity());
                data.putString("FluidName", fluid.isEmpty() ? "" : fluid.getHoverName().getString());
            } else {
                data.putInt("TransferRate", fluidPipeBlockEntity.getFluidPipeType().getTransferRate());
                data.putInt("FluidStored", 0);
                data.putInt("FluidCapacity", fluidPipeBlockEntity.getFluidPipeType().getCapacity());
                data.putString("FluidName", "");
            }
        }
    }

    @Override
    public Identifier getUid() {
        return FastPipesJadePlugin.FLUID_PIPE_INFO;
    }
}

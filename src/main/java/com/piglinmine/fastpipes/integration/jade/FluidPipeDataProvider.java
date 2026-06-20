package com.piglinmine.fastpipes.integration.jade;

import com.piglinmine.fastpipes.blockentity.FluidPipeBlockEntity;
import com.piglinmine.fastpipes.network.NetworkManager;
import com.piglinmine.fastpipes.network.fluid.FluidNetwork;
import com.piglinmine.fastpipes.network.pipe.Pipe;
import com.piglinmine.fastpipes.network.pipe.fluid.FluidPipe;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.fluids.FluidStack;

import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IServerDataProvider;

public enum FluidPipeDataProvider implements IServerDataProvider<BlockAccessor> {
    INSTANCE;

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

package com.piglinmine.fastpipes.network.pipe.energy;

import com.piglinmine.fastpipes.network.energy.EnergyNetwork;
import net.neoforged.neoforge.energy.IEnergyStorage;

public class ServerEnergyPipeEnergyStorage implements IEnergyStorage, EnergyPipeEnergyStorage {
    private final EnergyNetwork network;

    public ServerEnergyPipeEnergyStorage(EnergyNetwork network) {
        this.network = network;
    }

    @Override
    public int receiveEnergy(int maxReceive, boolean simulate) {
        return network.getEnergyStorage().receiveEnergy(maxReceive, simulate);
    }

    @Override
    public int extractEnergy(int maxExtract, boolean simulate) {
        return 0;
    }

    @Override
    public int getEnergyStored() {
        return network.getEnergyStorage().getEnergyStored();
    }

    @Override
    public int getMaxEnergyStored() {
        return network.getEnergyStorage().getMaxEnergyStored();
    }

    @Override
    public boolean canExtract() {
        return false;
    }

    @Override
    public boolean canReceive() {
        return true;
    }

    @Override
    public EnergyPipeType getEnergyPipeType() {
        return network.getPipeType();
    }
} 
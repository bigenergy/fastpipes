package com.piglinmine.fastpipes.network.pipe.energy;

import com.piglinmine.fastpipes.FastPipes;
import net.minecraft.resources.ResourceLocation;

public enum EnergyPipeType {
    BASIC(0, 1000, 100),
    IMPROVED(1, 2000, 200),
    ADVANCED(2, 4000, 400),
    ELITE(3, 8000, 800),
    ULTIMATE(4, 16000, 1600);

    private final int tier;
    private final int capacity;
    private final int transferRate;

    EnergyPipeType(int tier, int capacity, int transferRate) {
        this.tier = tier;
        this.capacity = capacity;
        this.transferRate = transferRate;
    }

    public static EnergyPipeType get(int ordinal) {
        EnergyPipeType[] values = values();
        if (ordinal < 0 || ordinal >= values.length) {
            return BASIC;
        }
        return values[ordinal];
    }

    public int getTier() {
        return tier;
    }

    public int getCapacity() {
        return capacity;
    }

    public int getTransferRate() {
        return transferRate;
    }

    public ResourceLocation getId() {
        switch (this) {
            case BASIC:
                return ResourceLocation.fromNamespaceAndPath(FastPipes.MOD_ID, "basic_energy_pipe");
            case IMPROVED:
                return ResourceLocation.fromNamespaceAndPath(FastPipes.MOD_ID, "improved_energy_pipe");
            case ADVANCED:
                return ResourceLocation.fromNamespaceAndPath(FastPipes.MOD_ID, "advanced_energy_pipe");
            case ELITE:
                return ResourceLocation.fromNamespaceAndPath(FastPipes.MOD_ID, "elite_energy_pipe");
            case ULTIMATE:
                return ResourceLocation.fromNamespaceAndPath(FastPipes.MOD_ID, "ultimate_energy_pipe");
            default:
                throw new RuntimeException("Unknown EnergyPipeType: " + this);
        }
    }

    public ResourceLocation getNetworkType() {
        return ResourceLocation.fromNamespaceAndPath(FastPipes.MOD_ID, "energy_" + name().toLowerCase());
    }
} 
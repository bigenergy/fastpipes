package com.piglinmine.fastpipes.network.pipe.energy;

import com.piglinmine.fastpipes.FastPipes;
import com.piglinmine.fastpipes.config.ServerConfig;
import net.minecraft.resources.ResourceLocation;

public enum EnergyPipeType {
    BASIC(0),
    IMPROVED(1),
    ADVANCED(2),
    ELITE(3),
    ULTIMATE(4);

    private final int tier;

    EnergyPipeType(int tier) {
        this.tier = tier;
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

    private ServerConfig.EnergyPipe getConfig() {
        switch (this) {
            case BASIC: return FastPipes.SERVER_CONFIG.getBasicEnergyPipe();
            case IMPROVED: return FastPipes.SERVER_CONFIG.getImprovedEnergyPipe();
            case ADVANCED: return FastPipes.SERVER_CONFIG.getAdvancedEnergyPipe();
            case ELITE: return FastPipes.SERVER_CONFIG.getEliteEnergyPipe();
            case ULTIMATE: return FastPipes.SERVER_CONFIG.getUltimateEnergyPipe();
            default: throw new RuntimeException("Unknown EnergyPipeType: " + this);
        }
    }

    public int getCapacity() {
        return getConfig().getCapacity();
    }

    public int getTransferRate() {
        return getConfig().getTransferRate();
    }

    public ResourceLocation getId() {
        switch (this) {
            case BASIC:
                return new ResourceLocation(FastPipes.MOD_ID, "basic_energy_pipe");
            case IMPROVED:
                return new ResourceLocation(FastPipes.MOD_ID, "improved_energy_pipe");
            case ADVANCED:
                return new ResourceLocation(FastPipes.MOD_ID, "advanced_energy_pipe");
            case ELITE:
                return new ResourceLocation(FastPipes.MOD_ID, "elite_energy_pipe");
            case ULTIMATE:
                return new ResourceLocation(FastPipes.MOD_ID, "ultimate_energy_pipe");
            default:
                throw new RuntimeException("Unknown EnergyPipeType: " + this);
        }
    }

    public ResourceLocation getNetworkType() {
        return new ResourceLocation(FastPipes.MOD_ID, "energy_" + name().toLowerCase());
    }
} 
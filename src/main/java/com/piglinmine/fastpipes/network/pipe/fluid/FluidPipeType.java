package com.piglinmine.fastpipes.network.pipe.fluid;

import com.piglinmine.fastpipes.FastPipes;
import com.piglinmine.fastpipes.config.ServerConfig;
import net.minecraft.resources.ResourceLocation;

public enum FluidPipeType {
    BASIC(0),
    IMPROVED(1),
    ADVANCED(2),
    ELITE(3),
    ULTIMATE(4);

    private final int tier;

    FluidPipeType(int tier) {
        this.tier = tier;
    }

    public static FluidPipeType get(int ordinal) {
        FluidPipeType[] values = values();
        if (ordinal < 0 || ordinal >= values.length) {
            return BASIC;
        }
        return values[ordinal];
    }

    public int getTier() {
        return tier;
    }

    private ServerConfig.FluidPipe getConfig() {
        switch (this) {
            case BASIC: return FastPipes.SERVER_CONFIG.getBasicFluidPipe();
            case IMPROVED: return FastPipes.SERVER_CONFIG.getImprovedFluidPipe();
            case ADVANCED: return FastPipes.SERVER_CONFIG.getAdvancedFluidPipe();
            case ELITE: return FastPipes.SERVER_CONFIG.getEliteFluidPipe();
            case ULTIMATE: return FastPipes.SERVER_CONFIG.getUltimateFluidPipe();
            default: throw new RuntimeException("Unknown FluidPipeType: " + this);
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
                return new ResourceLocation(FastPipes.MOD_ID, "basic_fluid_pipe");
            case IMPROVED:
                return new ResourceLocation(FastPipes.MOD_ID, "improved_fluid_pipe");
            case ADVANCED:
                return new ResourceLocation(FastPipes.MOD_ID, "advanced_fluid_pipe");
            case ELITE:
                return new ResourceLocation(FastPipes.MOD_ID, "elite_fluid_pipe");
            case ULTIMATE:
                return new ResourceLocation(FastPipes.MOD_ID, "ultimate_fluid_pipe");
            default:
                throw new RuntimeException("Unknown FluidPipeType: " + this);
        }
    }

    public ResourceLocation getNetworkType() {
        return new ResourceLocation(FastPipes.MOD_ID, "fluid_" + name().toLowerCase());
    }
} 
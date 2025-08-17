package com.piglinmine.fastpipes.network.pipe.fluid;

import com.piglinmine.fastpipes.FastPipes;
import net.minecraft.resources.ResourceLocation;

public enum FluidPipeType {
    BASIC(0, 1000),
    IMPROVED(1, 2000),
    ADVANCED(2, 4000),
    ELITE(3, 8000),
    ULTIMATE(4, 16000);

    private final int tier;
    private final int capacity;

    FluidPipeType(int tier, int capacity) {
        this.tier = tier;
        this.capacity = capacity;
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

    public int getCapacity() {
        return capacity;
    }

    public int getTransferRate() {
        // TODO: Use config values when available
        switch (this) {
            case BASIC:
                return 50;
            case IMPROVED:
                return 100;
            case ADVANCED:
                return 200;
            case ELITE:
                return 400;
            case ULTIMATE:
                return 800;
            default:
                throw new RuntimeException("Unknown FluidPipeType: " + this);
        }
    }

    public ResourceLocation getId() {
        switch (this) {
            case BASIC:
                return ResourceLocation.fromNamespaceAndPath(FastPipes.MOD_ID, "basic_fluid_pipe");
            case IMPROVED:
                return ResourceLocation.fromNamespaceAndPath(FastPipes.MOD_ID, "improved_fluid_pipe");
            case ADVANCED:
                return ResourceLocation.fromNamespaceAndPath(FastPipes.MOD_ID, "advanced_fluid_pipe");
            case ELITE:
                return ResourceLocation.fromNamespaceAndPath(FastPipes.MOD_ID, "elite_fluid_pipe");
            case ULTIMATE:
                return ResourceLocation.fromNamespaceAndPath(FastPipes.MOD_ID, "ultimate_fluid_pipe");
            default:
                throw new RuntimeException("Unknown FluidPipeType: " + this);
        }
    }

    public ResourceLocation getNetworkType() {
        return ResourceLocation.fromNamespaceAndPath(FastPipes.MOD_ID, "fluid_" + name().toLowerCase());
    }
} 
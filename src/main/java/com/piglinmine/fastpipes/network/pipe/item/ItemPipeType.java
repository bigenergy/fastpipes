package com.piglinmine.fastpipes.network.pipe.item;

import com.piglinmine.fastpipes.FastPipes;
import net.minecraft.resources.ResourceLocation;

public enum ItemPipeType {
    BASIC(0),
    IMPROVED(1),
    ADVANCED(2);

    private final int tier;

    ItemPipeType(int tier) {
        this.tier = tier;
    }

    public static ItemPipeType get(int ordinal) {
        ItemPipeType[] values = values();
        if (ordinal < 0 || ordinal >= values.length) {
            return BASIC;
        }
        return values[ordinal];
    }

    public int getTier() {
        return tier;
    }

    public int getMaxTicksInPipe() {
        switch (this) {
            case BASIC:
                return 20; // TODO: Use config value
            case IMPROVED:
                return 15; // TODO: Use config value
            case ADVANCED:
                return 10; // TODO: Use config value
            default:
                throw new RuntimeException("Unknown ItemPipeType: " + this);
        }
    }

    public ResourceLocation getId() {
        switch (this) {
            case BASIC:
                return ResourceLocation.fromNamespaceAndPath(FastPipes.MOD_ID, "basic_item_pipe");
            case IMPROVED:
                return ResourceLocation.fromNamespaceAndPath(FastPipes.MOD_ID, "improved_item_pipe");
            case ADVANCED:
                return ResourceLocation.fromNamespaceAndPath(FastPipes.MOD_ID, "advanced_item_pipe");
            default:
                throw new RuntimeException("Unknown ItemPipeType: " + this);
        }
    }

    public ResourceLocation getNetworkType() {
        return ResourceLocation.fromNamespaceAndPath(FastPipes.MOD_ID, "item_" + name().toLowerCase());
    }
} 
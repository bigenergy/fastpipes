package com.piglinmine.fastpipes.network.pipe.item;

import com.piglinmine.fastpipes.FastPipes;
import com.piglinmine.fastpipes.config.ServerConfig;
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

    private ServerConfig.ItemPipe getConfig() {
        switch (this) {
            case BASIC: return FastPipes.SERVER_CONFIG.getBasicItemPipe();
            case IMPROVED: return FastPipes.SERVER_CONFIG.getImprovedItemPipe();
            case ADVANCED: return FastPipes.SERVER_CONFIG.getAdvancedItemPipe();
            default: throw new RuntimeException("Unknown ItemPipeType: " + this);
        }
    }

    public int getMaxTicksInPipe() {
        return getConfig().getMaxTicks();
    }

    public ResourceLocation getId() {
        switch (this) {
            case BASIC:
                return new ResourceLocation(FastPipes.MOD_ID, "basic_item_pipe");
            case IMPROVED:
                return new ResourceLocation(FastPipes.MOD_ID, "improved_item_pipe");
            case ADVANCED:
                return new ResourceLocation(FastPipes.MOD_ID, "advanced_item_pipe");
            default:
                throw new RuntimeException("Unknown ItemPipeType: " + this);
        }
    }

    public ResourceLocation getNetworkType() {
        return new ResourceLocation(FastPipes.MOD_ID, "item_" + name().toLowerCase());
    }
} 
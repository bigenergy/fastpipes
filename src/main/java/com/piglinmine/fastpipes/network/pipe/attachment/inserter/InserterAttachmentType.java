package com.piglinmine.fastpipes.network.pipe.attachment.inserter;

import com.piglinmine.fastpipes.FastPipes;
import com.piglinmine.fastpipes.FPipesItems;
import com.piglinmine.fastpipes.config.ServerConfig;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;

public enum InserterAttachmentType {
    BASIC(1),
    IMPROVED(2),
    ADVANCED(3),
    ELITE(4),
    ULTIMATE(5);

    private final int tier;

    InserterAttachmentType(int tier) {
        this.tier = tier;
    }

    public static InserterAttachmentType get(byte b) {
        InserterAttachmentType[] v = values();
        if (b < 0 || b >= v.length) return BASIC;
        return v[b];
    }

    public int getTier() { return tier; }

    public InserterAttachmentFactory getFactory() {
        return new InserterAttachmentFactory(this);
    }

    public int getFilterSlots() { return getConfig().getFilterSlots(); }
    public boolean getCanSetRedstoneMode() { return getConfig().getCanSetRedstoneMode(); }
    public boolean getCanSetWhitelistBlacklist() { return getConfig().getCanSetWhitelistBlacklist(); }
    public boolean getCanSetExactMode() { return getConfig().getCanSetExactMode(); }
    public int getPriority() { return getConfig().getPriority(); }

    private ServerConfig.InserterAttachment getConfig() {
        switch (this) {
            case BASIC:    return FastPipes.SERVER_CONFIG.getBasicInserterAttachment();
            case IMPROVED: return FastPipes.SERVER_CONFIG.getImprovedInserterAttachment();
            case ADVANCED: return FastPipes.SERVER_CONFIG.getAdvancedInserterAttachment();
            case ELITE:    return FastPipes.SERVER_CONFIG.getEliteInserterAttachment();
            case ULTIMATE: return FastPipes.SERVER_CONFIG.getUltimateInserterAttachment();
            default: throw new RuntimeException("Unknown InserterAttachmentType: " + this);
        }
    }

    public ResourceLocation getId() {
        switch (this) {
            case BASIC:    return ResourceLocation.fromNamespaceAndPath(FastPipes.MOD_ID, "basic_inserter");
            case IMPROVED: return ResourceLocation.fromNamespaceAndPath(FastPipes.MOD_ID, "improved_inserter");
            case ADVANCED: return ResourceLocation.fromNamespaceAndPath(FastPipes.MOD_ID, "advanced_inserter");
            case ELITE:    return ResourceLocation.fromNamespaceAndPath(FastPipes.MOD_ID, "elite_inserter");
            case ULTIMATE: return ResourceLocation.fromNamespaceAndPath(FastPipes.MOD_ID, "ultimate_inserter");
            default: throw new RuntimeException("Unknown InserterAttachmentType: " + this);
        }
    }

    ResourceLocation getItemId() {
        switch (this) {
            case BASIC:    return ResourceLocation.fromNamespaceAndPath(FastPipes.MOD_ID, "basic_inserter_attachment");
            case IMPROVED: return ResourceLocation.fromNamespaceAndPath(FastPipes.MOD_ID, "improved_inserter_attachment");
            case ADVANCED: return ResourceLocation.fromNamespaceAndPath(FastPipes.MOD_ID, "advanced_inserter_attachment");
            case ELITE:    return ResourceLocation.fromNamespaceAndPath(FastPipes.MOD_ID, "elite_inserter_attachment");
            case ULTIMATE: return ResourceLocation.fromNamespaceAndPath(FastPipes.MOD_ID, "ultimate_inserter_attachment");
            default: throw new RuntimeException("Unknown InserterAttachmentType: " + this);
        }
    }

    Item getItem() {
        switch (this) {
            case BASIC:    return FPipesItems.BASIC_INSERTER_ATTACHMENT.get();
            case IMPROVED: return FPipesItems.IMPROVED_INSERTER_ATTACHMENT.get();
            case ADVANCED: return FPipesItems.ADVANCED_INSERTER_ATTACHMENT.get();
            case ELITE:    return FPipesItems.ELITE_INSERTER_ATTACHMENT.get();
            case ULTIMATE: return FPipesItems.ULTIMATE_INSERTER_ATTACHMENT.get();
            default: throw new RuntimeException("Unknown InserterAttachmentType: " + this);
        }
    }

    ResourceLocation getModelLocation() {
        switch (this) {
            case BASIC:    return ResourceLocation.fromNamespaceAndPath(FastPipes.MOD_ID, "block/pipe/attachment/inserter/basic");
            case IMPROVED: return ResourceLocation.fromNamespaceAndPath(FastPipes.MOD_ID, "block/pipe/attachment/inserter/improved");
            case ADVANCED: return ResourceLocation.fromNamespaceAndPath(FastPipes.MOD_ID, "block/pipe/attachment/inserter/advanced");
            case ELITE:    return ResourceLocation.fromNamespaceAndPath(FastPipes.MOD_ID, "block/pipe/attachment/inserter/elite");
            case ULTIMATE: return ResourceLocation.fromNamespaceAndPath(FastPipes.MOD_ID, "block/pipe/attachment/inserter/ultimate");
            default: throw new RuntimeException("Unknown InserterAttachmentType: " + this);
        }
    }
}

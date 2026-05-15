package com.piglinmine.fastpipes.network.pipe.attachment.extractor;

import com.piglinmine.fastpipes.FastPipes;
import com.piglinmine.fastpipes.FPipesItems;
import com.piglinmine.fastpipes.config.ServerConfig;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;

public enum ExtractorAttachmentType {
    BASIC(1),
    IMPROVED(2),
    ADVANCED(3),
    ELITE(4),
    ULTIMATE(5);

    private final int tier;

    ExtractorAttachmentType(int tier) {
        this.tier = tier;
    }

    public static ExtractorAttachmentType get(byte b) {
        ExtractorAttachmentType[] v = values();

        if (b < 0 || b >= v.length) {
            return BASIC;
        }

        return v[b];
    }

    public int getTier() {
        return tier;
    }

    public ExtractorAttachmentFactory getFactory() {
        return new ExtractorAttachmentFactory(this);
    }

    int getItemTickInterval() {
        return getConfig().getItemTickInterval();
    }

    int getFluidTickInterval() {
        return getConfig().getFluidTickInterval();
    }

    public int getItemsToExtract() {
        return getConfig().getItemsToExtract();
    }

    int getFluidsToExtract() {
        return getConfig().getFluidsToExtract();
    }

    public int getFilterSlots() {
        return getConfig().getFilterSlots();
    }

    public boolean getCanSetRedstoneMode() {
        return getConfig().getCanSetRedstoneMode();
    }

    public boolean getCanSetWhitelistBlacklist() {
        return getConfig().getCanSetWhitelistBlacklist();
    }

    public boolean getCanSetRoutingMode() {
        return getConfig().getCanSetRoutingMode();
    }

    public boolean getCanSetExactMode() {
        return getConfig().getCanSetExactMode();
    }

    private ServerConfig.ExtractorAttachment getConfig() {
        switch (this) {
            case BASIC:
                return FastPipes.SERVER_CONFIG.getBasicExtractorAttachment();
            case IMPROVED:
                return FastPipes.SERVER_CONFIG.getImprovedExtractorAttachment();
            case ADVANCED:
                return FastPipes.SERVER_CONFIG.getAdvancedExtractorAttachment();
            case ELITE:
                return FastPipes.SERVER_CONFIG.getEliteExtractorAttachment();
            case ULTIMATE:
                return FastPipes.SERVER_CONFIG.getUltimateExtractorAttachment();
            default:
                throw new RuntimeException("?");
        }
    }

    public ResourceLocation getId() {
        switch (this) {
            case BASIC:
                return ResourceLocation.fromNamespaceAndPath(FastPipes.MOD_ID, "basic_extractor");
            case IMPROVED:
                return ResourceLocation.fromNamespaceAndPath(FastPipes.MOD_ID, "improved_extractor");
            case ADVANCED:
                return ResourceLocation.fromNamespaceAndPath(FastPipes.MOD_ID, "advanced_extractor");
            case ELITE:
                return ResourceLocation.fromNamespaceAndPath(FastPipes.MOD_ID, "elite_extractor");
            case ULTIMATE:
                return ResourceLocation.fromNamespaceAndPath(FastPipes.MOD_ID, "ultimate_extractor");
            default:
                throw new RuntimeException("?");
        }
    }

    ResourceLocation getItemId() {
        switch (this) {
            case BASIC:
                return ResourceLocation.fromNamespaceAndPath(FastPipes.MOD_ID, "basic_extractor_attachment");
            case IMPROVED:
                return ResourceLocation.fromNamespaceAndPath(FastPipes.MOD_ID, "improved_extractor_attachment");
            case ADVANCED:
                return ResourceLocation.fromNamespaceAndPath(FastPipes.MOD_ID, "advanced_extractor_attachment");
            case ELITE:
                return ResourceLocation.fromNamespaceAndPath(FastPipes.MOD_ID, "elite_extractor_attachment");
            case ULTIMATE:
                return ResourceLocation.fromNamespaceAndPath(FastPipes.MOD_ID, "ultimate_extractor_attachment");
            default:
                throw new RuntimeException("?");
        }
    }

    Item getItem() {
        switch (this) {
            case BASIC:
                return FPipesItems.BASIC_EXTRACTOR_ATTACHMENT.get();
            case IMPROVED:
                return FPipesItems.IMPROVED_EXTRACTOR_ATTACHMENT.get();
            case ADVANCED:
                return FPipesItems.ADVANCED_EXTRACTOR_ATTACHMENT.get();
            case ELITE:
                return FPipesItems.ELITE_EXTRACTOR_ATTACHMENT.get();
            case ULTIMATE:
                return FPipesItems.ULTIMATE_EXTRACTOR_ATTACHMENT.get();
            default:
                throw new RuntimeException("?");
        }
    }

    ResourceLocation getModelLocation() {
        switch (this) {
            case BASIC:
                return ResourceLocation.fromNamespaceAndPath(FastPipes.MOD_ID, "block/pipe/attachment/extractor/basic");
            case IMPROVED:
                return ResourceLocation.fromNamespaceAndPath(FastPipes.MOD_ID, "block/pipe/attachment/extractor/improved");
            case ADVANCED:
                return ResourceLocation.fromNamespaceAndPath(FastPipes.MOD_ID, "block/pipe/attachment/extractor/advanced");
            case ELITE:
                return ResourceLocation.fromNamespaceAndPath(FastPipes.MOD_ID, "block/pipe/attachment/extractor/elite");
            case ULTIMATE:
                return ResourceLocation.fromNamespaceAndPath(FastPipes.MOD_ID, "block/pipe/attachment/extractor/ultimate");
            default:
                throw new RuntimeException("?");
        }
    }
} 
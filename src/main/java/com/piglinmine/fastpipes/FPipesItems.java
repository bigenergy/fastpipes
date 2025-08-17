package com.piglinmine.fastpipes;

import com.piglinmine.fastpipes.item.AttachmentItem;
import com.piglinmine.fastpipes.item.BaseBlockItem;
import com.piglinmine.fastpipes.network.pipe.attachment.extractor.ExtractorAttachmentType;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class FPipesItems {
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(net.minecraft.core.registries.Registries.ITEM, FastPipes.MOD_ID);

    // Block Items for Pipes
    public static final DeferredHolder<Item, BaseBlockItem> BASIC_ITEM_PIPE = ITEMS.register(
        "basic_item_pipe", 
        () -> new BaseBlockItem(FPipesBlocks.BASIC_ITEM_PIPE.get())
    );
    public static final DeferredHolder<Item, BaseBlockItem> IMPROVED_ITEM_PIPE = ITEMS.register(
        "improved_item_pipe", 
        () -> new BaseBlockItem(FPipesBlocks.IMPROVED_ITEM_PIPE.get())
    );
    public static final DeferredHolder<Item, BaseBlockItem> ADVANCED_ITEM_PIPE = ITEMS.register(
        "advanced_item_pipe", 
        () -> new BaseBlockItem(FPipesBlocks.ADVANCED_ITEM_PIPE.get())
    );

    public static final DeferredHolder<Item, BaseBlockItem> BASIC_FLUID_PIPE = ITEMS.register(
        "basic_fluid_pipe", 
        () -> new BaseBlockItem(FPipesBlocks.BASIC_FLUID_PIPE.get())
    );
    public static final DeferredHolder<Item, BaseBlockItem> IMPROVED_FLUID_PIPE = ITEMS.register(
        "improved_fluid_pipe", 
        () -> new BaseBlockItem(FPipesBlocks.IMPROVED_FLUID_PIPE.get())
    );
    public static final DeferredHolder<Item, BaseBlockItem> ADVANCED_FLUID_PIPE = ITEMS.register(
        "advanced_fluid_pipe", 
        () -> new BaseBlockItem(FPipesBlocks.ADVANCED_FLUID_PIPE.get())
    );
    public static final DeferredHolder<Item, BaseBlockItem> ELITE_FLUID_PIPE = ITEMS.register(
        "elite_fluid_pipe", 
        () -> new BaseBlockItem(FPipesBlocks.ELITE_FLUID_PIPE.get())
    );
    public static final DeferredHolder<Item, BaseBlockItem> ULTIMATE_FLUID_PIPE = ITEMS.register(
        "ultimate_fluid_pipe", 
        () -> new BaseBlockItem(FPipesBlocks.ULTIMATE_FLUID_PIPE.get())
    );

    public static final DeferredHolder<Item, BaseBlockItem> BASIC_ENERGY_PIPE = ITEMS.register(
        "basic_energy_pipe", 
        () -> new BaseBlockItem(FPipesBlocks.BASIC_ENERGY_PIPE.get())
    );
    public static final DeferredHolder<Item, BaseBlockItem> IMPROVED_ENERGY_PIPE = ITEMS.register(
        "improved_energy_pipe", 
        () -> new BaseBlockItem(FPipesBlocks.IMPROVED_ENERGY_PIPE.get())
    );
    public static final DeferredHolder<Item, BaseBlockItem> ADVANCED_ENERGY_PIPE = ITEMS.register(
        "advanced_energy_pipe", 
        () -> new BaseBlockItem(FPipesBlocks.ADVANCED_ENERGY_PIPE.get())
    );
    public static final DeferredHolder<Item, BaseBlockItem> ELITE_ENERGY_PIPE = ITEMS.register(
        "elite_energy_pipe", 
        () -> new BaseBlockItem(FPipesBlocks.ELITE_ENERGY_PIPE.get())
    );
    public static final DeferredHolder<Item, BaseBlockItem> ULTIMATE_ENERGY_PIPE = ITEMS.register(
        "ultimate_energy_pipe", 
        () -> new BaseBlockItem(FPipesBlocks.ULTIMATE_ENERGY_PIPE.get())
    );

    // Attachment Items
    public static final DeferredHolder<Item, AttachmentItem> BASIC_EXTRACTOR_ATTACHMENT = ITEMS.register(
        "basic_extractor_attachment", 
        () -> new AttachmentItem(ExtractorAttachmentType.BASIC.getFactory())
    );
    public static final DeferredHolder<Item, AttachmentItem> IMPROVED_EXTRACTOR_ATTACHMENT = ITEMS.register(
        "improved_extractor_attachment", 
        () -> new AttachmentItem(ExtractorAttachmentType.IMPROVED.getFactory())
    );
    public static final DeferredHolder<Item, AttachmentItem> ADVANCED_EXTRACTOR_ATTACHMENT = ITEMS.register(
        "advanced_extractor_attachment", 
        () -> new AttachmentItem(ExtractorAttachmentType.ADVANCED.getFactory())
    );
    public static final DeferredHolder<Item, AttachmentItem> ELITE_EXTRACTOR_ATTACHMENT = ITEMS.register(
        "elite_extractor_attachment", 
        () -> new AttachmentItem(ExtractorAttachmentType.ELITE.getFactory())
    );
    public static final DeferredHolder<Item, AttachmentItem> ULTIMATE_EXTRACTOR_ATTACHMENT = ITEMS.register(
        "ultimate_extractor_attachment", 
        () -> new AttachmentItem(ExtractorAttachmentType.ULTIMATE.getFactory())
    );
} 
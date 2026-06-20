package com.piglinmine.fastpipes;

import com.piglinmine.fastpipes.barrel.BarrelTier;
import com.piglinmine.fastpipes.barrel.BarrelUpgradeItem;
import com.piglinmine.fastpipes.item.AttachmentItem;
import com.piglinmine.fastpipes.item.BaseBlockItem;
import com.piglinmine.fastpipes.item.WrenchItem;
import com.piglinmine.fastpipes.network.pipe.attachment.extractor.ExtractorAttachmentType;
import com.piglinmine.fastpipes.network.pipe.attachment.inserter.InserterAttachmentType;
import com.piglinmine.fastpipes.network.pipe.attachment.sensor.SensorAttachmentType;
import com.piglinmine.fastpipes.network.pipe.attachment.void_attachment.VoidAttachmentType;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

// 1.21.11: Use DeferredRegister.Items + registerItem(name, factory, properties) so
// Item.Properties.setId(...) is populated before the Item constructor runs.
// The bare ITEMS.register(name, () -> new Item(new Item.Properties())) path crashes
// in Item.<init> with "Item id not set".
public class FPipesItems {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(FastPipes.MOD_ID);

    // Block Items for Pipes
    public static final DeferredItem<BaseBlockItem> BASIC_ITEM_PIPE = ITEMS.registerItem(
        "basic_item_pipe",
        props -> new BaseBlockItem(FPipesBlocks.BASIC_ITEM_PIPE.get(), props)
    );
    public static final DeferredItem<BaseBlockItem> IMPROVED_ITEM_PIPE = ITEMS.registerItem(
        "improved_item_pipe",
        props -> new BaseBlockItem(FPipesBlocks.IMPROVED_ITEM_PIPE.get(), props)
    );
    public static final DeferredItem<BaseBlockItem> ADVANCED_ITEM_PIPE = ITEMS.registerItem(
        "advanced_item_pipe",
        props -> new BaseBlockItem(FPipesBlocks.ADVANCED_ITEM_PIPE.get(), props)
    );

    public static final DeferredItem<BaseBlockItem> BASIC_FLUID_PIPE = ITEMS.registerItem(
        "basic_fluid_pipe",
        props -> new BaseBlockItem(FPipesBlocks.BASIC_FLUID_PIPE.get(), props)
    );
    public static final DeferredItem<BaseBlockItem> IMPROVED_FLUID_PIPE = ITEMS.registerItem(
        "improved_fluid_pipe",
        props -> new BaseBlockItem(FPipesBlocks.IMPROVED_FLUID_PIPE.get(), props)
    );
    public static final DeferredItem<BaseBlockItem> ADVANCED_FLUID_PIPE = ITEMS.registerItem(
        "advanced_fluid_pipe",
        props -> new BaseBlockItem(FPipesBlocks.ADVANCED_FLUID_PIPE.get(), props)
    );
    public static final DeferredItem<BaseBlockItem> ELITE_FLUID_PIPE = ITEMS.registerItem(
        "elite_fluid_pipe",
        props -> new BaseBlockItem(FPipesBlocks.ELITE_FLUID_PIPE.get(), props)
    );
    public static final DeferredItem<BaseBlockItem> ULTIMATE_FLUID_PIPE = ITEMS.registerItem(
        "ultimate_fluid_pipe",
        props -> new BaseBlockItem(FPipesBlocks.ULTIMATE_FLUID_PIPE.get(), props)
    );

    public static final DeferredItem<BaseBlockItem> BASIC_ENERGY_PIPE = ITEMS.registerItem(
        "basic_energy_pipe",
        props -> new BaseBlockItem(FPipesBlocks.BASIC_ENERGY_PIPE.get(), props)
    );
    public static final DeferredItem<BaseBlockItem> IMPROVED_ENERGY_PIPE = ITEMS.registerItem(
        "improved_energy_pipe",
        props -> new BaseBlockItem(FPipesBlocks.IMPROVED_ENERGY_PIPE.get(), props)
    );
    public static final DeferredItem<BaseBlockItem> ADVANCED_ENERGY_PIPE = ITEMS.registerItem(
        "advanced_energy_pipe",
        props -> new BaseBlockItem(FPipesBlocks.ADVANCED_ENERGY_PIPE.get(), props)
    );
    public static final DeferredItem<BaseBlockItem> ELITE_ENERGY_PIPE = ITEMS.registerItem(
        "elite_energy_pipe",
        props -> new BaseBlockItem(FPipesBlocks.ELITE_ENERGY_PIPE.get(), props)
    );
    public static final DeferredItem<BaseBlockItem> ULTIMATE_ENERGY_PIPE = ITEMS.registerItem(
        "ultimate_energy_pipe",
        props -> new BaseBlockItem(FPipesBlocks.ULTIMATE_ENERGY_PIPE.get(), props)
    );

    // Tools
    public static final DeferredItem<WrenchItem> WRENCH = ITEMS.registerItem(
        "wrench",
        WrenchItem::new
    );

    // Attachment Items
    public static final DeferredItem<AttachmentItem> BASIC_EXTRACTOR_ATTACHMENT = ITEMS.registerItem(
        "basic_extractor_attachment",
        props -> new AttachmentItem(ExtractorAttachmentType.BASIC.getFactory(), props)
    );
    public static final DeferredItem<AttachmentItem> IMPROVED_EXTRACTOR_ATTACHMENT = ITEMS.registerItem(
        "improved_extractor_attachment",
        props -> new AttachmentItem(ExtractorAttachmentType.IMPROVED.getFactory(), props)
    );
    public static final DeferredItem<AttachmentItem> ADVANCED_EXTRACTOR_ATTACHMENT = ITEMS.registerItem(
        "advanced_extractor_attachment",
        props -> new AttachmentItem(ExtractorAttachmentType.ADVANCED.getFactory(), props)
    );
    public static final DeferredItem<AttachmentItem> ELITE_EXTRACTOR_ATTACHMENT = ITEMS.registerItem(
        "elite_extractor_attachment",
        props -> new AttachmentItem(ExtractorAttachmentType.ELITE.getFactory(), props)
    );
    public static final DeferredItem<AttachmentItem> ULTIMATE_EXTRACTOR_ATTACHMENT = ITEMS.registerItem(
        "ultimate_extractor_attachment",
        props -> new AttachmentItem(ExtractorAttachmentType.ULTIMATE.getFactory(), props)
    );

    public static final DeferredItem<AttachmentItem> BASIC_INSERTER_ATTACHMENT = ITEMS.registerItem(
        "basic_inserter_attachment",
        props -> new AttachmentItem(InserterAttachmentType.BASIC.getFactory(), props)
    );
    public static final DeferredItem<AttachmentItem> IMPROVED_INSERTER_ATTACHMENT = ITEMS.registerItem(
        "improved_inserter_attachment",
        props -> new AttachmentItem(InserterAttachmentType.IMPROVED.getFactory(), props)
    );
    public static final DeferredItem<AttachmentItem> ADVANCED_INSERTER_ATTACHMENT = ITEMS.registerItem(
        "advanced_inserter_attachment",
        props -> new AttachmentItem(InserterAttachmentType.ADVANCED.getFactory(), props)
    );
    public static final DeferredItem<AttachmentItem> ELITE_INSERTER_ATTACHMENT = ITEMS.registerItem(
        "elite_inserter_attachment",
        props -> new AttachmentItem(InserterAttachmentType.ELITE.getFactory(), props)
    );
    public static final DeferredItem<AttachmentItem> ULTIMATE_INSERTER_ATTACHMENT = ITEMS.registerItem(
        "ultimate_inserter_attachment",
        props -> new AttachmentItem(InserterAttachmentType.ULTIMATE.getFactory(), props)
    );

    // Void Attachment
    public static final DeferredItem<AttachmentItem> VOID_ATTACHMENT = ITEMS.registerItem(
        "void_attachment",
        props -> new AttachmentItem(VoidAttachmentType.INSTANCE.getFactory(), props)
    );

    // Sensor Attachment
    public static final DeferredItem<AttachmentItem> SENSOR_ATTACHMENT = ITEMS.registerItem(
        "sensor_attachment",
        props -> new AttachmentItem(SensorAttachmentType.INSTANCE.getFactory(), props)
    );

    // Terminal
    public static final DeferredItem<BaseBlockItem> TERMINAL = ITEMS.registerItem(
        "terminal",
        props -> new BaseBlockItem(FPipesBlocks.TERMINAL.get(), props)
    );

    // Tiered Barrels
    public static final DeferredItem<BaseBlockItem> OAK_BARREL = ITEMS.registerItem(
        "oak_barrel", props -> new BaseBlockItem(FPipesBlocks.OAK_BARREL.get(), props));
    public static final DeferredItem<BaseBlockItem> COPPER_BARREL = ITEMS.registerItem(
        "copper_barrel", props -> new BaseBlockItem(FPipesBlocks.COPPER_BARREL.get(), props));
    public static final DeferredItem<BaseBlockItem> IRON_BARREL = ITEMS.registerItem(
        "iron_barrel", props -> new BaseBlockItem(FPipesBlocks.IRON_BARREL.get(), props));
    public static final DeferredItem<BaseBlockItem> GOLD_BARREL = ITEMS.registerItem(
        "gold_barrel", props -> new BaseBlockItem(FPipesBlocks.GOLD_BARREL.get(), props));
    public static final DeferredItem<BaseBlockItem> DIAMOND_BARREL = ITEMS.registerItem(
        "diamond_barrel", props -> new BaseBlockItem(FPipesBlocks.DIAMOND_BARREL.get(), props));
    public static final DeferredItem<BaseBlockItem> NETHERITE_BARREL = ITEMS.registerItem(
        "netherite_barrel", props -> new BaseBlockItem(FPipesBlocks.NETHERITE_BARREL.get(), props));

    // Barrel Upgrades
    public static final DeferredItem<BarrelUpgradeItem> COPPER_BARREL_UPGRADE = ITEMS.registerItem(
        "copper_barrel_upgrade", props -> new BarrelUpgradeItem(BarrelTier.COPPER, props));
    public static final DeferredItem<BarrelUpgradeItem> IRON_BARREL_UPGRADE = ITEMS.registerItem(
        "iron_barrel_upgrade", props -> new BarrelUpgradeItem(BarrelTier.IRON, props));
    public static final DeferredItem<BarrelUpgradeItem> GOLD_BARREL_UPGRADE = ITEMS.registerItem(
        "gold_barrel_upgrade", props -> new BarrelUpgradeItem(BarrelTier.GOLD, props));
    public static final DeferredItem<BarrelUpgradeItem> DIAMOND_BARREL_UPGRADE = ITEMS.registerItem(
        "diamond_barrel_upgrade", props -> new BarrelUpgradeItem(BarrelTier.DIAMOND, props));
    public static final DeferredItem<BarrelUpgradeItem> NETHERITE_BARREL_UPGRADE = ITEMS.registerItem(
        "netherite_barrel_upgrade", props -> new BarrelUpgradeItem(BarrelTier.NETHERITE, props));
}

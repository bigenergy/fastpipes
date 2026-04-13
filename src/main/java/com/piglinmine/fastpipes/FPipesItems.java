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
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class FPipesItems {
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, FastPipes.MOD_ID);

    // Block Items for Pipes
    public static final RegistryObject<BaseBlockItem> BASIC_ITEM_PIPE = ITEMS.register(
        "basic_item_pipe", 
        () -> new BaseBlockItem(FPipesBlocks.BASIC_ITEM_PIPE.get())
    );
    public static final RegistryObject<BaseBlockItem> IMPROVED_ITEM_PIPE = ITEMS.register(
        "improved_item_pipe", 
        () -> new BaseBlockItem(FPipesBlocks.IMPROVED_ITEM_PIPE.get())
    );
    public static final RegistryObject<BaseBlockItem> ADVANCED_ITEM_PIPE = ITEMS.register(
        "advanced_item_pipe", 
        () -> new BaseBlockItem(FPipesBlocks.ADVANCED_ITEM_PIPE.get())
    );

    public static final RegistryObject<BaseBlockItem> BASIC_FLUID_PIPE = ITEMS.register(
        "basic_fluid_pipe", 
        () -> new BaseBlockItem(FPipesBlocks.BASIC_FLUID_PIPE.get())
    );
    public static final RegistryObject<BaseBlockItem> IMPROVED_FLUID_PIPE = ITEMS.register(
        "improved_fluid_pipe", 
        () -> new BaseBlockItem(FPipesBlocks.IMPROVED_FLUID_PIPE.get())
    );
    public static final RegistryObject<BaseBlockItem> ADVANCED_FLUID_PIPE = ITEMS.register(
        "advanced_fluid_pipe", 
        () -> new BaseBlockItem(FPipesBlocks.ADVANCED_FLUID_PIPE.get())
    );
    public static final RegistryObject<BaseBlockItem> ELITE_FLUID_PIPE = ITEMS.register(
        "elite_fluid_pipe", 
        () -> new BaseBlockItem(FPipesBlocks.ELITE_FLUID_PIPE.get())
    );
    public static final RegistryObject<BaseBlockItem> ULTIMATE_FLUID_PIPE = ITEMS.register(
        "ultimate_fluid_pipe", 
        () -> new BaseBlockItem(FPipesBlocks.ULTIMATE_FLUID_PIPE.get())
    );

    public static final RegistryObject<BaseBlockItem> BASIC_ENERGY_PIPE = ITEMS.register(
        "basic_energy_pipe", 
        () -> new BaseBlockItem(FPipesBlocks.BASIC_ENERGY_PIPE.get())
    );
    public static final RegistryObject<BaseBlockItem> IMPROVED_ENERGY_PIPE = ITEMS.register(
        "improved_energy_pipe", 
        () -> new BaseBlockItem(FPipesBlocks.IMPROVED_ENERGY_PIPE.get())
    );
    public static final RegistryObject<BaseBlockItem> ADVANCED_ENERGY_PIPE = ITEMS.register(
        "advanced_energy_pipe", 
        () -> new BaseBlockItem(FPipesBlocks.ADVANCED_ENERGY_PIPE.get())
    );
    public static final RegistryObject<BaseBlockItem> ELITE_ENERGY_PIPE = ITEMS.register(
        "elite_energy_pipe", 
        () -> new BaseBlockItem(FPipesBlocks.ELITE_ENERGY_PIPE.get())
    );
    public static final RegistryObject<BaseBlockItem> ULTIMATE_ENERGY_PIPE = ITEMS.register(
        "ultimate_energy_pipe", 
        () -> new BaseBlockItem(FPipesBlocks.ULTIMATE_ENERGY_PIPE.get())
    );

    // Tools
    public static final RegistryObject<WrenchItem> WRENCH = ITEMS.register(
        "wrench",
        () -> new WrenchItem()
    );

    // Attachment Items
    public static final RegistryObject<AttachmentItem> BASIC_EXTRACTOR_ATTACHMENT = ITEMS.register(
        "basic_extractor_attachment", 
        () -> new AttachmentItem(ExtractorAttachmentType.BASIC.getFactory())
    );
    public static final RegistryObject<AttachmentItem> IMPROVED_EXTRACTOR_ATTACHMENT = ITEMS.register(
        "improved_extractor_attachment", 
        () -> new AttachmentItem(ExtractorAttachmentType.IMPROVED.getFactory())
    );
    public static final RegistryObject<AttachmentItem> ADVANCED_EXTRACTOR_ATTACHMENT = ITEMS.register(
        "advanced_extractor_attachment", 
        () -> new AttachmentItem(ExtractorAttachmentType.ADVANCED.getFactory())
    );
    public static final RegistryObject<AttachmentItem> ELITE_EXTRACTOR_ATTACHMENT = ITEMS.register(
        "elite_extractor_attachment", 
        () -> new AttachmentItem(ExtractorAttachmentType.ELITE.getFactory())
    );
    public static final RegistryObject<AttachmentItem> ULTIMATE_EXTRACTOR_ATTACHMENT = ITEMS.register(
        "ultimate_extractor_attachment",
        () -> new AttachmentItem(ExtractorAttachmentType.ULTIMATE.getFactory())
    );

    public static final RegistryObject<AttachmentItem> BASIC_INSERTER_ATTACHMENT = ITEMS.register(
        "basic_inserter_attachment",
        () -> new AttachmentItem(InserterAttachmentType.BASIC.getFactory())
    );
    public static final RegistryObject<AttachmentItem> IMPROVED_INSERTER_ATTACHMENT = ITEMS.register(
        "improved_inserter_attachment",
        () -> new AttachmentItem(InserterAttachmentType.IMPROVED.getFactory())
    );
    public static final RegistryObject<AttachmentItem> ADVANCED_INSERTER_ATTACHMENT = ITEMS.register(
        "advanced_inserter_attachment",
        () -> new AttachmentItem(InserterAttachmentType.ADVANCED.getFactory())
    );
    public static final RegistryObject<AttachmentItem> ELITE_INSERTER_ATTACHMENT = ITEMS.register(
        "elite_inserter_attachment",
        () -> new AttachmentItem(InserterAttachmentType.ELITE.getFactory())
    );
    public static final RegistryObject<AttachmentItem> ULTIMATE_INSERTER_ATTACHMENT = ITEMS.register(
        "ultimate_inserter_attachment",
        () -> new AttachmentItem(InserterAttachmentType.ULTIMATE.getFactory())
    );

    // Void Attachment
    public static final RegistryObject<AttachmentItem> VOID_ATTACHMENT = ITEMS.register(
        "void_attachment",
        () -> new AttachmentItem(VoidAttachmentType.INSTANCE.getFactory())
    );

    // Sensor Attachment
    public static final RegistryObject<AttachmentItem> SENSOR_ATTACHMENT = ITEMS.register(
        "sensor_attachment",
        () -> new AttachmentItem(SensorAttachmentType.INSTANCE.getFactory())
    );

    // Terminal
    public static final RegistryObject<BaseBlockItem> TERMINAL = ITEMS.register(
        "terminal",
        () -> new BaseBlockItem(FPipesBlocks.TERMINAL.get())
    );

    // Tiered Barrels
    public static final RegistryObject<BaseBlockItem> OAK_BARREL = ITEMS.register(
        "oak_barrel", () -> new BaseBlockItem(FPipesBlocks.OAK_BARREL.get()));
    public static final RegistryObject<BaseBlockItem> COPPER_BARREL = ITEMS.register(
        "copper_barrel", () -> new BaseBlockItem(FPipesBlocks.COPPER_BARREL.get()));
    public static final RegistryObject<BaseBlockItem> IRON_BARREL = ITEMS.register(
        "iron_barrel", () -> new BaseBlockItem(FPipesBlocks.IRON_BARREL.get()));
    public static final RegistryObject<BaseBlockItem> GOLD_BARREL = ITEMS.register(
        "gold_barrel", () -> new BaseBlockItem(FPipesBlocks.GOLD_BARREL.get()));
    public static final RegistryObject<BaseBlockItem> DIAMOND_BARREL = ITEMS.register(
        "diamond_barrel", () -> new BaseBlockItem(FPipesBlocks.DIAMOND_BARREL.get()));
    public static final RegistryObject<BaseBlockItem> NETHERITE_BARREL = ITEMS.register(
        "netherite_barrel", () -> new BaseBlockItem(FPipesBlocks.NETHERITE_BARREL.get()));

    // Barrel Upgrades
    public static final RegistryObject<BarrelUpgradeItem> COPPER_BARREL_UPGRADE = ITEMS.register(
        "copper_barrel_upgrade", () -> new BarrelUpgradeItem(BarrelTier.COPPER));
    public static final RegistryObject<BarrelUpgradeItem> IRON_BARREL_UPGRADE = ITEMS.register(
        "iron_barrel_upgrade", () -> new BarrelUpgradeItem(BarrelTier.IRON));
    public static final RegistryObject<BarrelUpgradeItem> GOLD_BARREL_UPGRADE = ITEMS.register(
        "gold_barrel_upgrade", () -> new BarrelUpgradeItem(BarrelTier.GOLD));
    public static final RegistryObject<BarrelUpgradeItem> DIAMOND_BARREL_UPGRADE = ITEMS.register(
        "diamond_barrel_upgrade", () -> new BarrelUpgradeItem(BarrelTier.DIAMOND));
    public static final RegistryObject<BarrelUpgradeItem> NETHERITE_BARREL_UPGRADE = ITEMS.register(
        "netherite_barrel_upgrade", () -> new BarrelUpgradeItem(BarrelTier.NETHERITE));
} 
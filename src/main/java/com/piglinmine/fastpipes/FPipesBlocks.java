package com.piglinmine.fastpipes;

import com.piglinmine.fastpipes.barrel.BarrelTier;
import com.piglinmine.fastpipes.barrel.TieredBarrelBlock;
import com.piglinmine.fastpipes.block.EnergyPipeBlock;
import com.piglinmine.fastpipes.block.FluidPipeBlock;
import com.piglinmine.fastpipes.block.ItemPipeBlock;
import com.piglinmine.fastpipes.block.TerminalBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import com.piglinmine.fastpipes.network.pipe.energy.EnergyPipeType;
import com.piglinmine.fastpipes.network.pipe.fluid.FluidPipeType;
import com.piglinmine.fastpipes.network.pipe.item.ItemPipeType;
import com.piglinmine.fastpipes.network.pipe.shape.PipeShapeCache;
import com.piglinmine.fastpipes.network.pipe.shape.PipeShapeFactory;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;

public class FPipesBlocks {
    private static final PipeShapeCache PIPE_SHAPE_CACHE = new PipeShapeCache(new PipeShapeFactory());

    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(FastPipes.MOD_ID);

    // 1.21.11: Use registerBlock(name, factory, propertiesSupplier) so DeferredRegister
    // calls Properties.setId(...) with the block's ResourceKey before construction.
    // The bare BLOCKS.register(name, () -> new Block(Properties.of())) path leaves the id
    // unset and crashes in BlockBehaviour$Properties.effectiveDrops with "Block id not set".

    // Item Pipes
    public static final DeferredBlock<ItemPipeBlock> BASIC_ITEM_PIPE = BLOCKS.registerBlock(
        "basic_item_pipe",
        props -> new ItemPipeBlock(PIPE_SHAPE_CACHE, ItemPipeType.BASIC, props),
        () -> BlockBehaviour.Properties.of()
    );
    public static final DeferredBlock<ItemPipeBlock> IMPROVED_ITEM_PIPE = BLOCKS.registerBlock(
        "improved_item_pipe",
        props -> new ItemPipeBlock(PIPE_SHAPE_CACHE, ItemPipeType.IMPROVED, props),
        () -> BlockBehaviour.Properties.of()
    );
    public static final DeferredBlock<ItemPipeBlock> ADVANCED_ITEM_PIPE = BLOCKS.registerBlock(
        "advanced_item_pipe",
        props -> new ItemPipeBlock(PIPE_SHAPE_CACHE, ItemPipeType.ADVANCED, props),
        () -> BlockBehaviour.Properties.of()
    );

    // Fluid Pipes
    public static final DeferredBlock<FluidPipeBlock> BASIC_FLUID_PIPE = BLOCKS.registerBlock(
        "basic_fluid_pipe",
        props -> new FluidPipeBlock(PIPE_SHAPE_CACHE, FluidPipeType.BASIC, props),
        () -> BlockBehaviour.Properties.of()
    );
    public static final DeferredBlock<FluidPipeBlock> IMPROVED_FLUID_PIPE = BLOCKS.registerBlock(
        "improved_fluid_pipe",
        props -> new FluidPipeBlock(PIPE_SHAPE_CACHE, FluidPipeType.IMPROVED, props),
        () -> BlockBehaviour.Properties.of()
    );
    public static final DeferredBlock<FluidPipeBlock> ADVANCED_FLUID_PIPE = BLOCKS.registerBlock(
        "advanced_fluid_pipe",
        props -> new FluidPipeBlock(PIPE_SHAPE_CACHE, FluidPipeType.ADVANCED, props),
        () -> BlockBehaviour.Properties.of()
    );
    public static final DeferredBlock<FluidPipeBlock> ELITE_FLUID_PIPE = BLOCKS.registerBlock(
        "elite_fluid_pipe",
        props -> new FluidPipeBlock(PIPE_SHAPE_CACHE, FluidPipeType.ELITE, props),
        () -> BlockBehaviour.Properties.of()
    );
    public static final DeferredBlock<FluidPipeBlock> ULTIMATE_FLUID_PIPE = BLOCKS.registerBlock(
        "ultimate_fluid_pipe",
        props -> new FluidPipeBlock(PIPE_SHAPE_CACHE, FluidPipeType.ULTIMATE, props),
        () -> BlockBehaviour.Properties.of()
    );

    // Energy Pipes
    public static final DeferredBlock<EnergyPipeBlock> BASIC_ENERGY_PIPE = BLOCKS.registerBlock(
        "basic_energy_pipe",
        props -> new EnergyPipeBlock(PIPE_SHAPE_CACHE, EnergyPipeType.BASIC, props),
        () -> BlockBehaviour.Properties.of()
    );
    public static final DeferredBlock<EnergyPipeBlock> IMPROVED_ENERGY_PIPE = BLOCKS.registerBlock(
        "improved_energy_pipe",
        props -> new EnergyPipeBlock(PIPE_SHAPE_CACHE, EnergyPipeType.IMPROVED, props),
        () -> BlockBehaviour.Properties.of()
    );
    public static final DeferredBlock<EnergyPipeBlock> ADVANCED_ENERGY_PIPE = BLOCKS.registerBlock(
        "advanced_energy_pipe",
        props -> new EnergyPipeBlock(PIPE_SHAPE_CACHE, EnergyPipeType.ADVANCED, props),
        () -> BlockBehaviour.Properties.of()
    );
    public static final DeferredBlock<EnergyPipeBlock> ELITE_ENERGY_PIPE = BLOCKS.registerBlock(
        "elite_energy_pipe",
        props -> new EnergyPipeBlock(PIPE_SHAPE_CACHE, EnergyPipeType.ELITE, props),
        () -> BlockBehaviour.Properties.of()
    );
    public static final DeferredBlock<EnergyPipeBlock> ULTIMATE_ENERGY_PIPE = BLOCKS.registerBlock(
        "ultimate_energy_pipe",
        props -> new EnergyPipeBlock(PIPE_SHAPE_CACHE, EnergyPipeType.ULTIMATE, props),
        () -> BlockBehaviour.Properties.of()
    );

    // Terminal
    public static final DeferredBlock<TerminalBlock> TERMINAL = BLOCKS.registerBlock(
        "terminal",
        TerminalBlock::new,
        () -> BlockBehaviour.Properties.of()
    );

    // Tiered Barrels
    public static final DeferredBlock<TieredBarrelBlock> OAK_BARREL = BLOCKS.registerBlock(
        "oak_barrel", props -> new TieredBarrelBlock(BarrelTier.OAK, props), () -> BlockBehaviour.Properties.of());
    public static final DeferredBlock<TieredBarrelBlock> COPPER_BARREL = BLOCKS.registerBlock(
        "copper_barrel", props -> new TieredBarrelBlock(BarrelTier.COPPER, props), () -> BlockBehaviour.Properties.of());
    public static final DeferredBlock<TieredBarrelBlock> IRON_BARREL = BLOCKS.registerBlock(
        "iron_barrel", props -> new TieredBarrelBlock(BarrelTier.IRON, props), () -> BlockBehaviour.Properties.of());
    public static final DeferredBlock<TieredBarrelBlock> GOLD_BARREL = BLOCKS.registerBlock(
        "gold_barrel", props -> new TieredBarrelBlock(BarrelTier.GOLD, props), () -> BlockBehaviour.Properties.of());
    public static final DeferredBlock<TieredBarrelBlock> DIAMOND_BARREL = BLOCKS.registerBlock(
        "diamond_barrel", props -> new TieredBarrelBlock(BarrelTier.DIAMOND, props), () -> BlockBehaviour.Properties.of());
    public static final DeferredBlock<TieredBarrelBlock> NETHERITE_BARREL = BLOCKS.registerBlock(
        "netherite_barrel", props -> new TieredBarrelBlock(BarrelTier.NETHERITE, props), () -> BlockBehaviour.Properties.of());

    public static Block getBarrelBlock(BarrelTier tier) {
        return switch (tier) {
            case OAK -> OAK_BARREL.get();
            case COPPER -> COPPER_BARREL.get();
            case IRON -> IRON_BARREL.get();
            case GOLD -> GOLD_BARREL.get();
            case DIAMOND -> DIAMOND_BARREL.get();
            case NETHERITE -> NETHERITE_BARREL.get();
        };
    }
}

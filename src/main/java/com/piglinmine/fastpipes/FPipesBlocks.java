package com.piglinmine.fastpipes;

import com.piglinmine.fastpipes.block.EnergyPipeBlock;
import com.piglinmine.fastpipes.block.FluidPipeBlock;
import com.piglinmine.fastpipes.block.ItemPipeBlock;
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

    // Item Pipes
    public static final DeferredBlock<ItemPipeBlock> BASIC_ITEM_PIPE = BLOCKS.register(
        "basic_item_pipe", 
        () -> new ItemPipeBlock(PIPE_SHAPE_CACHE, ItemPipeType.BASIC)
    );
    public static final DeferredBlock<ItemPipeBlock> IMPROVED_ITEM_PIPE = BLOCKS.register(
        "improved_item_pipe", 
        () -> new ItemPipeBlock(PIPE_SHAPE_CACHE, ItemPipeType.IMPROVED)
    );
    public static final DeferredBlock<ItemPipeBlock> ADVANCED_ITEM_PIPE = BLOCKS.register(
        "advanced_item_pipe", 
        () -> new ItemPipeBlock(PIPE_SHAPE_CACHE, ItemPipeType.ADVANCED)
    );

    // Fluid Pipes
    public static final DeferredBlock<FluidPipeBlock> BASIC_FLUID_PIPE = BLOCKS.register(
        "basic_fluid_pipe", 
        () -> new FluidPipeBlock(PIPE_SHAPE_CACHE, FluidPipeType.BASIC)
    );
    public static final DeferredBlock<FluidPipeBlock> IMPROVED_FLUID_PIPE = BLOCKS.register(
        "improved_fluid_pipe", 
        () -> new FluidPipeBlock(PIPE_SHAPE_CACHE, FluidPipeType.IMPROVED)
    );
    public static final DeferredBlock<FluidPipeBlock> ADVANCED_FLUID_PIPE = BLOCKS.register(
        "advanced_fluid_pipe", 
        () -> new FluidPipeBlock(PIPE_SHAPE_CACHE, FluidPipeType.ADVANCED)
    );
    public static final DeferredBlock<FluidPipeBlock> ELITE_FLUID_PIPE = BLOCKS.register(
        "elite_fluid_pipe", 
        () -> new FluidPipeBlock(PIPE_SHAPE_CACHE, FluidPipeType.ELITE)
    );
    public static final DeferredBlock<FluidPipeBlock> ULTIMATE_FLUID_PIPE = BLOCKS.register(
        "ultimate_fluid_pipe", 
        () -> new FluidPipeBlock(PIPE_SHAPE_CACHE, FluidPipeType.ULTIMATE)
    );

    // Energy Pipes
    public static final DeferredBlock<EnergyPipeBlock> BASIC_ENERGY_PIPE = BLOCKS.register(
        "basic_energy_pipe", 
        () -> new EnergyPipeBlock(PIPE_SHAPE_CACHE, EnergyPipeType.BASIC)
    );
    public static final DeferredBlock<EnergyPipeBlock> IMPROVED_ENERGY_PIPE = BLOCKS.register(
        "improved_energy_pipe", 
        () -> new EnergyPipeBlock(PIPE_SHAPE_CACHE, EnergyPipeType.IMPROVED)
    );
    public static final DeferredBlock<EnergyPipeBlock> ADVANCED_ENERGY_PIPE = BLOCKS.register(
        "advanced_energy_pipe", 
        () -> new EnergyPipeBlock(PIPE_SHAPE_CACHE, EnergyPipeType.ADVANCED)
    );
    public static final DeferredBlock<EnergyPipeBlock> ELITE_ENERGY_PIPE = BLOCKS.register(
        "elite_energy_pipe", 
        () -> new EnergyPipeBlock(PIPE_SHAPE_CACHE, EnergyPipeType.ELITE)
    );
    public static final DeferredBlock<EnergyPipeBlock> ULTIMATE_ENERGY_PIPE = BLOCKS.register(
        "ultimate_energy_pipe", 
        () -> new EnergyPipeBlock(PIPE_SHAPE_CACHE, EnergyPipeType.ULTIMATE)
    );
} 
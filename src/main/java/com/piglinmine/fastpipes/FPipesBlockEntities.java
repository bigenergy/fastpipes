package com.piglinmine.fastpipes;

import com.piglinmine.fastpipes.blockentity.EnergyPipeBlockEntity;
import com.piglinmine.fastpipes.blockentity.FluidPipeBlockEntity;
import com.piglinmine.fastpipes.blockentity.ItemPipeBlockEntity;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class FPipesBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, FastPipes.MOD_ID);

    // Item Pipe Block Entities
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<ItemPipeBlockEntity>> BASIC_ITEM_PIPE = BLOCK_ENTITIES.register(
        "basic_item_pipe",
        () -> BlockEntityType.Builder.of(
            ItemPipeBlockEntity::new,
            FPipesBlocks.BASIC_ITEM_PIPE.get()
        ).build(null)
    );
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<ItemPipeBlockEntity>> IMPROVED_ITEM_PIPE = BLOCK_ENTITIES.register(
        "improved_item_pipe",
        () -> BlockEntityType.Builder.of(
            ItemPipeBlockEntity::new,
            FPipesBlocks.IMPROVED_ITEM_PIPE.get()
        ).build(null)
    );
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<ItemPipeBlockEntity>> ADVANCED_ITEM_PIPE = BLOCK_ENTITIES.register(
        "advanced_item_pipe",
        () -> BlockEntityType.Builder.of(
            ItemPipeBlockEntity::new,
            FPipesBlocks.ADVANCED_ITEM_PIPE.get()
        ).build(null)
    );

    // Fluid Pipe Block Entities
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<FluidPipeBlockEntity>> BASIC_FLUID_PIPE = BLOCK_ENTITIES.register(
        "basic_fluid_pipe",
        () -> BlockEntityType.Builder.of(
            FluidPipeBlockEntity::new,
            FPipesBlocks.BASIC_FLUID_PIPE.get()
        ).build(null)
    );
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<FluidPipeBlockEntity>> IMPROVED_FLUID_PIPE = BLOCK_ENTITIES.register(
        "improved_fluid_pipe",
        () -> BlockEntityType.Builder.of(
            FluidPipeBlockEntity::new,
            FPipesBlocks.IMPROVED_FLUID_PIPE.get()
        ).build(null)
    );
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<FluidPipeBlockEntity>> ADVANCED_FLUID_PIPE = BLOCK_ENTITIES.register(
        "advanced_fluid_pipe",
        () -> BlockEntityType.Builder.of(
            FluidPipeBlockEntity::new,
            FPipesBlocks.ADVANCED_FLUID_PIPE.get()
        ).build(null)
    );
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<FluidPipeBlockEntity>> ELITE_FLUID_PIPE = BLOCK_ENTITIES.register(
        "elite_fluid_pipe",
        () -> BlockEntityType.Builder.of(
            FluidPipeBlockEntity::new,
            FPipesBlocks.ELITE_FLUID_PIPE.get()
        ).build(null)
    );
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<FluidPipeBlockEntity>> ULTIMATE_FLUID_PIPE = BLOCK_ENTITIES.register(
        "ultimate_fluid_pipe",
        () -> BlockEntityType.Builder.of(
            FluidPipeBlockEntity::new,
            FPipesBlocks.ULTIMATE_FLUID_PIPE.get()
        ).build(null)
    );

    // Energy Pipe Block Entities
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<EnergyPipeBlockEntity>> BASIC_ENERGY_PIPE = BLOCK_ENTITIES.register(
        "basic_energy_pipe",
        () -> BlockEntityType.Builder.of(
            EnergyPipeBlockEntity::new,
            FPipesBlocks.BASIC_ENERGY_PIPE.get()
        ).build(null)
    );
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<EnergyPipeBlockEntity>> IMPROVED_ENERGY_PIPE = BLOCK_ENTITIES.register(
        "improved_energy_pipe",
        () -> BlockEntityType.Builder.of(
            EnergyPipeBlockEntity::new,
            FPipesBlocks.IMPROVED_ENERGY_PIPE.get()
        ).build(null)
    );
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<EnergyPipeBlockEntity>> ADVANCED_ENERGY_PIPE = BLOCK_ENTITIES.register(
        "advanced_energy_pipe",
        () -> BlockEntityType.Builder.of(
            EnergyPipeBlockEntity::new,
            FPipesBlocks.ADVANCED_ENERGY_PIPE.get()
        ).build(null)
    );
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<EnergyPipeBlockEntity>> ELITE_ENERGY_PIPE = BLOCK_ENTITIES.register(
        "elite_energy_pipe",
        () -> BlockEntityType.Builder.of(
            EnergyPipeBlockEntity::new,
            FPipesBlocks.ELITE_ENERGY_PIPE.get()
        ).build(null)
    );
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<EnergyPipeBlockEntity>> ULTIMATE_ENERGY_PIPE = BLOCK_ENTITIES.register(
        "ultimate_energy_pipe",
        () -> BlockEntityType.Builder.of(
            EnergyPipeBlockEntity::new,
            FPipesBlocks.ULTIMATE_ENERGY_PIPE.get()
        ).build(null)
    );
} 
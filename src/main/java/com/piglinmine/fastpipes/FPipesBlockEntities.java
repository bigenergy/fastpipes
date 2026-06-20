package com.piglinmine.fastpipes;

import com.piglinmine.fastpipes.barrel.TieredBarrelBlockEntity;
import com.piglinmine.fastpipes.blockentity.EnergyPipeBlockEntity;
import com.piglinmine.fastpipes.blockentity.FluidPipeBlockEntity;
import com.piglinmine.fastpipes.blockentity.ItemPipeBlockEntity;
import com.piglinmine.fastpipes.blockentity.TerminalBlockEntity;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class FPipesBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, FastPipes.MOD_ID);

    // Item Pipe Block Entities
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<ItemPipeBlockEntity>> BASIC_ITEM_PIPE = BLOCK_ENTITIES.register(
        "basic_item_pipe",
        () -> new BlockEntityType<>(
            ItemPipeBlockEntity::new,
            FPipesBlocks.BASIC_ITEM_PIPE.get()
        )
    );
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<ItemPipeBlockEntity>> IMPROVED_ITEM_PIPE = BLOCK_ENTITIES.register(
        "improved_item_pipe",
        () -> new BlockEntityType<>(
            ItemPipeBlockEntity::new,
            FPipesBlocks.IMPROVED_ITEM_PIPE.get()
        )
    );
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<ItemPipeBlockEntity>> ADVANCED_ITEM_PIPE = BLOCK_ENTITIES.register(
        "advanced_item_pipe",
        () -> new BlockEntityType<>(
            ItemPipeBlockEntity::new,
            FPipesBlocks.ADVANCED_ITEM_PIPE.get()
        )
    );

    // Fluid Pipe Block Entities
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<FluidPipeBlockEntity>> BASIC_FLUID_PIPE = BLOCK_ENTITIES.register(
        "basic_fluid_pipe",
        () -> new BlockEntityType<>(
            FluidPipeBlockEntity::new,
            FPipesBlocks.BASIC_FLUID_PIPE.get()
        )
    );
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<FluidPipeBlockEntity>> IMPROVED_FLUID_PIPE = BLOCK_ENTITIES.register(
        "improved_fluid_pipe",
        () -> new BlockEntityType<>(
            FluidPipeBlockEntity::new,
            FPipesBlocks.IMPROVED_FLUID_PIPE.get()
        )
    );
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<FluidPipeBlockEntity>> ADVANCED_FLUID_PIPE = BLOCK_ENTITIES.register(
        "advanced_fluid_pipe",
        () -> new BlockEntityType<>(
            FluidPipeBlockEntity::new,
            FPipesBlocks.ADVANCED_FLUID_PIPE.get()
        )
    );
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<FluidPipeBlockEntity>> ELITE_FLUID_PIPE = BLOCK_ENTITIES.register(
        "elite_fluid_pipe",
        () -> new BlockEntityType<>(
            FluidPipeBlockEntity::new,
            FPipesBlocks.ELITE_FLUID_PIPE.get()
        )
    );
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<FluidPipeBlockEntity>> ULTIMATE_FLUID_PIPE = BLOCK_ENTITIES.register(
        "ultimate_fluid_pipe",
        () -> new BlockEntityType<>(
            FluidPipeBlockEntity::new,
            FPipesBlocks.ULTIMATE_FLUID_PIPE.get()
        )
    );

    // Energy Pipe Block Entities
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<EnergyPipeBlockEntity>> BASIC_ENERGY_PIPE = BLOCK_ENTITIES.register(
        "basic_energy_pipe",
        () -> new BlockEntityType<>(
            EnergyPipeBlockEntity::new,
            FPipesBlocks.BASIC_ENERGY_PIPE.get()
        )
    );
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<EnergyPipeBlockEntity>> IMPROVED_ENERGY_PIPE = BLOCK_ENTITIES.register(
        "improved_energy_pipe",
        () -> new BlockEntityType<>(
            EnergyPipeBlockEntity::new,
            FPipesBlocks.IMPROVED_ENERGY_PIPE.get()
        )
    );
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<EnergyPipeBlockEntity>> ADVANCED_ENERGY_PIPE = BLOCK_ENTITIES.register(
        "advanced_energy_pipe",
        () -> new BlockEntityType<>(
            EnergyPipeBlockEntity::new,
            FPipesBlocks.ADVANCED_ENERGY_PIPE.get()
        )
    );
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<EnergyPipeBlockEntity>> ELITE_ENERGY_PIPE = BLOCK_ENTITIES.register(
        "elite_energy_pipe",
        () -> new BlockEntityType<>(
            EnergyPipeBlockEntity::new,
            FPipesBlocks.ELITE_ENERGY_PIPE.get()
        )
    );
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<EnergyPipeBlockEntity>> ULTIMATE_ENERGY_PIPE = BLOCK_ENTITIES.register(
        "ultimate_energy_pipe",
        () -> new BlockEntityType<>(
            EnergyPipeBlockEntity::new,
            FPipesBlocks.ULTIMATE_ENERGY_PIPE.get()
        )
    );

    // Terminal
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<TerminalBlockEntity>> TERMINAL = BLOCK_ENTITIES.register(
        "terminal",
        () -> new BlockEntityType<>(
            TerminalBlockEntity::new,
            FPipesBlocks.TERMINAL.get()
        )
    );

    // Tiered Barrel (one BE type for all barrel blocks)
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<TieredBarrelBlockEntity>> TIERED_BARREL = BLOCK_ENTITIES.register(
        "tiered_barrel",
        () -> new BlockEntityType<>(
            TieredBarrelBlockEntity::new,
            FPipesBlocks.OAK_BARREL.get(),
            FPipesBlocks.COPPER_BARREL.get(),
            FPipesBlocks.IRON_BARREL.get(),
            FPipesBlocks.GOLD_BARREL.get(),
            FPipesBlocks.DIAMOND_BARREL.get(),
            FPipesBlocks.NETHERITE_BARREL.get()
        )
    );
} 
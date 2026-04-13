package com.piglinmine.fastpipes;

import com.piglinmine.fastpipes.barrel.TieredBarrelBlockEntity;
import com.piglinmine.fastpipes.blockentity.EnergyPipeBlockEntity;
import com.piglinmine.fastpipes.blockentity.FluidPipeBlockEntity;
import com.piglinmine.fastpipes.blockentity.ItemPipeBlockEntity;
import com.piglinmine.fastpipes.blockentity.TerminalBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class FPipesBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, FastPipes.MOD_ID);

    // Item Pipe Block Entities
    public static final RegistryObject<BlockEntityType<ItemPipeBlockEntity>> BASIC_ITEM_PIPE = BLOCK_ENTITIES.register(
        "basic_item_pipe",
        () -> BlockEntityType.Builder.of(
            ItemPipeBlockEntity::new,
            FPipesBlocks.BASIC_ITEM_PIPE.get()
        ).build(null)
    );
    public static final RegistryObject<BlockEntityType<ItemPipeBlockEntity>> IMPROVED_ITEM_PIPE = BLOCK_ENTITIES.register(
        "improved_item_pipe",
        () -> BlockEntityType.Builder.of(
            ItemPipeBlockEntity::new,
            FPipesBlocks.IMPROVED_ITEM_PIPE.get()
        ).build(null)
    );
    public static final RegistryObject<BlockEntityType<ItemPipeBlockEntity>> ADVANCED_ITEM_PIPE = BLOCK_ENTITIES.register(
        "advanced_item_pipe",
        () -> BlockEntityType.Builder.of(
            ItemPipeBlockEntity::new,
            FPipesBlocks.ADVANCED_ITEM_PIPE.get()
        ).build(null)
    );

    // Fluid Pipe Block Entities
    public static final RegistryObject<BlockEntityType<FluidPipeBlockEntity>> BASIC_FLUID_PIPE = BLOCK_ENTITIES.register(
        "basic_fluid_pipe",
        () -> BlockEntityType.Builder.of(
            FluidPipeBlockEntity::new,
            FPipesBlocks.BASIC_FLUID_PIPE.get()
        ).build(null)
    );
    public static final RegistryObject<BlockEntityType<FluidPipeBlockEntity>> IMPROVED_FLUID_PIPE = BLOCK_ENTITIES.register(
        "improved_fluid_pipe",
        () -> BlockEntityType.Builder.of(
            FluidPipeBlockEntity::new,
            FPipesBlocks.IMPROVED_FLUID_PIPE.get()
        ).build(null)
    );
    public static final RegistryObject<BlockEntityType<FluidPipeBlockEntity>> ADVANCED_FLUID_PIPE = BLOCK_ENTITIES.register(
        "advanced_fluid_pipe",
        () -> BlockEntityType.Builder.of(
            FluidPipeBlockEntity::new,
            FPipesBlocks.ADVANCED_FLUID_PIPE.get()
        ).build(null)
    );
    public static final RegistryObject<BlockEntityType<FluidPipeBlockEntity>> ELITE_FLUID_PIPE = BLOCK_ENTITIES.register(
        "elite_fluid_pipe",
        () -> BlockEntityType.Builder.of(
            FluidPipeBlockEntity::new,
            FPipesBlocks.ELITE_FLUID_PIPE.get()
        ).build(null)
    );
    public static final RegistryObject<BlockEntityType<FluidPipeBlockEntity>> ULTIMATE_FLUID_PIPE = BLOCK_ENTITIES.register(
        "ultimate_fluid_pipe",
        () -> BlockEntityType.Builder.of(
            FluidPipeBlockEntity::new,
            FPipesBlocks.ULTIMATE_FLUID_PIPE.get()
        ).build(null)
    );

    // Energy Pipe Block Entities
    public static final RegistryObject<BlockEntityType<EnergyPipeBlockEntity>> BASIC_ENERGY_PIPE = BLOCK_ENTITIES.register(
        "basic_energy_pipe",
        () -> BlockEntityType.Builder.of(
            EnergyPipeBlockEntity::new,
            FPipesBlocks.BASIC_ENERGY_PIPE.get()
        ).build(null)
    );
    public static final RegistryObject<BlockEntityType<EnergyPipeBlockEntity>> IMPROVED_ENERGY_PIPE = BLOCK_ENTITIES.register(
        "improved_energy_pipe",
        () -> BlockEntityType.Builder.of(
            EnergyPipeBlockEntity::new,
            FPipesBlocks.IMPROVED_ENERGY_PIPE.get()
        ).build(null)
    );
    public static final RegistryObject<BlockEntityType<EnergyPipeBlockEntity>> ADVANCED_ENERGY_PIPE = BLOCK_ENTITIES.register(
        "advanced_energy_pipe",
        () -> BlockEntityType.Builder.of(
            EnergyPipeBlockEntity::new,
            FPipesBlocks.ADVANCED_ENERGY_PIPE.get()
        ).build(null)
    );
    public static final RegistryObject<BlockEntityType<EnergyPipeBlockEntity>> ELITE_ENERGY_PIPE = BLOCK_ENTITIES.register(
        "elite_energy_pipe",
        () -> BlockEntityType.Builder.of(
            EnergyPipeBlockEntity::new,
            FPipesBlocks.ELITE_ENERGY_PIPE.get()
        ).build(null)
    );
    public static final RegistryObject<BlockEntityType<EnergyPipeBlockEntity>> ULTIMATE_ENERGY_PIPE = BLOCK_ENTITIES.register(
        "ultimate_energy_pipe",
        () -> BlockEntityType.Builder.of(
            EnergyPipeBlockEntity::new,
            FPipesBlocks.ULTIMATE_ENERGY_PIPE.get()
        ).build(null)
    );

    // Terminal
    public static final RegistryObject<BlockEntityType<TerminalBlockEntity>> TERMINAL = BLOCK_ENTITIES.register(
        "terminal",
        () -> BlockEntityType.Builder.of(
            TerminalBlockEntity::new,
            FPipesBlocks.TERMINAL.get()
        ).build(null)
    );

    // Tiered Barrel (one BE type for all barrel blocks)
    public static final RegistryObject<BlockEntityType<TieredBarrelBlockEntity>> TIERED_BARREL = BLOCK_ENTITIES.register(
        "tiered_barrel",
        () -> BlockEntityType.Builder.of(
            TieredBarrelBlockEntity::new,
            FPipesBlocks.OAK_BARREL.get(),
            FPipesBlocks.COPPER_BARREL.get(),
            FPipesBlocks.IRON_BARREL.get(),
            FPipesBlocks.GOLD_BARREL.get(),
            FPipesBlocks.DIAMOND_BARREL.get(),
            FPipesBlocks.NETHERITE_BARREL.get()
        ).build(null)
    );
} 
package com.piglinmine.fastpipes;
import com.piglinmine.fastpipes.blockentity.EnergyPipeBlockEntity;
import com.piglinmine.fastpipes.blockentity.FluidPipeBlockEntity;
import com.piglinmine.fastpipes.blockentity.ItemPipeBlockEntity;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;

public class FPipesCapabilities {

    @SubscribeEvent
    public static void registerCapabilities(RegisterCapabilitiesEvent event) {
        // Register Item Handler capabilities for Item Pipes
        event.registerBlockEntity(
            Capabilities.ItemHandler.BLOCK,
            FPipesBlockEntities.BASIC_ITEM_PIPE.get(),
            ItemPipeBlockEntity::getItemHandler
        );
        event.registerBlockEntity(
            Capabilities.ItemHandler.BLOCK,
            FPipesBlockEntities.IMPROVED_ITEM_PIPE.get(),
            ItemPipeBlockEntity::getItemHandler
        );
        event.registerBlockEntity(
            Capabilities.ItemHandler.BLOCK,
            FPipesBlockEntities.ADVANCED_ITEM_PIPE.get(),
            ItemPipeBlockEntity::getItemHandler
        );

        // Register Fluid Handler capabilities for Fluid Pipes
        event.registerBlockEntity(
            Capabilities.FluidHandler.BLOCK,
            FPipesBlockEntities.BASIC_FLUID_PIPE.get(),
            FluidPipeBlockEntity::getFluidHandler
        );
        event.registerBlockEntity(
            Capabilities.FluidHandler.BLOCK,
            FPipesBlockEntities.IMPROVED_FLUID_PIPE.get(),
            FluidPipeBlockEntity::getFluidHandler
        );
        event.registerBlockEntity(
            Capabilities.FluidHandler.BLOCK,
            FPipesBlockEntities.ADVANCED_FLUID_PIPE.get(),
            FluidPipeBlockEntity::getFluidHandler
        );
        event.registerBlockEntity(
            Capabilities.FluidHandler.BLOCK,
            FPipesBlockEntities.ELITE_FLUID_PIPE.get(),
            FluidPipeBlockEntity::getFluidHandler
        );
        event.registerBlockEntity(
            Capabilities.FluidHandler.BLOCK,
            FPipesBlockEntities.ULTIMATE_FLUID_PIPE.get(),
            FluidPipeBlockEntity::getFluidHandler
        );

        // Register Energy Storage capabilities for Energy Pipes
        event.registerBlockEntity(
            Capabilities.EnergyStorage.BLOCK,
            FPipesBlockEntities.BASIC_ENERGY_PIPE.get(),
            EnergyPipeBlockEntity::getEnergyStorage
        );
        event.registerBlockEntity(
            Capabilities.EnergyStorage.BLOCK,
            FPipesBlockEntities.IMPROVED_ENERGY_PIPE.get(),
            EnergyPipeBlockEntity::getEnergyStorage
        );
        event.registerBlockEntity(
            Capabilities.EnergyStorage.BLOCK,
            FPipesBlockEntities.ADVANCED_ENERGY_PIPE.get(),
            EnergyPipeBlockEntity::getEnergyStorage
        );
        event.registerBlockEntity(
            Capabilities.EnergyStorage.BLOCK,
            FPipesBlockEntities.ELITE_ENERGY_PIPE.get(),
            EnergyPipeBlockEntity::getEnergyStorage
        );
        event.registerBlockEntity(
            Capabilities.EnergyStorage.BLOCK,
            FPipesBlockEntities.ULTIMATE_ENERGY_PIPE.get(),
            EnergyPipeBlockEntity::getEnergyStorage
        );
    }
} 
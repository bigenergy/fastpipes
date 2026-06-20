package com.piglinmine.fastpipes;

import com.piglinmine.fastpipes.barrel.TieredBarrelBlockEntity;
import com.piglinmine.fastpipes.blockentity.EnergyPipeBlockEntity;
import com.piglinmine.fastpipes.blockentity.FluidPipeBlockEntity;
import com.piglinmine.fastpipes.blockentity.ItemPipeBlockEntity;
import com.piglinmine.fastpipes.blockentity.TerminalBlockEntity;
import com.piglinmine.fastpipes.compat.LegacyEnergyHandlerAdapter;
import com.piglinmine.fastpipes.compat.LegacyFluidHandlerResourceHandler;
import com.piglinmine.fastpipes.compat.LegacyItemHandlerResourceHandler;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.energy.IEnergyStorage;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.items.IItemHandler;

/**
 * Registers BlockEntity capability providers. NeoForge 1.21.11 moved item/fluid/energy
 * capabilities to a resource-based system ({@code ResourceHandler<ItemResource>},
 * {@code ResourceHandler<FluidResource>}, {@code EnergyHandler}). Our block entities still
 * speak the legacy interfaces ({@link IItemHandler}, {@link IFluidHandler}, {@link IEnergyStorage}),
 * so each provider wraps the legacy handler via one of the adapters in
 * {@code com.piglinmine.fastpipes.compat}.
 */
public class FPipesCapabilities {

    @SubscribeEvent
    public static void registerCapabilities(RegisterCapabilitiesEvent event) {
        // ==== Item capabilities ====
        // All three item pipe tiers expose a push-only IItemHandler that routes inserted stacks
        // into the network.
        for (var holder : new net.neoforged.neoforge.registries.DeferredHolder[]{
                FPipesBlockEntities.BASIC_ITEM_PIPE,
                FPipesBlockEntities.IMPROVED_ITEM_PIPE,
                FPipesBlockEntities.ADVANCED_ITEM_PIPE
        }) {
            @SuppressWarnings("unchecked")
            var beType = (net.minecraft.world.level.block.entity.BlockEntityType<ItemPipeBlockEntity>) holder.get();
            event.registerBlockEntity(Capabilities.Item.BLOCK, beType, (be, side) -> {
                IItemHandler legacy = be.getItemHandler(side);
                return legacy == null ? null : new LegacyItemHandlerResourceHandler(legacy);
            });
        }

        // Terminal exposes an empty IItemHandler placeholder so adjacent pipes detect it as a
        // valid inv-connection target.
        event.registerBlockEntity(Capabilities.Item.BLOCK, FPipesBlockEntities.TERMINAL.get(), (be, side) -> {
            IItemHandler legacy = be.getItemHandler(side);
            return legacy == null ? null : new LegacyItemHandlerResourceHandler(legacy);
        });

        // Tiered barrel exposes its WorldlyContainer as IItemHandler via InvWrapper.
        event.registerBlockEntity(Capabilities.Item.BLOCK, FPipesBlockEntities.TIERED_BARREL.get(), (be, side) -> {
            IItemHandler legacy = be.getItemHandler(side);
            return legacy == null ? null : new LegacyItemHandlerResourceHandler(legacy);
        });

        // ==== Fluid capabilities ====
        for (var holder : new net.neoforged.neoforge.registries.DeferredHolder[]{
                FPipesBlockEntities.BASIC_FLUID_PIPE,
                FPipesBlockEntities.IMPROVED_FLUID_PIPE,
                FPipesBlockEntities.ADVANCED_FLUID_PIPE,
                FPipesBlockEntities.ELITE_FLUID_PIPE,
                FPipesBlockEntities.ULTIMATE_FLUID_PIPE
        }) {
            @SuppressWarnings("unchecked")
            var beType = (net.minecraft.world.level.block.entity.BlockEntityType<FluidPipeBlockEntity>) holder.get();
            event.registerBlockEntity(Capabilities.Fluid.BLOCK, beType, (be, side) -> {
                IFluidHandler legacy = be.getFluidHandler(side);
                return legacy == null ? null : new LegacyFluidHandlerResourceHandler(legacy);
            });
        }

        // ==== Energy capabilities ====
        for (var holder : new net.neoforged.neoforge.registries.DeferredHolder[]{
                FPipesBlockEntities.BASIC_ENERGY_PIPE,
                FPipesBlockEntities.IMPROVED_ENERGY_PIPE,
                FPipesBlockEntities.ADVANCED_ENERGY_PIPE,
                FPipesBlockEntities.ELITE_ENERGY_PIPE,
                FPipesBlockEntities.ULTIMATE_ENERGY_PIPE
        }) {
            @SuppressWarnings("unchecked")
            var beType = (net.minecraft.world.level.block.entity.BlockEntityType<EnergyPipeBlockEntity>) holder.get();
            event.registerBlockEntity(Capabilities.Energy.BLOCK, beType, (be, side) -> {
                IEnergyStorage legacy = be.getEnergyStorage(side);
                return legacy == null ? null : new LegacyEnergyHandlerAdapter(legacy);
            });
        }
    }
}

package com.piglinmine.fastpipes.blockentity;

import com.piglinmine.fastpipes.FPipesBlockEntities;
import com.piglinmine.fastpipes.FPipesBlocks;
import com.piglinmine.fastpipes.network.NetworkManager;
import com.piglinmine.fastpipes.network.pipe.Pipe;
import com.piglinmine.fastpipes.network.pipe.energy.ClientEnergyPipeEnergyStorage;
import com.piglinmine.fastpipes.network.pipe.energy.EnergyPipe;
import com.piglinmine.fastpipes.network.pipe.energy.EnergyPipeType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.energy.IEnergyStorage;

import javax.annotation.Nullable;

public class EnergyPipeBlockEntity extends PipeBlockEntity {
    private final EnergyPipeType type;
    private final ClientEnergyPipeEnergyStorage clientEnergyStorage;

    public EnergyPipeBlockEntity(BlockPos pos, BlockState state) {
        super(getBlockEntityType(state), pos, state);
        this.type = getEnergyPipeType(state);
        this.clientEnergyStorage = new ClientEnergyPipeEnergyStorage(type);
    }

    private static BlockEntityType<?> getBlockEntityType(BlockState state) {
        if (state.getBlock() == FPipesBlocks.BASIC_ENERGY_PIPE.get()) {
            return FPipesBlockEntities.BASIC_ENERGY_PIPE.get();
        } else if (state.getBlock() == FPipesBlocks.IMPROVED_ENERGY_PIPE.get()) {
            return FPipesBlockEntities.IMPROVED_ENERGY_PIPE.get();
        } else if (state.getBlock() == FPipesBlocks.ADVANCED_ENERGY_PIPE.get()) {
            return FPipesBlockEntities.ADVANCED_ENERGY_PIPE.get();
        } else if (state.getBlock() == FPipesBlocks.ELITE_ENERGY_PIPE.get()) {
            return FPipesBlockEntities.ELITE_ENERGY_PIPE.get();
        } else if (state.getBlock() == FPipesBlocks.ULTIMATE_ENERGY_PIPE.get()) {
            return FPipesBlockEntities.ULTIMATE_ENERGY_PIPE.get();
        }
        // Fallback to basic if unknown
        return FPipesBlockEntities.BASIC_ENERGY_PIPE.get();
    }

    private static EnergyPipeType getEnergyPipeType(BlockState state) {
        if (state.getBlock() == FPipesBlocks.BASIC_ENERGY_PIPE.get()) {
            return EnergyPipeType.BASIC;
        } else if (state.getBlock() == FPipesBlocks.IMPROVED_ENERGY_PIPE.get()) {
            return EnergyPipeType.IMPROVED;
        } else if (state.getBlock() == FPipesBlocks.ADVANCED_ENERGY_PIPE.get()) {
            return EnergyPipeType.ADVANCED;
        } else if (state.getBlock() == FPipesBlocks.ELITE_ENERGY_PIPE.get()) {
            return EnergyPipeType.ELITE;
        } else if (state.getBlock() == FPipesBlocks.ULTIMATE_ENERGY_PIPE.get()) {
            return EnergyPipeType.ULTIMATE;
        }
        // Fallback to basic if unknown
        return EnergyPipeType.BASIC;
    }

    public static void tick(EnergyPipeBlockEntity blockEntity) {
        // Energy pipes don't need special ticking, the network handles it
    }

    public EnergyPipeType getEnergyPipeType() {
        return type;
    }

    // NeoForge Capability Handler - returns null if no capability available
    @Nullable
    public IEnergyStorage getEnergyStorage(@Nullable Direction side) {
        if (!level.isClientSide) {
            NetworkManager mgr = NetworkManager.get(level);
            Pipe pipe = mgr.getPipe(worldPosition);
            if (pipe instanceof EnergyPipe energyPipe) {
                return energyPipe.getEnergyStorage();
            }
        }
        return clientEnergyStorage;
    }

    @Override
    protected Pipe createPipe(Level level, BlockPos pos) {
        return new EnergyPipe(level, pos, type);
    }
} 
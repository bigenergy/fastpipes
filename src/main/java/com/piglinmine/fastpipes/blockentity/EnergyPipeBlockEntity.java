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
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.IEnergyStorage;

import javax.annotation.Nullable;

public class EnergyPipeBlockEntity extends PipeBlockEntity {
    private final EnergyPipeType type;
    private final ClientEnergyPipeEnergyStorage clientEnergyStorage;
    // Order matters: stableEnergyStorage must be initialized before energyCapability references it.
    private final IEnergyStorage stableEnergyStorage = new DelegatingEnergyStorage();
    // Stable LazyOptional that delegates to whichever storage the current network has.
    // Cached for the lifetime of the BlockEntity so external code (e.g. IE generators) can
    // hold a single reference; reseating the network's storage instance doesn't break it.
    private final LazyOptional<IEnergyStorage> energyCapability =
        LazyOptional.of(() -> stableEnergyStorage);

    public EnergyPipeBlockEntity(BlockPos pos, BlockState state) {
        super(getBlockEntityType(state), pos, state);
        this.type = getEnergyPipeType(state);
        this.clientEnergyStorage = new ClientEnergyPipeEnergyStorage(type);
    }

    /** Always looks up the current network's storage. Prevents stale capability caches in
     *  adjacent machines from binding to orphaned EnergyNetwork instances after a rescan. */
    private final class DelegatingEnergyStorage implements IEnergyStorage {
        private IEnergyStorage current() {
            if (level == null || level.isClientSide) return clientEnergyStorage;
            NetworkManager mgr = NetworkManager.get(level);
            Pipe pipe = mgr.getPipe(worldPosition);
            if (pipe instanceof EnergyPipe energyPipe) {
                IEnergyStorage s = energyPipe.getEnergyStorage();
                if (s != null) return s;
            }
            return null;
        }
        @Override public int receiveEnergy(int maxReceive, boolean simulate) {
            IEnergyStorage s = current();
            if (s == null) return 0;
            // Per-pipe rate limit: this pipe's transferRate caps how much energy
            // can enter the network through this specific pipe. Mixed-tier networks
            // therefore behave intuitively (Basic feeds limit input at Basic rate,
            // higher-tier trunk pipes don't constrain unrelated flows).
            int clamped = Math.min(maxReceive, type.getTransferRate());
            return s.receiveEnergy(clamped, simulate);
        }
        @Override public int extractEnergy(int maxExtract, boolean simulate) {
            IEnergyStorage s = current();
            if (s == null) return 0;
            int clamped = Math.min(maxExtract, type.getTransferRate());
            return s.extractEnergy(clamped, simulate);
        }
        @Override public int getEnergyStored() {
            IEnergyStorage s = current();
            return s == null ? 0 : s.getEnergyStored();
        }
        @Override public int getMaxEnergyStored() {
            IEnergyStorage s = current();
            return s == null ? 0 : s.getMaxEnergyStored();
        }
        @Override public boolean canExtract() {
            IEnergyStorage s = current();
            return s != null && s.canExtract();
        }
        @Override public boolean canReceive() {
            IEnergyStorage s = current();
            return s != null && s.canReceive();
        }
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

    // Returns the stable wrapper — external callers don't bind to a specific network instance.
    @Nullable
    public IEnergyStorage getEnergyStorage(@Nullable Direction side) {
        return stableEnergyStorage;
    }

    @Override
    public <T> net.minecraftforge.common.util.LazyOptional<T> getCapability(net.minecraftforge.common.capabilities.Capability<T> cap, @javax.annotation.Nullable Direction side) {
        if (cap == net.minecraftforge.common.capabilities.ForgeCapabilities.ENERGY) {
            return energyCapability.cast();
        }
        return super.getCapability(cap, side);
    }

    @Override
    public void invalidateCaps() {
        super.invalidateCaps();
        energyCapability.invalidate();
    }

    @Override
    protected Pipe createPipe(Level level, BlockPos pos) {
        return new EnergyPipe(level, pos, type);
    }
} 
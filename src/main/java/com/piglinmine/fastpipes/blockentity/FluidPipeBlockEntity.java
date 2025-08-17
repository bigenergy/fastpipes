package com.piglinmine.fastpipes.blockentity;

import com.piglinmine.fastpipes.FPipesBlockEntities;
import com.piglinmine.fastpipes.FPipesBlocks;
import com.piglinmine.fastpipes.network.pipe.Pipe;
import com.piglinmine.fastpipes.network.pipe.fluid.FluidPipe;
import com.piglinmine.fastpipes.network.pipe.fluid.FluidPipeType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.fluids.FluidStack;

import javax.annotation.Nullable;

public class FluidPipeBlockEntity extends PipeBlockEntity {
    private FluidPipeType type;
    private FluidStack fluid = FluidStack.EMPTY;
    private float fullness = 0;
    private float renderFullness = 0;

    public FluidPipeBlockEntity(BlockPos pos, BlockState state) {
        super(getBlockEntityType(state), pos, state);
        this.type = getFluidPipeType(state);
    }

    private static BlockEntityType<?> getBlockEntityType(BlockState state) {
        if (state.getBlock() == FPipesBlocks.BASIC_FLUID_PIPE.get()) {
            return FPipesBlockEntities.BASIC_FLUID_PIPE.get();
        } else if (state.getBlock() == FPipesBlocks.IMPROVED_FLUID_PIPE.get()) {
            return FPipesBlockEntities.IMPROVED_FLUID_PIPE.get();
        } else if (state.getBlock() == FPipesBlocks.ADVANCED_FLUID_PIPE.get()) {
            return FPipesBlockEntities.ADVANCED_FLUID_PIPE.get();
        } else if (state.getBlock() == FPipesBlocks.ELITE_FLUID_PIPE.get()) {
            return FPipesBlockEntities.ELITE_FLUID_PIPE.get();
        } else if (state.getBlock() == FPipesBlocks.ULTIMATE_FLUID_PIPE.get()) {
            return FPipesBlockEntities.ULTIMATE_FLUID_PIPE.get();
        }
        // Fallback to basic if unknown
        return FPipesBlockEntities.BASIC_FLUID_PIPE.get();
    }

    private static FluidPipeType getFluidPipeType(BlockState state) {
        if (state.getBlock() == FPipesBlocks.BASIC_FLUID_PIPE.get()) {
            return FluidPipeType.BASIC;
        } else if (state.getBlock() == FPipesBlocks.IMPROVED_FLUID_PIPE.get()) {
            return FluidPipeType.IMPROVED;
        } else if (state.getBlock() == FPipesBlocks.ADVANCED_FLUID_PIPE.get()) {
            return FluidPipeType.ADVANCED;
        } else if (state.getBlock() == FPipesBlocks.ELITE_FLUID_PIPE.get()) {
            return FluidPipeType.ELITE;
        } else if (state.getBlock() == FPipesBlocks.ULTIMATE_FLUID_PIPE.get()) {
            return FluidPipeType.ULTIMATE;
        }
        // Fallback to basic if unknown
        return FluidPipeType.BASIC;
    }

    public static void tick(FluidPipeBlockEntity blockEntity) {
        // TODO: Implement fluid pipe ticking
    }

    public FluidStack getFluid() {
        return fluid;
    }

    public void setFluid(FluidStack fluid) {
        this.fluid = fluid;
    }

    public float updateAndGetRenderFullness(float partialTicks) {
        float step = partialTicks * 0.05F;

        if (renderFullness > fullness) {
            renderFullness -= step;

            if (renderFullness < fullness) {
                renderFullness = fullness;
            }
        } else if (renderFullness < fullness) {
            renderFullness += step;

            if (renderFullness > fullness) {
                renderFullness = fullness;
            }
        }

        return renderFullness;
    }

    @Override
    public CompoundTag writeUpdate(CompoundTag tag, HolderLookup.Provider registries) {
        // TODO: Implement when NetworkManager and FluidPipe are available
        /*
        Pipe pipe = NetworkManager.get(level).getPipe(worldPosition);
        if (pipe instanceof FluidPipe && pipe.getNetwork() != null) {
            tag.put("fluid", ((FluidNetwork) pipe.getNetwork()).getFluidTank().getFluid().writeToNBT(new CompoundTag()));
            tag.putFloat("fullness", ((FluidPipe) pipe).getFullness());
        }
        */

        return super.writeUpdate(tag, registries);
    }

    @Override
    public void readUpdate(@Nullable CompoundTag tag, HolderLookup.Provider registries) {
        if (tag != null && tag.contains("fluid")) {
            fluid = FluidStack.parseOptional(registries, tag.getCompound("fluid"));
        } else {
            fluid = FluidStack.EMPTY;
        }

        if (tag != null && tag.contains("fullness")) {
            fullness = tag.getFloat("fullness");
            renderFullness = fullness;
        } else {
            fullness = 0;
            renderFullness = 0;
        }

        super.readUpdate(tag, registries);
    }

    public float getFullness() {
        return fullness;
    }

    public void setFullness(float fullness) {
        this.fullness = fullness;
    }

    // NeoForge Capability Handler
    public net.neoforged.neoforge.fluids.capability.IFluidHandler getFluidHandler(net.minecraft.core.Direction side) {
        // TODO: Return actual fluid handler when network system is available
        // For now, return null to indicate no capability
        return null;
    }

    protected Pipe createPipe(Level level, BlockPos pos) {
        return new FluidPipe(level, pos, type);
    }
} 
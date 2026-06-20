package com.piglinmine.fastpipes.blockentity;

import com.piglinmine.fastpipes.FPipesBlockEntities;
import com.piglinmine.fastpipes.FPipesBlocks;
import com.piglinmine.fastpipes.network.NetworkManager;
import com.piglinmine.fastpipes.network.fluid.FluidNetwork;
import com.piglinmine.fastpipes.network.pipe.Pipe;
import com.piglinmine.fastpipes.network.pipe.fluid.FluidPipe;
import com.piglinmine.fastpipes.network.pipe.fluid.FluidPipeType;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.fluids.FluidStack;

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
        // Server-side network updates are handled by FluidPipe.update() via CommonSetup.onLevelTick.
        // Client-side fluid/fullness data is updated via FluidPipeMessage packets.
    }

    public FluidPipeType getFluidPipeType() {
        return type;
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
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        // TODO 1.21.11: writeUpdate/readUpdate removed; migrated to saveAdditional/loadAdditional with ValueOutput
        if (level != null && !level.isClientSide()) {
            Pipe pipe = NetworkManager.get(level).getPipe(worldPosition);
            if (pipe instanceof FluidPipe fluidPipe && fluidPipe.getNetwork() instanceof FluidNetwork fluidNetwork) {
                FluidStack fluid = fluidNetwork.getFluidTank().getFluid();
                output.store("fluid", FluidStack.OPTIONAL_CODEC, fluid);
                output.putFloat("fullness", fluidPipe.getFullness());
            }
        }
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        // TODO 1.21.11: writeUpdate/readUpdate removed; migrated to saveAdditional/loadAdditional with ValueInput
        fluid = input.read("fluid", FluidStack.OPTIONAL_CODEC).orElse(FluidStack.EMPTY);
        fullness = input.getFloatOr("fullness", 0F);
        renderFullness = fullness;
    }

    public float getFullness() {
        return fullness;
    }

    public void setFullness(float fullness) {
        this.fullness = fullness;
    }

    // NeoForge Capability Handler
    public net.neoforged.neoforge.fluids.capability.IFluidHandler getFluidHandler(net.minecraft.core.Direction side) {
        if (level == null || level.isClientSide()) return null;
        Pipe pipe = NetworkManager.get(level).getPipe(worldPosition);
        if (pipe != null && pipe.getNetwork() instanceof FluidNetwork fluidNetwork) {
            return fluidNetwork.getFluidTank();
        }
        return null;
    }

    protected Pipe createPipe(Level level, BlockPos pos) {
        return new FluidPipe(level, pos, type);
    }
}

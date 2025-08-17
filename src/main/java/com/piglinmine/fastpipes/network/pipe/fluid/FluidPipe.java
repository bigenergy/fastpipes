package com.piglinmine.fastpipes.network.pipe.fluid;

import com.piglinmine.fastpipes.FastPipes;
import com.piglinmine.fastpipes.network.fluid.FluidNetwork;
import com.piglinmine.fastpipes.network.message.FluidPipeMessage;
import com.piglinmine.fastpipes.network.pipe.Pipe;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;

public class FluidPipe extends Pipe {
    public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath(FastPipes.MOD_ID, "fluid");

    private final FluidPipeType type;
    private float lastFullness = 0;

    public FluidPipe(Level level, BlockPos pos, FluidPipeType type) {
        super(level, pos);

        this.type = type;
    }

    @Override
    public void update() {
        super.update();

        float f = getFullness();
        if (Math.abs(lastFullness - f) >= 0.1) {
            lastFullness = f;

            sendFluidPipeUpdate();
        }
    }

    public void sendFluidPipeUpdate() {
        // TODO: Implement fluid pipe update message when networking is available
        // FastPipes.NETWORK.sendInArea(level, pos, 32, new FluidPipeMessage(pos, ((FluidNetwork) network).getFluidTank().getFluid(), getFullness()));
    }

    public float getFullness() {
        if (network instanceof FluidNetwork fluidNetwork) {
            int cap = fluidNetwork.getFluidTank().getCapacity();
            int stored = fluidNetwork.getFluidTank().getFluidAmount();

            return Math.round(((float) stored / (float) cap) * 10.0F) / 10.0F;
        }
        return 0.0F;
    }

    public FluidPipeType getType() {
        return type;
    }

    @Override
    public CompoundTag writeToNbt(CompoundTag tag) {
        tag = super.writeToNbt(tag);

        tag.putInt("type", type.ordinal());

        return tag;
    }

    @Override
    public void readFromNbt(CompoundTag tag) {
        super.readFromNbt(tag);
        // Type is set in constructor, no need to read from NBT
    }

    @Override
    public ResourceLocation getId() {
        return ID;
    }

    @Override
    public ResourceLocation getNetworkType() {
        return type.getNetworkType();
    }
} 
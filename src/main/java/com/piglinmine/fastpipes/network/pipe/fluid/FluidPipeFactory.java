package com.piglinmine.fastpipes.network.pipe.fluid;

import com.piglinmine.fastpipes.network.pipe.Pipe;
import com.piglinmine.fastpipes.network.pipe.PipeFactory;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;

public class FluidPipeFactory implements PipeFactory {
    @Override
    public Pipe createFromNbt(Level level, CompoundTag tag) {
        BlockPos pos = BlockPos.of(tag.getLong("pos"));
        FluidPipeType type = FluidPipeType.get(tag.getInt("type"));
        
        FluidPipe pipe = new FluidPipe(level, pos, type);
        pipe.readFromNbt(tag);
        
        return pipe;
    }
} 
package com.piglinmine.fastpipes.block;
import com.piglinmine.fastpipes.util.CapabilityUtil;

import com.piglinmine.fastpipes.blockentity.FluidPipeBlockEntity;
import com.piglinmine.fastpipes.blockentity.PipeBlockEntity;
import com.piglinmine.fastpipes.network.NetworkManager;
import com.piglinmine.fastpipes.network.pipe.Pipe;
import com.piglinmine.fastpipes.network.pipe.fluid.FluidPipeType;
import com.piglinmine.fastpipes.network.pipe.shape.PipeShapeCache;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class FluidPipeBlock extends PipeBlock implements EntityBlock {
    private final FluidPipeType type;

    public FluidPipeBlock(PipeShapeCache shapeCache, FluidPipeType type) {
        super(shapeCache);
        this.type = type;
    }

    public FluidPipeType getType() {
        return type;
    }

    @Override
    protected boolean hasConnection(LevelAccessor level, BlockPos pos, Direction direction) {
        BlockEntity currentBlockEntity = level.getBlockEntity(pos);
        if (currentBlockEntity instanceof FluidPipeBlockEntity fpe) {
            if (fpe.getAttachmentManager().hasAttachment(direction)) return false;
            if (fpe.isDisconnected(direction)) return false;
        }

        BlockState facingState = level.getBlockState(pos.relative(direction));
        BlockEntity facingBlockEntity = level.getBlockEntity(pos.relative(direction));

        if (facingBlockEntity instanceof FluidPipeBlockEntity fpe) {
            if (fpe.getAttachmentManager().hasAttachment(direction.getOpposite())) return false;
            if (fpe.isDisconnected(direction.getOpposite())) return false;
        }

        if (!(facingState.getBlock() instanceof FluidPipeBlock fpb) || fpb.getType() != type) return false;

        // Color check: colored pipes only connect to same color or uncolored pipes
        // Only check on server — client trusts server-sent block state
        if (level instanceof Level lvl && !lvl.isClientSide) {
            Pipe myPipe = NetworkManager.get(lvl).getPipe(pos);
            Pipe theirPipe = NetworkManager.get(lvl).getPipe(pos.relative(direction));
            if (myPipe != null && theirPipe != null) {
                DyeColor myColor = myPipe.getColor();
                DyeColor theirColor = theirPipe.getColor();
                if (myColor != null && theirColor != null && myColor != theirColor) return false;
            }
        }

        return true;
    }

    @Override
    protected boolean hasInvConnection(LevelAccessor world, BlockPos pos, Direction direction) {
        BlockEntity be = world.getBlockEntity(pos);
        if (be instanceof FluidPipeBlockEntity fpe && fpe.isDisconnected(direction)) return false;

        // Don't show inventory connection indicator toward other fluid pipes
        BlockState facingState = world.getBlockState(pos.relative(direction));
        if (facingState.getBlock() instanceof FluidPipeBlock) return false;

        if (world instanceof Level level) {
            return CapabilityUtil.getFluidHandler(level, pos.relative(direction), direction.getOpposite()) != null;
        }
        return false;
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new FluidPipeBlockEntity(pos, state);
    }
} 
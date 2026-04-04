package com.piglinmine.fastpipes.block;

import com.piglinmine.fastpipes.blockentity.EnergyPipeBlockEntity;
import com.piglinmine.fastpipes.blockentity.PipeBlockEntity;
import com.piglinmine.fastpipes.network.NetworkManager;
import com.piglinmine.fastpipes.network.pipe.Pipe;
import com.piglinmine.fastpipes.network.pipe.energy.EnergyPipeEnergyStorage;
import com.piglinmine.fastpipes.network.pipe.energy.EnergyPipeType;
import com.piglinmine.fastpipes.network.pipe.shape.PipeShapeCache;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.energy.IEnergyStorage;
import org.jetbrains.annotations.Nullable;

public class EnergyPipeBlock extends PipeBlock implements EntityBlock {
    private final EnergyPipeType type;

    public EnergyPipeBlock(PipeShapeCache shapeCache, EnergyPipeType type) {
        super(shapeCache);
        this.type = type;
    }

    public EnergyPipeType getType() {
        return type;
    }

    @Override
    protected boolean hasConnection(LevelAccessor level, BlockPos pos, Direction direction) {
        BlockEntity currentBlockEntity = level.getBlockEntity(pos);
        if (currentBlockEntity instanceof EnergyPipeBlockEntity epe) {
            if (epe.getAttachmentManager().hasAttachment(direction)) return false;
            if (epe.isDisconnected(direction)) return false;
        }

        BlockState facingState = level.getBlockState(pos.relative(direction));
        BlockEntity facingBlockEntity = level.getBlockEntity(pos.relative(direction));

        if (facingBlockEntity instanceof EnergyPipeBlockEntity epe) {
            if (epe.getAttachmentManager().hasAttachment(direction.getOpposite())) return false;
            if (epe.isDisconnected(direction.getOpposite())) return false;
        }

        if (!(facingState.getBlock() instanceof EnergyPipeBlock epb) || epb.getType() != type) return false;

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
        if (be instanceof EnergyPipeBlockEntity epe && epe.isDisconnected(direction)) return false;

        if (world instanceof Level level) {
            IEnergyStorage energyStorage = level.getCapability(Capabilities.EnergyStorage.BLOCK, pos.relative(direction), direction.getOpposite());
            if (energyStorage == null) {
                return false;
            }

            // Don't connect to other energy pipes
            if (energyStorage instanceof EnergyPipeEnergyStorage) {
                return false;
            }

            return true;
        }
        return false;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new EnergyPipeBlockEntity(pos, state);
    }
} 
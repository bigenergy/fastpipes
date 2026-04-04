package com.piglinmine.fastpipes.block;

import com.piglinmine.fastpipes.blockentity.ItemPipeBlockEntity;
import com.piglinmine.fastpipes.blockentity.PipeBlockEntity;
import com.piglinmine.fastpipes.network.NetworkManager;
import com.piglinmine.fastpipes.network.pipe.Pipe;
import com.piglinmine.fastpipes.network.pipe.item.ItemPipeType;
import com.piglinmine.fastpipes.network.pipe.shape.PipeShapeCache;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.capabilities.Capabilities;
import org.jetbrains.annotations.Nullable;

public class ItemPipeBlock extends PipeBlock implements EntityBlock {
    private final ItemPipeType type;

    public ItemPipeBlock(PipeShapeCache shapeCache, ItemPipeType type) {
        super(shapeCache);
        this.type = type;
    }

    public ItemPipeType getType() {
        return type;
    }

    @Override
    protected boolean hasConnection(LevelAccessor world, BlockPos pos, Direction direction) {
        BlockEntity currentBlockEntity = world.getBlockEntity(pos);
        if (currentBlockEntity instanceof ItemPipeBlockEntity ipe) {
            if (ipe.getAttachmentManager().hasAttachment(direction)) return false;
            if (ipe.isDisconnected(direction)) return false;
        }

        BlockState facingState = world.getBlockState(pos.relative(direction));
        BlockEntity facingBlockEntity = world.getBlockEntity(pos.relative(direction));

        if (facingBlockEntity instanceof ItemPipeBlockEntity ipe) {
            if (ipe.getAttachmentManager().hasAttachment(direction.getOpposite())) return false;
            if (ipe.isDisconnected(direction.getOpposite())) return false;
        }

        if (!(facingState.getBlock() instanceof ItemPipeBlock)) return false;

        // Color check: colored pipes only connect to same color or uncolored pipes
        // Uncolored pipes connect to everything
        // Only check on server — client trusts server-sent block state
        if (world instanceof Level lvl && !lvl.isClientSide) {
            // Get color directly from Pipe objects in NetworkManager (bypasses block entity)
            Pipe myPipe = NetworkManager.get(lvl).getPipe(pos);
            Pipe theirPipe = NetworkManager.get(lvl).getPipe(pos.relative(direction));
            if (myPipe != null && theirPipe != null) {
                DyeColor myColor = myPipe.getColor();
                DyeColor theirColor = theirPipe.getColor();
                if (myColor != null && theirColor != null && myColor != theirColor) {
                    return false;
                }
            }
        }

        return true;
    }

    @Override
    protected boolean hasInvConnection(LevelAccessor world, BlockPos pos, Direction direction) {
        BlockEntity be = world.getBlockEntity(pos);
        if (be instanceof ItemPipeBlockEntity ipe && ipe.isDisconnected(direction)) return false;

        // Don't show inventory connection indicator toward other item pipes
        // (pipes expose IItemHandler, which would cause false inv connections
        //  when pipe-to-pipe connection is blocked by color)
        BlockState facingState = world.getBlockState(pos.relative(direction));
        if (facingState.getBlock() instanceof ItemPipeBlock) return false;

        if (world instanceof Level level) {
            return level.getCapability(Capabilities.ItemHandler.BLOCK, pos.relative(direction), direction.getOpposite()) != null;
        }
        return false;
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new ItemPipeBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return level.isClientSide ? (levelTicker, pos, stateTicker, blockEntity) -> ItemPipeBlockEntity.tick((ItemPipeBlockEntity) blockEntity) : null;
    }
} 
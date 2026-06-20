package com.piglinmine.fastpipes.block;

import com.piglinmine.fastpipes.blockentity.TerminalBlockEntity;
import com.piglinmine.fastpipes.menu.TerminalMenuProvider;
import com.piglinmine.fastpipes.network.NetworkManager;
import com.piglinmine.fastpipes.network.pipe.Pipe;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.BlockHitResult;

import javax.annotation.Nullable;

public class TerminalBlock extends Block implements EntityBlock {

    public static final EnumProperty<Direction> FACING = HorizontalDirectionalBlock.FACING;

    public TerminalBlock(BlockBehaviour.Properties properties) {
        super(properties.strength(2.0f).noOcclusion());
        this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new TerminalBlockEntity(pos, state);
    }

    // TODO 1.21.11: onRemove was removed in 1.21.11; vanilla BaseContainerBlockEntity.preRemoveSideEffects handles drops automatically.
    // Craft grid items will be dropped via the standard container drop mechanism.

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (level.isClientSide()) {
            return InteractionResult.SUCCESS;
        }

        if (player instanceof ServerPlayer serverPlayer) {
            Pipe connectedPipe = findConnectedPipe(level, pos);
            if (connectedPipe != null) {
                if (level.getBlockEntity(pos) instanceof TerminalBlockEntity be) {
                    if (!be.tryAcquire(player.getUUID(), player.getName().getString())) {
                        String owner = be.getActiveUserName() != null ? be.getActiveUserName() : "?";
                        serverPlayer.displayClientMessage(
                            Component.translatable("gui.fastpipes.terminal.in_use", owner), true);
                        return InteractionResult.SUCCESS;
                    }
                }
                TerminalMenuProvider.open(pos, serverPlayer);
            }
        }
        return InteractionResult.SUCCESS;
    }

    @Nullable
    public static Pipe findConnectedPipe(Level level, BlockPos terminalPos) {
        if (level.isClientSide()) return null;
        NetworkManager mgr = NetworkManager.get(level);
        for (Direction dir : Direction.values()) {
            Pipe pipe = mgr.getPipe(terminalPos.relative(dir));
            if (pipe != null) {
                return pipe;
            }
        }
        return null;
    }
}

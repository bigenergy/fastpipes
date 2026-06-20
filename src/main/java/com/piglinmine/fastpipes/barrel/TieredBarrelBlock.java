package com.piglinmine.fastpipes.barrel;

import com.piglinmine.fastpipes.FPipesBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.BlockHitResult;

import javax.annotation.Nullable;

public class TieredBarrelBlock extends Block implements EntityBlock {
    public static final EnumProperty<Direction> FACING = BlockStateProperties.FACING;

    private final BarrelTier tier;

    public TieredBarrelBlock(BarrelTier tier) {
        super(Properties.of().strength(2.5f));
        this.tier = tier;
        this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH));
    }

    public BarrelTier getTier() {
        return tier;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.defaultBlockState().setValue(FACING, context.getNearestLookingDirection().getOpposite());
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new TieredBarrelBlockEntity(pos, state);
    }

    @Override
    protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (!level.isClientSide() && stack.getItem() instanceof BarrelUpgradeItem upgradeItem) {
            BarrelTier targetTier = upgradeItem.getTargetTier();
            if (tier.canUpgradeTo(targetTier)) {
                upgradeBarrel(level, pos, state, targetTier);
                if (!player.isCreative()) {
                    stack.shrink(1);
                }
                return InteractionResult.SUCCESS;
            }
        }
        return InteractionResult.TRY_WITH_EMPTY_HAND;
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (level.isClientSide()) return InteractionResult.SUCCESS;
        if (player instanceof ServerPlayer serverPlayer) {
            if (level.getBlockEntity(pos) instanceof TieredBarrelBlockEntity be) {
                TieredBarrelMenuProvider.open(serverPlayer, be);
            }
        }
        return InteractionResult.SUCCESS;
    }

    private void upgradeBarrel(Level level, BlockPos pos, BlockState oldState, BarrelTier targetTier) {
        // Save contents
        TieredBarrelBlockEntity oldBe = null;
        if (level.getBlockEntity(pos) instanceof TieredBarrelBlockEntity be) {
            oldBe = be;
        }
        ItemStack[] savedItems = null;
        String savedName = null;
        if (oldBe != null) {
            savedItems = new ItemStack[oldBe.getContainerSize()];
            for (int i = 0; i < oldBe.getContainerSize(); i++) {
                savedItems[i] = oldBe.getItem(i).copy();
            }
            if (oldBe.hasCustomName()) {
                savedName = oldBe.getCustomName().getString();
            }
        }

        // Replace block
        Direction facing = oldState.getValue(FACING);
        Block targetBlock = FPipesBlocks.getBarrelBlock(targetTier);
        if (targetBlock == null) return;

        // Clear contents before setBlock so vanilla onRemove doesn't drop them
        // (we re-fill the new BE below, otherwise items would be duplicated).
        if (oldBe != null) {
            oldBe.clearContent();
        }

        level.setBlock(pos, targetBlock.defaultBlockState().setValue(FACING, facing), 3);

        // Restore contents
        if (savedItems != null && level.getBlockEntity(pos) instanceof TieredBarrelBlockEntity newBe) {
            for (int i = 0; i < Math.min(savedItems.length, newBe.getContainerSize()); i++) {
                newBe.setItem(i, savedItems[i]);
            }
        }
    }

    // TODO 1.21.11: onRemove was removed in 1.21.11; vanilla BaseContainerBlockEntity.preRemoveSideEffects handles drops automatically.

    @Override
    protected boolean hasAnalogOutputSignal(BlockState state) {
        return true;
    }

    @Override
    protected int getAnalogOutputSignal(BlockState state, Level level, BlockPos pos, net.minecraft.core.Direction direction) {
        if (level.getBlockEntity(pos) instanceof TieredBarrelBlockEntity be) {
            return net.minecraft.world.inventory.AbstractContainerMenu.getRedstoneSignalFromContainer(be);
        }
        return 0;
    }
}

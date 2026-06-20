package com.piglinmine.fastpipes.blockentity;

import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

// TODO 1.21.11: the old writeUpdate/readUpdate(CompoundTag, HolderLookup.Provider) contract
// has been dropped because the BlockEntity save/load API now flows through ValueOutput /
// ValueInput. Subclasses should override saveAdditional(ValueOutput) and
// loadAdditional(ValueInput) directly; the default NeoForge IBlockEntityExtension
// implementations of onDataPacket / handleUpdateTag already route into loadWithComponents,
// so we no longer need to override them here.
public abstract class BaseBlockEntity extends BlockEntity {
    protected BaseBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    @Override
    public final ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }
}

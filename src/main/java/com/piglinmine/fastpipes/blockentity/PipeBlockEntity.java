package com.piglinmine.fastpipes.blockentity;

import com.piglinmine.fastpipes.network.NetworkManager;
import com.piglinmine.fastpipes.network.pipe.Pipe;
import com.piglinmine.fastpipes.network.pipe.attachment.Attachment;
import com.piglinmine.fastpipes.network.pipe.attachment.AttachmentManager;
import com.piglinmine.fastpipes.network.pipe.attachment.ClientAttachmentManager;
import com.piglinmine.fastpipes.network.pipe.attachment.DummyAttachmentManager;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Containers;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.model.data.ModelData;
import net.neoforged.neoforge.client.model.data.ModelProperty;

import java.util.EnumSet;
import java.util.Set;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public abstract class PipeBlockEntity extends BaseBlockEntity {
    public static final ModelProperty<ResourceLocation[]> ATTACHMENTS_PROPERTY = new ModelProperty<>();
    private final AttachmentManager clientAttachmentManager = new ClientAttachmentManager();
    private final Set<Direction> clientDisconnectedSides = EnumSet.noneOf(Direction.class);

    protected PipeBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    public boolean isDisconnected(Direction dir) {
        if (level != null && level.isClientSide) {
            return clientDisconnectedSides.contains(dir);
        }

        Pipe pipe = NetworkManager.get(level).getPipe(worldPosition);
        if (pipe != null) {
            return pipe.isDisconnected(dir);
        }
        return false;
    }

    public AttachmentManager getAttachmentManager() {
        if (level.isClientSide) {
            return clientAttachmentManager;
        }

        Pipe pipe = NetworkManager.get(level).getPipe(worldPosition);

        if (pipe != null) {
            return pipe.getAttachmentManager();
        }

        return DummyAttachmentManager.INSTANCE;
    }

    @Override
    public void clearRemoved() {
        super.clearRemoved();

        if (!level.isClientSide) {
            NetworkManager mgr = NetworkManager.get(level);

            if (mgr.getPipe(worldPosition) == null) {
                mgr.addPipe(createPipe(level, worldPosition));
            }
        }
    }

    private boolean unloaded;

    @Override
    public void onChunkUnloaded() {
        super.onChunkUnloaded();
        unloaded = true;
    }

    @Override
    public void setRemoved() {
        super.setRemoved();

        if (!level.isClientSide && !unloaded) {
            NetworkManager mgr = NetworkManager.get(level);

            Pipe pipe = mgr.getPipe(worldPosition);
            if (pipe != null) {
                spawnDrops(pipe);

                for (Attachment attachment : pipe.getAttachmentManager().getAttachments()) {
                    Containers.dropItemStack(level, worldPosition.getX(), worldPosition.getY(), worldPosition.getZ(), attachment.getDrop());
                }
            }

            mgr.removePipe(worldPosition);
        }
    }

    protected void spawnDrops(Pipe pipe) {
        // Override in subclasses if needed
    }

    @Nonnull
    @Override
    public ModelData getModelData() {
        return ModelData.builder().with(ATTACHMENTS_PROPERTY, getAttachmentManager().getState()).build();
    }

    @Override
    public CompoundTag writeUpdate(CompoundTag tag, HolderLookup.Provider registries) {
        getAttachmentManager().writeUpdate(tag);

        if (level != null && !level.isClientSide) {
            Pipe pipe = NetworkManager.get(level).getPipe(worldPosition);
            if (pipe != null && !pipe.getDisconnectedSides().isEmpty()) {
                int bits = 0;
                for (Direction dir : pipe.getDisconnectedSides()) {
                    bits |= (1 << dir.get3DDataValue());
                }
                tag.putByte("disc", (byte) bits);
            }
        }

        return super.writeUpdate(tag, registries);
    }

    @Override
    public void readUpdate(@Nullable CompoundTag tag, HolderLookup.Provider registries) {
        super.readUpdate(tag, registries);
        getAttachmentManager().readUpdate(tag);

        clientDisconnectedSides.clear();
        if (tag != null && tag.contains("disc")) {
            int bits = tag.getByte("disc") & 0xFF;
            for (Direction dir : Direction.values()) {
                if ((bits & (1 << dir.get3DDataValue())) != 0) {
                    clientDisconnectedSides.add(dir);
                }
            }
        }

        requestModelDataUpdate();

        BlockState state = level.getBlockState(worldPosition);
        level.sendBlockUpdated(worldPosition, state, state, 1 | 2);
    }

    protected abstract Pipe createPipe(Level level, BlockPos pos);
} 
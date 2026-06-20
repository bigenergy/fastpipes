package com.piglinmine.fastpipes.blockentity;

import com.mojang.serialization.MapCodec;
import com.piglinmine.fastpipes.network.NetworkManager;
import com.piglinmine.fastpipes.network.pipe.Pipe;
import com.piglinmine.fastpipes.network.pipe.attachment.Attachment;
import com.piglinmine.fastpipes.network.pipe.attachment.AttachmentManager;
import com.piglinmine.fastpipes.network.pipe.attachment.ClientAttachmentManager;
import com.piglinmine.fastpipes.network.pipe.attachment.DummyAttachmentManager;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.Containers;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

import java.util.EnumSet;
import java.util.Optional;
import java.util.Set;
// Optional still used by the attachment bridge below.

import javax.annotation.Nullable;

public abstract class PipeBlockEntity extends BaseBlockEntity {
    // TODO 1.21.11: ModelData / ModelProperty were removed from this package; the old
    // ATTACHMENTS_PROPERTY / COLOR_PROPERTY constants and the getModelData() override are
    // gone. PipeBakedModel has been stubbed — attachment / color state must be re-wired
    // into the new model render-state architecture before pipes will render correctly.
    private final AttachmentManager clientAttachmentManager = new ClientAttachmentManager();
    private final Set<Direction> clientDisconnectedSides = EnumSet.noneOf(Direction.class);
    @Nullable
    private DyeColor clientColor = null;

    protected PipeBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    @Nullable
    public DyeColor getColor() {
        if (level != null && level.isClientSide()) {
            return clientColor;
        }

        Pipe pipe = NetworkManager.get(level).getPipe(worldPosition);
        if (pipe != null) {
            return pipe.getColor();
        }
        return null;
    }

    public boolean isDisconnected(Direction dir) {
        if (level != null && level.isClientSide()) {
            return clientDisconnectedSides.contains(dir);
        }

        Pipe pipe = NetworkManager.get(level).getPipe(worldPosition);
        if (pipe != null) {
            return pipe.isDisconnected(dir);
        }
        return false;
    }

    public AttachmentManager getAttachmentManager() {
        if (level.isClientSide()) {
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

        if (!level.isClientSide()) {
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

        if (!level.isClientSide() && !unloaded) {
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

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);

        // AttachmentManager.writeUpdate still works against CompoundTag — bridge through a
        // temporary tag and store its entries on the ValueOutput via the NeoForge extension.
        // TODO 1.21.11: port AttachmentManager to ValueOutput natively.
        CompoundTag bridge = new CompoundTag();
        getAttachmentManager().writeUpdate(bridge);
        if (!bridge.isEmpty()) {
            output.store(bridge);
        }

        if (level != null && !level.isClientSide()) {
            Pipe pipe = NetworkManager.get(level).getPipe(worldPosition);
            if (pipe != null) {
                if (!pipe.getDisconnectedSides().isEmpty()) {
                    int bits = 0;
                    for (Direction dir : pipe.getDisconnectedSides()) {
                        bits |= (1 << dir.get3DDataValue());
                    }
                    output.putByte("disc", (byte) bits);
                }
                if (pipe.getColor() != null) {
                    output.putByte("color", (byte) pipe.getColor().getId());
                }
            }
        }
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);

        // Reconstitute a CompoundTag from the ValueInput so the existing
        // AttachmentManager.readUpdate(CompoundTag) contract still works. The
        // ClientAttachmentManager is the only consumer here; the server-side
        // ServerAttachmentManager.readUpdate throws by design (see implementation).
        // TODO 1.21.11: port AttachmentManager.readUpdate to ValueInput natively.
        if (level != null && level.isClientSide()) {
            @SuppressWarnings("deprecation")
            Optional<CompoundTag> asTag = input.read(MapCodec.assumeMapUnsafe(CompoundTag.CODEC));
            asTag.ifPresent(clientAttachmentManager::readUpdate);
        }

        clientDisconnectedSides.clear();
        java.util.Set<String> keys = input.keySet();
        if (keys.contains("disc")) {
            int bits = input.getByteOr("disc", (byte) 0) & 0xFF;
            for (Direction dir : Direction.values()) {
                if ((bits & (1 << dir.get3DDataValue())) != 0) {
                    clientDisconnectedSides.add(dir);
                }
            }
        }

        if (keys.contains("color")) {
            clientColor = DyeColor.byId(input.getByteOr("color", (byte) 0) & 0xFF);
        } else {
            clientColor = null;
        }

        requestModelDataUpdate();

        if (level != null) {
            BlockState state = level.getBlockState(worldPosition);
            level.sendBlockUpdated(worldPosition, state, state, 1 | 2);
        }
    }

    protected abstract Pipe createPipe(Level level, BlockPos pos);
}

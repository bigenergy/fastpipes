package com.piglinmine.fastpipes.network.pipe;

import com.piglinmine.fastpipes.network.Network;
import com.piglinmine.fastpipes.network.pipe.attachment.Attachment;
import com.piglinmine.fastpipes.network.pipe.attachment.ServerAttachmentManager;
import net.minecraft.core.Direction;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.EnumSet;
import java.util.Objects;
import java.util.Set;

public abstract class Pipe {
    protected final Level level;
    protected final BlockPos pos;
    protected final ServerAttachmentManager attachmentManager = new ServerAttachmentManager(this);
    private final Logger logger = LogManager.getLogger(getClass());
    private final Set<Direction> disconnectedSides = EnumSet.noneOf(Direction.class);
    protected Network network;

    public Pipe(Level level, BlockPos pos) {
        this.level = level;
        this.pos = pos;
    }

    public void update() {
        for (Attachment attachment : attachmentManager.getAttachments()) {
            attachment.update();
        }
    }

    public ServerAttachmentManager getAttachmentManager() {
        return attachmentManager;
    }

    public Level getLevel() {
        return level;
    }

    public BlockPos getPos() {
        return pos;
    }

    public Network getNetwork() {
        return network;
    }

    public void joinNetwork(Network network) {
        this.network = network;

        logger.debug(pos + " joined network " + network.getId());

        sendBlockUpdate();
    }

    public void leaveNetwork() {
        logger.debug(pos + " left network " + network.getId());

        this.network = null;

        sendBlockUpdate();
    }

    public void sendBlockUpdate() {
        BlockState state = level.getBlockState(pos);
        level.sendBlockUpdated(pos, state, state, 1 | 2);
    }

    public boolean isDisconnected(Direction dir) {
        return disconnectedSides.contains(dir);
    }

    public boolean toggleDisconnect(Direction dir) {
        if (disconnectedSides.contains(dir)) {
            disconnectedSides.remove(dir);
            return false; // now connected
        } else {
            disconnectedSides.add(dir);
            return true; // now disconnected
        }
    }

    public Set<Direction> getDisconnectedSides() {
        return disconnectedSides;
    }

    public CompoundTag writeToNbt(CompoundTag tag) {
        tag.putLong("pos", pos.asLong());

        attachmentManager.writeToNbt(tag);

        if (!disconnectedSides.isEmpty()) {
            int bits = 0;
            for (Direction dir : disconnectedSides) {
                bits |= (1 << dir.get3DDataValue());
            }
            tag.putByte("disc", (byte) bits);
        }

        return tag;
    }

    public void readFromNbt(CompoundTag tag) {
        // Position is set in constructor, no need to read from NBT
        attachmentManager.readFromNbt(tag);

        disconnectedSides.clear();
        if (tag.contains("disc")) {
            int bits = tag.getByte("disc") & 0xFF;
            for (Direction dir : Direction.values()) {
                if ((bits & (1 << dir.get3DDataValue())) != 0) {
                    disconnectedSides.add(dir);
                }
            }
        }
    }

    public abstract ResourceLocation getId();

    public abstract ResourceLocation getNetworkType();

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Pipe pipe = (Pipe) o;
        return level.equals(pipe.level) &&
            pos.equals(pipe.pos);
    }

    @Override
    public int hashCode() {
        return Objects.hash(level, pos);
    }
} 
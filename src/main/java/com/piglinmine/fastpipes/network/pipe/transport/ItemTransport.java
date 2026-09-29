package com.piglinmine.fastpipes.network.pipe.transport;

import com.piglinmine.fastpipes.network.Network;
import com.piglinmine.fastpipes.network.pipe.Pipe;
import com.piglinmine.fastpipes.network.pipe.attachment.Attachment;
import com.piglinmine.fastpipes.network.pipe.item.ItemPipe;
import com.piglinmine.fastpipes.network.pipe.transport.callback.TransportCallback;
import com.piglinmine.fastpipes.network.pipe.transport.callback.TransportCallbackFactoryRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.LongTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import net.minecraft.core.HolderLookup;

import javax.annotation.Nullable;
import java.util.ArrayDeque;
import java.util.Deque;

public class ItemTransport {
    private static final Logger LOGGER = LogManager.getLogger(ItemTransport.class);
    private static final long LOG_INTERVAL_MS = 10_000L;
    private static final Object LOG_LOCK = new Object();
    private static long lastLoggedAt;
    private static int suppressedSinceLastLog;

    private final ItemStack value;
    private final BlockPos source;
    private final BlockPos destination;
    private final Deque<BlockPos> path;
    private final Direction initialDirection;
    private final TransportCallback finishedCallback;
    private final TransportCallback cancelCallback;
    private final TransportCallback pipeGoneCallback;
    private boolean firstPipe = true;
    private int progressInCurrentPipe;

    public ItemTransport(ItemStack value, BlockPos source, BlockPos destination, Deque<BlockPos> path, TransportCallback finishedCallback, TransportCallback cancelCallback, TransportCallback pipeGoneCallback) {
        this.value = value;
        this.source = source;
        this.destination = destination;
        this.path = path;
        this.initialDirection = getDirection(source, path.peek());
        this.path.poll(); // Pop first pipe.
        this.finishedCallback = finishedCallback;
        this.cancelCallback = cancelCallback;
        this.pipeGoneCallback = pipeGoneCallback;
    }

    public ItemTransport(ItemStack value, BlockPos source, BlockPos destination, Deque<BlockPos> path, Direction initialDirection, TransportCallback finishedCallback, TransportCallback cancelCallback, TransportCallback pipeGoneCallback, boolean firstPipe, int progressInCurrentPipe) {
        this.value = value;
        this.source = source;
        this.destination = destination;
        this.path = path;
        this.initialDirection = initialDirection;
        this.finishedCallback = finishedCallback;
        this.cancelCallback = cancelCallback;
        this.pipeGoneCallback = pipeGoneCallback;
        this.firstPipe = firstPipe;
        this.progressInCurrentPipe = progressInCurrentPipe;
    }

    private static Direction getDirection(BlockPos a, BlockPos b) {
        if (a.relative(Direction.NORTH).equals(b)) {
            return Direction.NORTH;
        }

        if (a.relative(Direction.EAST).equals(b)) {
            return Direction.EAST;
        }

        if (a.relative(Direction.SOUTH).equals(b)) {
            return Direction.SOUTH;
        }

        if (a.relative(Direction.WEST).equals(b)) {
            return Direction.WEST;
        }

        if (a.relative(Direction.UP).equals(b)) {
            return Direction.UP;
        }

        if (a.relative(Direction.DOWN).equals(b)) {
            return Direction.DOWN;
        }

        return Direction.NORTH;
    }

    @Nullable
    public static ItemTransport of(CompoundTag tag, HolderLookup.Provider registries) {
        // Use ItemStack.parseOptional with HolderLookup.Provider for MC 1.21.1
        ItemStack value = ItemStack.parseOptional(registries, tag.getCompound("v"));
        if (value.isEmpty()) {
            LOGGER.warn("Item no longer exists");
            return null;
        }

        BlockPos source = BlockPos.of(tag.getLong("src"));
        BlockPos destination = BlockPos.of(tag.getLong("dst"));

        ListTag pathTag = tag.getList("pth", Tag.TAG_LONG);
        Deque<BlockPos> path = new ArrayDeque<>();
        for (Tag pathItem : pathTag) {
            path.add(BlockPos.of(((LongTag) pathItem).getAsLong()));
        }

        Direction initialDirection = Direction.values()[tag.getInt("initd")];

        ResourceLocation finishedCallbackId = ResourceLocation.parse(tag.getString("fcid"));
        TransportCallback finishedCallback = TransportCallbackFactoryRegistry.createCallback(
            finishedCallbackId, tag.getCompound("fc"), registries
        );

        ResourceLocation cancelCallbackId = ResourceLocation.parse(tag.getString("ccid"));
        TransportCallback cancelCallback = TransportCallbackFactoryRegistry.createCallback(
            cancelCallbackId, tag.getCompound("cc"), registries
        );

        ResourceLocation pipeGoneCallbackId = ResourceLocation.parse(tag.getString("pgcid"));
        TransportCallback pipeGoneCallback = TransportCallbackFactoryRegistry.createCallback(
            pipeGoneCallbackId, tag.getCompound("pgc"), registries
        );

        if (finishedCallback == null || cancelCallback == null || pipeGoneCallback == null) {
            LOGGER.warn("Could not deserialize transport callbacks, dropping item transport");
            return null;
        }

        boolean firstPipe = tag.getBoolean("fp");
        int progressInCurrentPipe = tag.getInt("p");

        return new ItemTransport(value, source, destination, path, initialDirection, finishedCallback, cancelCallback, pipeGoneCallback, firstPipe, progressInCurrentPipe);
    }

    public ItemStack getValue() {
        return value;
    }

    public BlockPos getDestination() {
        return destination;
    }

    public Direction getDirection(ItemPipe currentPipe) {
        BlockPos nextPipe = path.peek();

        if (nextPipe == null) {
            return getDirection(currentPipe.getPos(), destination);
        }

        return getDirection(currentPipe.getPos(), nextPipe);
    }

    private boolean onDone(Network network, Level level, ItemPipe currentPipe) {
        finishedCallback.call(network, level, currentPipe.getPos(), cancelCallback);
        return true;
    }

    private boolean onPipeGone(Network network, Level level, BlockPos posWherePipeIsGone, String reason) {
        logPathBroken(posWherePipeIsGone, reason);
        pipeGoneCallback.call(network, level, posWherePipeIsGone, cancelCallback);
        return true;
    }

    /**
     * A broken path is not cosmetic: the callback hands the stack to whichever inventory in the
     * network will take it, and drops it on the ground if none will. So the warning has to stay —
     * but one line per affected item floods a busy server's log, and the old wording named
     * neither the place nor which of the two checks tripped, which made reports impossible to act
     * on. Report the details, then collapse repeats into a count.
     */
    private void logPathBroken(BlockPos brokenAt, String reason) {
        synchronized (LOG_LOCK) {
            long now = System.currentTimeMillis();
            if (now - lastLoggedAt < LOG_INTERVAL_MS) {
                suppressedSinceLastLog++;
                return;
            }

            if (suppressedSinceLastLog > 0) {
                LOGGER.warn(
                    "Item transport path broken at {} ({}), carrying {} to {}; {} further"
                        + " occurrences suppressed in the last {}s",
                    brokenAt, reason, value, destination, suppressedSinceLastLog,
                    LOG_INTERVAL_MS / 1000L);
            } else {
                LOGGER.warn("Item transport path broken at {} ({}), carrying {} to {}",
                    brokenAt, reason, value, destination);
            }

            suppressedSinceLastLog = 0;
            lastLoggedAt = now;
        }
    }

    public boolean update(Network network, ItemPipe currentPipe) {
        progressInCurrentPipe += 1;

        double progress = (double) progressInCurrentPipe / (double) getMaxTicksInPipe(currentPipe);

        BlockPos nextPos = currentPipe.getPos().relative(getDirection(currentPipe));
        // Only judge a neighbour we can actually see. Pipes tick even while their chunk is
        // unloaded, because Network#update walks every pipe in the network rather than relying
        // on block entity ticking, so this check ran against unloaded positions — reading one
        // forces a synchronous chunk load on the tick thread, the same hazard guarded everywhere
        // else since 1.3.5. An unloaded neighbour is unknown, not missing; wait for it to load.
        if (progress > 0.25
            && currentPipe.getLevel().isLoaded(nextPos)
            && currentPipe.getLevel().isEmptyBlock(nextPos)) {
            // Don't treat empty block as "pipe gone" if the current pipe has a void attachment facing that direction
            Direction dir = getDirection(currentPipe);
            Attachment att = currentPipe.getAttachmentManager().getAttachment(dir);
            if (att == null || !att.isVoidDestination()) {
                currentPipe.removeTransport(this);
                return onPipeGone(network, currentPipe.getLevel(), nextPos, "next block is air");
            }
        }

        if (progressInCurrentPipe >= getMaxTicksInPipe(currentPipe)) {
            currentPipe.removeTransport(this);
            firstPipe = false;

            BlockPos nextPipePos = path.poll();
            if (nextPipePos == null) {
                return onDone(network, currentPipe.getLevel(), currentPipe);
            }

            Pipe nextPipe = network.getPipe(nextPipePos);
            if (nextPipe == null) {
                return onPipeGone(network, currentPipe.getLevel(), nextPipePos,
                    "next pipe is no longer part of this network");
            }

            progressInCurrentPipe = 0;
            ((ItemPipe) nextPipe).addTransport(this);
        }

        return false;
    }

    private boolean isLastPipe() {
        return path.isEmpty();
    }

    private int getMaxTicksInPipe(ItemPipe currentPipe) {
        double mt = currentPipe.getMaxTicksInPipe();

        if (firstPipe) {
            mt *= 1.25D;
        }

        if (isLastPipe()) {
            mt *= 0.25D;
        }

        return (int) mt;
    }

    public ItemTransportProps createProps(ItemPipe currentPipe) {
        return new ItemTransportProps(
            value,
            getMaxTicksInPipe(currentPipe),
            progressInCurrentPipe,
            getDirection(currentPipe),
            initialDirection,
            isLastPipe(),
            firstPipe
        );
    }

    public CompoundTag writeToNbt(CompoundTag tag, HolderLookup.Provider registries) {
        // Use saveOptional with HolderLookup.Provider for MC 1.21.1
        tag.put("v", value.saveOptional(registries));
        tag.putLong("src", source.asLong());
        tag.putLong("dst", destination.asLong());

        ListTag path = new ListTag();
        for (BlockPos pathItem : this.path) {
            path.add(LongTag.valueOf(pathItem.asLong()));
        }
        tag.put("pth", path);

        tag.putInt("initd", initialDirection.ordinal());

        tag.put("fc", finishedCallback.writeToNbt(new CompoundTag(), registries));
        tag.putString("fcid", finishedCallback.getId().toString());
        tag.put("cc", cancelCallback.writeToNbt(new CompoundTag(), registries));
        tag.putString("ccid", cancelCallback.getId().toString());
        tag.put("pgc", pipeGoneCallback.writeToNbt(new CompoundTag(), registries));
        tag.putString("pgcid", pipeGoneCallback.getId().toString());

        tag.putBoolean("fp", firstPipe);
        tag.putInt("p", progressInCurrentPipe);

        return tag;
    }

    public ItemStack getStack() {
        return value;
    }

    public void tick() {
        progressInCurrentPipe++;
    }
} 
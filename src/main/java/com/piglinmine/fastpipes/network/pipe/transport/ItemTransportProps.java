package com.piglinmine.fastpipes.network.pipe.transport;

import net.minecraft.core.Direction;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.ItemStack;

public class ItemTransportProps {
    
    public static void encode(FriendlyByteBuf buffer, ItemTransportProps props) {
        buffer.writeItem(props.stack);
        buffer.writeInt(props.maxTicksInPipe);
        buffer.writeInt(props.progress);
        buffer.writeEnum(props.direction);
        buffer.writeEnum(props.initialDirection);
        buffer.writeBoolean(props.firstPipe);
        buffer.writeBoolean(props.lastPipe);
    }

    public static ItemTransportProps decode(FriendlyByteBuf buffer) {
        ItemStack stack = buffer.readItem();
        int maxTicksInPipe = buffer.readInt();
        int progress = buffer.readInt();
        Direction direction = buffer.readEnum(Direction.class);
        Direction initialDirection = buffer.readEnum(Direction.class);
        boolean firstPipe = buffer.readBoolean();
        boolean lastPipe = buffer.readBoolean();

        return new ItemTransportProps(stack, maxTicksInPipe, progress, direction, initialDirection, firstPipe, lastPipe);
    }

    private final ItemStack stack;
    private final int maxTicksInPipe;
    private int progress;
    private final Direction direction;
    private final Direction initialDirection;
    private final boolean firstPipe;
    private final boolean lastPipe;

    public ItemTransportProps(ItemStack stack, int maxTicksInPipe, int progress, Direction direction, Direction initialDirection, boolean firstPipe, boolean lastPipe) {
        this.stack = stack;
        this.maxTicksInPipe = maxTicksInPipe;
        this.progress = progress;
        this.direction = direction;
        this.initialDirection = initialDirection;
        this.firstPipe = firstPipe;
        this.lastPipe = lastPipe;
    }

    public ItemStack getStack() {
        return stack;
    }

    public int getMaxTicksInPipe() {
        return maxTicksInPipe;
    }

    public int getProgress() {
        return progress;
    }

    public Direction getDirection() {
        return direction;
    }

    public Direction getInitialDirection() {
        return initialDirection;
    }

    public boolean isFirstPipe() {
        return firstPipe;
    }

    public boolean isLastPipe() {
        return lastPipe;
    }

    public void tick() {
        progress++;
    }
} 
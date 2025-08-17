package com.piglinmine.fastpipes.network.pipe.transport;

import net.minecraft.core.Direction;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;

public class ItemTransportProps {
    
    // Manual StreamCodec implementation to avoid NeoForge composite limitations
    public static final StreamCodec<RegistryFriendlyByteBuf, ItemTransportProps> STREAM_CODEC = new StreamCodec<RegistryFriendlyByteBuf, ItemTransportProps>() {
        @Override
        public void encode(RegistryFriendlyByteBuf buffer, ItemTransportProps props) {
            ItemStack.STREAM_CODEC.encode(buffer, props.stack);
            buffer.writeInt(props.maxTicksInPipe);
            buffer.writeInt(props.progress);
            buffer.writeEnum(props.direction);
            buffer.writeEnum(props.initialDirection);
            buffer.writeBoolean(props.firstPipe);
            buffer.writeBoolean(props.lastPipe);
        }

        @Override
        public ItemTransportProps decode(RegistryFriendlyByteBuf buffer) {
            ItemStack stack = ItemStack.STREAM_CODEC.decode(buffer);
            int maxTicksInPipe = buffer.readInt();
            int progress = buffer.readInt();
            Direction direction = buffer.readEnum(Direction.class);
            Direction initialDirection = buffer.readEnum(Direction.class);
            boolean firstPipe = buffer.readBoolean();
            boolean lastPipe = buffer.readBoolean();
            
            return new ItemTransportProps(stack, maxTicksInPipe, progress, direction, initialDirection, firstPipe, lastPipe);
        }
    };

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
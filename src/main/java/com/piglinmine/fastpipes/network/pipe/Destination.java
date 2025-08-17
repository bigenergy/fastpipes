package com.piglinmine.fastpipes.network.pipe;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;

public class Destination {
    private final DestinationType type;
    private final BlockPos receiver;
    private final Direction incomingDirection;
    private final Pipe connectedPipe;

    public Destination(DestinationType type, BlockPos receiver, Direction incomingDirection, Pipe connectedPipe) {
        this.type = type;
        this.receiver = receiver;
        this.incomingDirection = incomingDirection;
        this.connectedPipe = connectedPipe;
    }

    public DestinationType getType() {
        return type;
    }

    public BlockPos getReceiver() {
        return receiver;
    }

    public Direction getIncomingDirection() {
        return incomingDirection;
    }

    public Pipe getConnectedPipe() {
        return connectedPipe;
    }
} 
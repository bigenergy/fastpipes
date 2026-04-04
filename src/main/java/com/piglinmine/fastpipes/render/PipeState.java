package com.piglinmine.fastpipes.render;

import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.Objects;

public class PipeState {
    @Nullable
    private final BlockState state;
    @Nullable
    private final ResourceLocation[] attachmentState;
    private final Direction side;
    private final RandomSource rand;
    @Nullable
    private final Integer colorId;

    public PipeState(@Nullable BlockState state, @Nullable ResourceLocation[] attachmentState, Direction side, RandomSource rand) {
        this(state, attachmentState, side, rand, null);
    }

    public PipeState(@Nullable BlockState state, @Nullable ResourceLocation[] attachmentState, Direction side, RandomSource rand, @Nullable Integer colorId) {
        this.state = state;
        this.attachmentState = attachmentState;
        this.side = side;
        this.rand = rand;
        this.colorId = colorId;
    }

    @Nullable
    public BlockState getState() {
        return state;
    }

    @Nullable
    public ResourceLocation[] getAttachmentState() {
        return attachmentState;
    }

    @Nullable
    public ResourceLocation getAttachmentState(Direction direction) {
        if (attachmentState == null) {
            return null;
        }

        return attachmentState[direction.ordinal()];
    }

    public boolean hasAttachmentState(Direction direction) {
        return getAttachmentState(direction) != null;
    }

    public Direction getSide() {
        return side;
    }

    public RandomSource getRand() {
        return rand;
    }

    @Nullable
    public Integer getColorId() {
        return colorId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PipeState pipeState = (PipeState) o;
        return Objects.equals(state, pipeState.state) &&
            Arrays.equals(attachmentState, pipeState.attachmentState) &&
            side == pipeState.side &&
            Objects.equals(colorId, pipeState.colorId);
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(state, side, colorId);
        result = 31 * result + Arrays.hashCode(attachmentState);
        return result;
    }
} 
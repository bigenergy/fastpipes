package com.piglinmine.fastpipes.network.pipe.attachment;

import com.piglinmine.fastpipes.network.pipe.Pipe;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;

public abstract class Attachment {
    protected final Pipe pipe;
    private final Direction direction;

    public Attachment(Pipe pipe, Direction direction) {
        this.pipe = pipe;
        this.direction = direction;
    }

    public Direction getDirection() {
        return direction;
    }

    public Pipe getPipe() {
        return pipe;
    }

    public CompoundTag writeToNbt(CompoundTag tag) {
        tag.putInt("dir", direction.ordinal());

        return tag;
    }

    public abstract void update();

    public abstract ResourceLocation getId();

    public abstract ItemStack getDrop();

    public void openContainer(ServerPlayer player) {
    }

    /**
     * Whether this attachment allows the adjacent block to be treated as a destination
     * in the item routing graph. Extractors return false (they are sources, not destinations).
     * Inserters return true (they explicitly mark a side as a destination).
     */
    public boolean isItemDestinationProvider() {
        return false;
    }

    /**
     * Whether this attachment allows the given item to be inserted into the adjacent block.
     * Only meaningful when {@link #isItemDestinationProvider()} returns true.
     */
    public boolean canInsert(ItemStack stack) {
        return true;
    }

    /**
     * Whether this attachment allows the given fluid to be pushed to the adjacent block.
     * Only meaningful for fluid pipe inserters.
     */
    public boolean canAcceptFluid(FluidStack stack) {
        return true;
    }

    /**
     * Priority for item routing. Higher value = items routed here first.
     * Only meaningful when {@link #isItemDestinationProvider()} returns true.
     */
    public int getInsertionPriority() {
        return 0;
    }

    /**
     * Whether this attachment acts as a void — destroying items/fluids that arrive.
     * Void destinations do not require an adjacent block entity or capability.
     */
    public boolean isVoidDestination() {
        return false;
    }
} 
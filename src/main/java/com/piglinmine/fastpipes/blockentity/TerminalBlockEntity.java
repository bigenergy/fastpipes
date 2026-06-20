package com.piglinmine.fastpipes.blockentity;

import com.piglinmine.fastpipes.FPipesBlockEntities;
import com.piglinmine.fastpipes.menu.TerminalContainerMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

import net.minecraft.core.Direction;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemStackHandler;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.UUID;

public class TerminalBlockEntity extends BaseBlockEntity {
    private final ItemStackHandler emptyHandler = new ItemStackHandler(0);
    private final ItemStack[] craftGrid = new ItemStack[9];
    private int sortModeOrdinal = 0; // 0 = NAME, matches SortMode.NAME.ordinal()
    @Nullable
    private UUID activeUser = null;
    @Nullable
    private String activeUserName = null;

    public TerminalBlockEntity(BlockPos pos, BlockState state) {
        super(FPipesBlockEntities.TERMINAL.get(), pos, state);
        Arrays.fill(craftGrid, ItemStack.EMPTY);
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        ValueOutput.TypedOutputList<ItemStack> list = output.list("CraftGrid", ItemStack.OPTIONAL_CODEC);
        for (ItemStack stack : craftGrid) {
            list.add(stack);
        }
        output.putInt("SortMode", sortModeOrdinal);
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        Arrays.fill(craftGrid, ItemStack.EMPTY);
        ValueInput.TypedInputList<ItemStack> list = input.listOrEmpty("CraftGrid", ItemStack.OPTIONAL_CODEC);
        int i = 0;
        for (ItemStack stack : list) {
            if (i >= 9) break;
            craftGrid[i++] = stack;
        }
        sortModeOrdinal = input.getIntOr("SortMode", 0);
    }

    public ItemStack[] getCraftGrid() {
        return craftGrid;
    }

    public void setCraftGrid(int slot, ItemStack stack) {
        if (slot >= 0 && slot < 9) {
            craftGrid[slot] = stack;
            setChanged();
        }
    }

    public int getSortModeOrdinal() {
        return sortModeOrdinal;
    }

    public void setSortModeOrdinal(int ordinal) {
        if (this.sortModeOrdinal != ordinal) {
            this.sortModeOrdinal = ordinal;
            setChanged();
        }
    }

    /**
     * Try to acquire the terminal lock for the given player.
     */
    public boolean tryAcquire(UUID uuid, String name) {
        if (activeUser != null && !activeUser.equals(uuid) && isStaleLock()) {
            activeUser = null;
            activeUserName = null;
        }
        if (activeUser == null || activeUser.equals(uuid)) {
            activeUser = uuid;
            activeUserName = name;
            return true;
        }
        return false;
    }

    private boolean isStaleLock() {
        if (activeUser == null) return true;
        if (level == null || level.isClientSide()) return false;
        MinecraftServer server = level.getServer();
        if (server == null) return true;
        ServerPlayer owner = server.getPlayerList().getPlayer(activeUser);
        if (owner == null) {
            return true;
        }
        if (!(owner.containerMenu instanceof TerminalContainerMenu menu)) {
            return true;
        }
        return !menu.getTerminalPos().equals(this.worldPosition);
    }

    public void release(UUID uuid) {
        if (activeUser != null && activeUser.equals(uuid)) {
            activeUser = null;
            activeUserName = null;
        }
    }

    @Nullable
    public String getActiveUserName() {
        return activeUserName;
    }

    public void forceRelease() {
        activeUser = null;
        activeUserName = null;
    }

    public IItemHandler getItemHandler(@Nullable Direction side) {
        return emptyHandler;
    }
}

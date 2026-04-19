package com.piglinmine.fastpipes.blockentity;

import com.piglinmine.fastpipes.FPipesBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

import net.minecraft.core.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;

import com.piglinmine.fastpipes.menu.TerminalContainerMenu;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.UUID;

public class TerminalBlockEntity extends BaseBlockEntity {
    private final ItemStack[] craftGrid = new ItemStack[9];
    private int sortModeOrdinal = 0; // 0 = NAME, matches SortMode.NAME.ordinal()
    @Nullable
    private UUID activeUser = null; // UUID of player currently using the terminal
    @Nullable
    private String activeUserName = null; // cached name for messaging
    private final LazyOptional<IItemHandler> itemHandler = LazyOptional.of(() -> new ItemStackHandler(0));

    public TerminalBlockEntity(BlockPos pos, BlockState state) {
        super(FPipesBlockEntities.TERMINAL.get(), pos, state);
        Arrays.fill(craftGrid, ItemStack.EMPTY);
    }

    @Override
    public <T> LazyOptional<T> getCapability(Capability<T> cap, @Nullable Direction side) {
        if (cap == ForgeCapabilities.ITEM_HANDLER) {
            return itemHandler.cast();
        }
        return super.getCapability(cap, side);
    }

    @Override
    public void invalidateCaps() {
        super.invalidateCaps();
        itemHandler.invalidate();
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        ListTag list = new ListTag();
        for (ItemStack stack : craftGrid) {
            if (stack.isEmpty()) {
                list.add(new CompoundTag());
            } else {
                list.add(stack.save(new CompoundTag()));
            }
        }
        tag.put("CraftGrid", list);
        tag.putInt("SortMode", sortModeOrdinal);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        if (tag.contains("CraftGrid")) {
            ListTag list = tag.getList("CraftGrid", CompoundTag.TAG_COMPOUND);
            for (int i = 0; i < Math.min(list.size(), 9); i++) {
                CompoundTag itemTag = list.getCompound(i);
                craftGrid[i] = itemTag.isEmpty() ? ItemStack.EMPTY :
                    ItemStack.of(itemTag);
            }
        }
        if (tag.contains("SortMode")) {
            sortModeOrdinal = tag.getInt("SortMode");
        }
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
     * @return true if lock acquired (or already owned by this player); false if another player holds it.
     */
    public boolean tryAcquire(UUID uuid, String name) {
        // If the stored owner is stale (offline OR their menu is no longer open on this terminal),
        // treat the lock as free. Prevents a crashed player from blocking the terminal forever.
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
        if (level == null || level.isClientSide) return false;
        MinecraftServer server = level.getServer();
        if (server == null) return true;
        ServerPlayer owner = server.getPlayerList().getPlayer(activeUser);
        if (owner == null) {
            // Player not online → lock is stale
            return true;
        }
        // Player online but their current menu is NOT a terminal menu pointing to this block
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

    /** Force-clear the lock. Useful if state got stuck after a crash. */
    public void forceRelease() {
        activeUser = null;
        activeUserName = null;
    }
}

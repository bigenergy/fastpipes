package com.piglinmine.fastpipes.menu;

import com.piglinmine.fastpipes.menu.slot.FilterSlot;
import com.piglinmine.fastpipes.menu.slot.FluidFilterSlot;
import com.piglinmine.fastpipes.network.FastPipesNetwork;
import com.piglinmine.fastpipes.network.message.FluidFilterSlotUpdateMessage;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.fluids.FluidStack;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class BaseContainerMenu extends AbstractContainerMenu {
    private final List<FluidFilterSlot> fluidSlots = new ArrayList<>();
    private final Player player;

    protected BaseContainerMenu(@Nullable MenuType<?> type, int windowId, Player player) {
        super(type, windowId);
        this.player = player;
    }

    protected void addPlayerInventory(int xInventory, int yInventory) {
        int id = 9;

        // Player inventory (3x9)
        for (int y = 0; y < 3; y++) {
            for (int x = 0; x < 9; x++) {
                addSlot(new Slot(player.getInventory(), id, xInventory + x * 18, yInventory + y * 18));
                id++;
            }
        }

        id = 0;

        // Player hotbar (1x9)
        for (int i = 0; i < 9; i++) {
            int x = xInventory + i * 18;
            int y = yInventory + 4 + (3 * 18);
            addSlot(new Slot(player.getInventory(), id, x, y));
            id++;
        }
    }

    @Override
    public void clicked(int id, int dragType, ClickType clickType, Player player) {
        Slot slot = id >= 0 ? getSlot(id) : null;
        ItemStack holding = player.containerMenu.getCarried();

        if (slot instanceof FilterSlot) {
            if (holding.isEmpty()) {
                slot.set(ItemStack.EMPTY);
            } else if (slot.mayPlace(holding)) {
                slot.set(holding.copy());
            }
            return;
        } else if (slot instanceof FluidFilterSlot fluidSlot) {
            FluidStack result = fluidSlot.onContainerClicked(holding);
            if (!player.level().isClientSide()) {
                FastPipesNetwork.sendToClient((ServerPlayer) player,
                    new FluidFilterSlotUpdateMessage(id, result));
            }
            return;
        }

        super.clicked(id, dragType, clickType, player);
    }

    @Override
    protected Slot addSlot(Slot slot) {
        if (slot instanceof FluidFilterSlot) {
            fluidSlots.add((FluidFilterSlot) slot);
        }
        return super.addSlot(slot);
    }

    @Override
    public boolean stillValid(Player player) {
        return true;
    }

    public List<FluidFilterSlot> getFluidSlots() {
        return fluidSlots;
    }

    /**
     * Position of the block this menu belongs to, or {@code null} when the menu is
     * not tied to a pipe attachment (e.g. the terminal).
     */
    @Nullable
    public BlockPos getPos() {
        return null;
    }

    /**
     * Side of the block this menu belongs to, or {@code null} when the menu is not
     * tied to a pipe attachment (e.g. the terminal).
     */
    @Nullable
    public Direction getDirection() {
        return null;
    }

    /**
     * Tag override ("#namespace:tag") configured for a filter slot, or an empty
     * string if the slot has none. Menus without tag-override support always
     * return an empty string.
     */
    public String getTagOverride(int slot) {
        return "";
    }

    /**
     * Sets the tag override of a filter slot and syncs it to the server. Menus
     * without tag-override support ignore this.
     */
    public void setTagOverride(int slot, String value) {
    }

    @Override
    public boolean canTakeItemForPickAll(ItemStack stack, Slot slot) {
        if (slot instanceof FilterSlot || slot instanceof FluidFilterSlot) {
            return false;
        }
        return super.canTakeItemForPickAll(stack, slot);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        return ItemStack.EMPTY;
    }
} 
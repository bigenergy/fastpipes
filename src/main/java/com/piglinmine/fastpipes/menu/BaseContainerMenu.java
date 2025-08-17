package com.piglinmine.fastpipes.menu;

import com.piglinmine.fastpipes.menu.slot.FilterSlot;
import com.piglinmine.fastpipes.menu.slot.FluidFilterSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

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
        } else if (slot instanceof FluidFilterSlot) {
            if (holding.isEmpty()) {
                ((FluidFilterSlot) slot).onContainerClicked(ItemStack.EMPTY);
            } else {
                ((FluidFilterSlot) slot).onContainerClicked(holding);
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
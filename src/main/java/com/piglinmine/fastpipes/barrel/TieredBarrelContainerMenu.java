package com.piglinmine.fastpipes.barrel;

import com.piglinmine.fastpipes.FPipesContainerMenus;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class TieredBarrelContainerMenu extends AbstractContainerMenu {
    private final Container container;
    private final BarrelTier tier;

    // Server constructor
    public TieredBarrelContainerMenu(int containerId, Inventory playerInventory, TieredBarrelBlockEntity be) {
        super(FPipesContainerMenus.TIERED_BARREL.get(), containerId);
        this.tier = be.getTier();
        this.container = be;
        be.startOpen(playerInventory.player);
        addBarrelSlots();
        addPlayerInventory(playerInventory);
    }

    // Client constructor
    public TieredBarrelContainerMenu(int containerId, Inventory playerInventory, BarrelTier tier) {
        super(FPipesContainerMenus.TIERED_BARREL.get(), containerId);
        this.tier = tier;
        this.container = new SimpleContainer(tier.getSlots());
        addBarrelSlots();
        addPlayerInventory(playerInventory);
    }

    private void addBarrelSlots() {
        int rows = tier.getRows();
        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < 9; col++) {
                addSlot(new Slot(container, col + row * 9, 8 + col * 18, 18 + row * 18));
            }
        }
    }

    private void addPlayerInventory(Inventory inv) {
        int yOffset = 32 + tier.getRows() * 18;

        // Player inventory
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                addSlot(new Slot(inv, col + row * 9 + 9, 8 + col * 18, yOffset + row * 18));
            }
        }
        // Hotbar
        for (int col = 0; col < 9; col++) {
            addSlot(new Slot(inv, col, 8 + col * 18, yOffset + 58));
        }
    }

    public BarrelTier getTier() {
        return tier;
    }

    @Override
    public boolean stillValid(Player player) {
        return container.stillValid(player);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        ItemStack result = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);
        if (slot != null && slot.hasItem()) {
            ItemStack slotStack = slot.getItem();
            result = slotStack.copy();

            int barrelSlots = tier.getSlots();
            if (index < barrelSlots) {
                // From barrel to player inventory
                if (!moveItemStackTo(slotStack, barrelSlots, barrelSlots + 36, true)) {
                    return ItemStack.EMPTY;
                }
            } else {
                // From player to barrel
                if (!moveItemStackTo(slotStack, 0, barrelSlots, false)) {
                    return ItemStack.EMPTY;
                }
            }

            if (slotStack.isEmpty()) {
                slot.set(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }
        }
        return result;
    }

    @Override
    public void removed(Player player) {
        super.removed(player);
        container.stopOpen(player);
    }
}

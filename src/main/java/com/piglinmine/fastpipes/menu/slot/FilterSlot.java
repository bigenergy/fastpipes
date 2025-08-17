package com.piglinmine.fastpipes.menu.slot;

import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.SlotItemHandler;

import javax.annotation.Nonnull;

public class FilterSlot extends SlotItemHandler {
    
    public FilterSlot(IItemHandler handler, int inventoryIndex, int x, int y) {
        super(handler, inventoryIndex, x, y);
    }

    @Override
    public void set(@Nonnull ItemStack stack) {
        if (!stack.isEmpty()) {
            stack.setCount(1);
        }
        super.set(stack);
    }
} 
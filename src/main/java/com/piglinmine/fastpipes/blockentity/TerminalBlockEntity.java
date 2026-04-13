package com.piglinmine.fastpipes.blockentity;

import com.piglinmine.fastpipes.FPipesBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Arrays;

public class TerminalBlockEntity extends BaseBlockEntity {
    private final ItemStack[] craftGrid = new ItemStack[9];

    public TerminalBlockEntity(BlockPos pos, BlockState state) {
        super(FPipesBlockEntities.TERMINAL.get(), pos, state);
        Arrays.fill(craftGrid, ItemStack.EMPTY);
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
}

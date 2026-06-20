package com.piglinmine.fastpipes.barrel;

import com.piglinmine.fastpipes.FPipesBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BaseContainerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.wrapper.InvWrapper;

import javax.annotation.Nullable;
import java.util.stream.IntStream;

public class TieredBarrelBlockEntity extends BaseContainerBlockEntity implements WorldlyContainer {
    private NonNullList<ItemStack> items;
    private final int[] slots;

    public TieredBarrelBlockEntity(BlockPos pos, BlockState state) {
        super(FPipesBlockEntities.TIERED_BARREL.get(), pos, state);
        BarrelTier tier = getTier();
        this.items = NonNullList.withSize(tier.getSlots(), ItemStack.EMPTY);
        this.slots = IntStream.range(0, tier.getSlots()).toArray();
    }

    public BarrelTier getTier() {
        if (getBlockState().getBlock() instanceof TieredBarrelBlock barrelBlock) {
            return barrelBlock.getTier();
        }
        return BarrelTier.OAK;
    }

    @Override
    protected Component getDefaultName() {
        return Component.translatable("block.fastpipes." + getTier().getRegistryName());
    }

    @Override
    protected NonNullList<ItemStack> getItems() {
        return items;
    }

    @Override
    protected void setItems(NonNullList<ItemStack> items) {
        this.items = items;
    }

    @Override
    public int getContainerSize() {
        return items.size();
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        ContainerHelper.saveAllItems(output, this.items);
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        BarrelTier tier = getTier();
        this.items = NonNullList.withSize(tier.getSlots(), ItemStack.EMPTY);
        ContainerHelper.loadAllItems(input, this.items);
        this.items = ensureSize(this.items, tier.getSlots());
    }

    private static NonNullList<ItemStack> ensureSize(NonNullList<ItemStack> list, int size) {
        if (list.size() == size) return list;
        NonNullList<ItemStack> resized = NonNullList.withSize(size, ItemStack.EMPTY);
        for (int i = 0; i < Math.min(list.size(), size); i++) {
            resized.set(i, list.get(i));
        }
        return resized;
    }

    @Override
    protected AbstractContainerMenu createMenu(int containerId, Inventory playerInventory) {
        return new TieredBarrelContainerMenu(containerId, playerInventory, this);
    }

    @Override
    public int[] getSlotsForFace(Direction side) {
        return slots;
    }

    @Override
    public boolean canPlaceItemThroughFace(int index, ItemStack stack, @Nullable Direction direction) {
        return true;
    }

    @Override
    public boolean canTakeItemThroughFace(int index, ItemStack stack, Direction direction) {
        return true;
    }

    public IItemHandler getItemHandler(@Nullable Direction side) {
        return new InvWrapper(this);
    }
}

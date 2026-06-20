package com.piglinmine.fastpipes.util;

import com.mojang.serialization.DataResult;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.RegistryOps;
import net.minecraft.world.item.ItemStack;

/**
 * TODO 1.21.11: ItemStack.parseOptional / saveOptional were removed in 1.21.11.
 * This helper wraps the codec-based replacement.
 */
public final class ItemStackSerialization {
    private ItemStackSerialization() {}

    public static ItemStack parseOptional(HolderLookup.Provider registries, CompoundTag tag) {
        if (tag == null || tag.isEmpty()) return ItemStack.EMPTY;
        RegistryOps<Tag> ops = registries.createSerializationContext(NbtOps.INSTANCE);
        DataResult<ItemStack> result = ItemStack.OPTIONAL_CODEC.parse(ops, tag);
        return result.result().orElse(ItemStack.EMPTY);
    }

    public static Tag saveOptional(HolderLookup.Provider registries, ItemStack stack) {
        if (stack.isEmpty()) return new CompoundTag();
        RegistryOps<Tag> ops = registries.createSerializationContext(NbtOps.INSTANCE);
        DataResult<Tag> result = ItemStack.OPTIONAL_CODEC.encodeStart(ops, stack);
        return result.result().orElseGet(CompoundTag::new);
    }

    public static net.neoforged.neoforge.fluids.FluidStack parseOptionalFluid(HolderLookup.Provider registries, CompoundTag tag) {
        if (tag == null || tag.isEmpty()) return net.neoforged.neoforge.fluids.FluidStack.EMPTY;
        RegistryOps<Tag> ops = registries.createSerializationContext(NbtOps.INSTANCE);
        DataResult<net.neoforged.neoforge.fluids.FluidStack> result = net.neoforged.neoforge.fluids.FluidStack.OPTIONAL_CODEC.parse(ops, tag);
        return result.result().orElse(net.neoforged.neoforge.fluids.FluidStack.EMPTY);
    }

    public static Tag saveOptionalFluid(HolderLookup.Provider registries, net.neoforged.neoforge.fluids.FluidStack stack) {
        if (stack.isEmpty()) return new CompoundTag();
        RegistryOps<Tag> ops = registries.createSerializationContext(NbtOps.INSTANCE);
        DataResult<Tag> result = net.neoforged.neoforge.fluids.FluidStack.OPTIONAL_CODEC.encodeStart(ops, stack);
        return result.result().orElseGet(CompoundTag::new);
    }
}

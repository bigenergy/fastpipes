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

    /**
     * Serializes a {@link net.neoforged.neoforge.items.ItemStackHandler} into a {@link CompoundTag}
     * via the new ValueOutput pipeline. Used by attachment NBT bridges that still operate on
     * raw CompoundTag.
     */
    public static CompoundTag saveItemStackHandler(HolderLookup.Provider registries,
                                                   net.neoforged.neoforge.items.ItemStackHandler handler) {
        net.minecraft.world.level.storage.TagValueOutput out =
            net.minecraft.world.level.storage.TagValueOutput.createWithContext(
                net.minecraft.util.ProblemReporter.DISCARDING, registries);
        handler.serialize(out);
        return out.buildResult();
    }

    /**
     * Loads an {@link net.neoforged.neoforge.items.ItemStackHandler}'s state from a
     * {@link CompoundTag} via the new ValueInput pipeline.
     */
    public static void loadItemStackHandler(HolderLookup.Provider registries,
                                            net.neoforged.neoforge.items.ItemStackHandler handler,
                                            CompoundTag tag) {
        if (tag == null || tag.isEmpty()) return;
        net.minecraft.world.level.storage.ValueInput in =
            net.minecraft.world.level.storage.TagValueInput.create(
                net.minecraft.util.ProblemReporter.DISCARDING, registries, tag);
        handler.deserialize(in);
    }
}

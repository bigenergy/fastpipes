package com.piglinmine.fastpipes.network.pipe.attachment.inserter;

import com.piglinmine.fastpipes.inventory.fluid.FluidInventory;
import com.piglinmine.fastpipes.menu.InserterAttachmentMenuProvider;
import com.piglinmine.fastpipes.network.NetworkManager;
import com.piglinmine.fastpipes.network.fluid.FluidNetwork;
import com.piglinmine.fastpipes.network.pipe.Pipe;
import com.piglinmine.fastpipes.network.pipe.attachment.Attachment;
import com.piglinmine.fastpipes.network.pipe.attachment.extractor.BlacklistWhitelist;
import com.piglinmine.fastpipes.network.pipe.attachment.extractor.RedstoneMode;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.items.ItemStackHandler;

import java.util.Arrays;
import javax.annotation.Nullable;

public class InserterAttachment extends Attachment {
    public static final int MAX_FILTER_SLOTS = 15;

    private final InserterAttachmentType type;
    private final ItemStackHandler itemFilter;
    private final FluidInventory fluidFilter;

    private RedstoneMode redstoneMode = RedstoneMode.IGNORED;
    private BlacklistWhitelist blacklistWhitelist = BlacklistWhitelist.BLACKLIST;
    private boolean exactMode = false;
    private final String[] tagOverrides = new String[MAX_FILTER_SLOTS];

    public InserterAttachment(Pipe pipe, Direction direction, InserterAttachmentType type) {
        super(pipe, direction);
        this.type = type;
        this.itemFilter = createItemFilterInventory(this);
        this.fluidFilter = createFluidFilterInventory(this);
        Arrays.fill(this.tagOverrides, "");
    }

    public static ItemStackHandler createItemFilterInventory(@Nullable InserterAttachment attachment) {
        return new ItemStackHandler(MAX_FILTER_SLOTS) {
            @Override
            protected void onContentsChanged(int slot) {
                super.onContentsChanged(slot);
                if (attachment != null) {
                    NetworkManager.get(attachment.pipe.getLevel()).setDirty();
                }
            }
        };
    }

    public static FluidInventory createFluidFilterInventory(@Nullable InserterAttachment attachment) {
        return new FluidInventory(MAX_FILTER_SLOTS) {
            @Override
            protected void onContentsChanged() {
                super.onContentsChanged();
                if (attachment != null) {
                    NetworkManager.get(attachment.pipe.getLevel()).setDirty();
                }
            }
        };
    }

    public boolean isFluidMode() {
        return pipe.getNetwork() instanceof FluidNetwork;
    }

    @Override
    public void update() {
        // Inserter is passive — it marks its side as a destination and filters items/fluids routed here.
    }

    @Override
    public boolean isItemDestinationProvider() {
        return true;
    }

    @Override
    public boolean canInsert(ItemStack stack) {
        if (!redstoneMode.isEnabled(pipe.getLevel(), pipe.getPos())) {
            return false;
        }

        boolean hasFilter = false;
        for (int i = 0; i < itemFilter.getSlots(); ++i) {
            if (!itemFilter.getStackInSlot(i).isEmpty() || !tagOverrides[i].isEmpty()) {
                hasFilter = true;
                break;
            }
        }
        if (!hasFilter) return true;

        if (blacklistWhitelist == BlacklistWhitelist.WHITELIST) {
            for (int i = 0; i < itemFilter.getSlots(); ++i) {
                ItemStack filtered = itemFilter.getStackInSlot(i);
                String override = tagOverrides[i];

                boolean equals;
                if (!override.isEmpty() && override.startsWith("#")) {
                    ResourceLocation tagId = ResourceLocation.tryParse(override.substring(1));
                    if (tagId != null) {
                        TagKey<Item> tag = TagKey.create(Registries.ITEM, tagId);
                        equals = stack.is(tag);
                    } else {
                        equals = false;
                    }
                } else if (filtered.isEmpty()) {
                    continue;
                } else {
                    equals = filtered.is(stack.getItem());
                    if (exactMode) equals = equals && ItemStack.isSameItemSameComponents(filtered, stack);
                }

                if (equals) return true;
            }
            return false;
        } else {
            for (int i = 0; i < itemFilter.getSlots(); ++i) {
                ItemStack filtered = itemFilter.getStackInSlot(i);
                String override = tagOverrides[i];

                boolean equals;
                if (!override.isEmpty() && override.startsWith("#")) {
                    ResourceLocation tagId = ResourceLocation.tryParse(override.substring(1));
                    if (tagId != null) {
                        TagKey<Item> tag = TagKey.create(Registries.ITEM, tagId);
                        equals = stack.is(tag);
                    } else {
                        equals = false;
                    }
                } else if (filtered.isEmpty()) {
                    continue;
                } else {
                    equals = filtered.is(stack.getItem());
                    if (exactMode) equals = equals && ItemStack.isSameItemSameComponents(filtered, stack);
                }

                if (equals) return false;
            }
            return true;
        }
    }

    @Override
    public boolean canAcceptFluid(FluidStack stack) {
        if (!redstoneMode.isEnabled(pipe.getLevel(), pipe.getPos())) {
            return false;
        }

        boolean hasFilter = false;
        for (int i = 0; i < fluidFilter.getSlots(); ++i) {
            if (!fluidFilter.getFluid(i).isEmpty() || !tagOverrides[i].isEmpty()) {
                hasFilter = true;
                break;
            }
        }
        if (!hasFilter) return true;

        if (blacklistWhitelist == BlacklistWhitelist.WHITELIST) {
            for (int i = 0; i < fluidFilter.getSlots(); ++i) {
                FluidStack filtered = fluidFilter.getFluid(i);
                String override = tagOverrides[i];

                boolean equals;
                if (!override.isEmpty() && override.startsWith("#")) {
                    ResourceLocation tagId = ResourceLocation.tryParse(override.substring(1));
                    if (tagId != null) {
                        TagKey<Fluid> tag = TagKey.create(Registries.FLUID, tagId);
                        equals = stack.getFluid().builtInRegistryHolder().is(tag);
                    } else {
                        equals = false;
                    }
                } else if (filtered.isEmpty()) {
                    continue;
                } else {
                    equals = filtered.getFluid() == stack.getFluid();
                    if (exactMode) equals = equals && FluidStack.isSameFluidSameComponents(filtered, stack);
                }

                if (equals) return true;
            }
            return false;
        } else {
            for (int i = 0; i < fluidFilter.getSlots(); ++i) {
                FluidStack filtered = fluidFilter.getFluid(i);
                String override = tagOverrides[i];

                boolean equals;
                if (!override.isEmpty() && override.startsWith("#")) {
                    ResourceLocation tagId = ResourceLocation.tryParse(override.substring(1));
                    if (tagId != null) {
                        TagKey<Fluid> tag = TagKey.create(Registries.FLUID, tagId);
                        equals = stack.getFluid().builtInRegistryHolder().is(tag);
                    } else {
                        equals = false;
                    }
                } else if (filtered.isEmpty()) {
                    continue;
                } else {
                    equals = filtered.getFluid() == stack.getFluid();
                    if (exactMode) equals = equals && FluidStack.isSameFluidSameComponents(filtered, stack);
                }

                if (equals) return false;
            }
            return true;
        }
    }

    @Override
    public int getInsertionPriority() {
        return type.getPriority();
    }

    @Override
    public ResourceLocation getId() {
        return type.getId();
    }

    @Override
    public ItemStack getDrop() {
        return new ItemStack(type.getItem());
    }

    @Override
    public void openContainer(ServerPlayer player) {
        super.openContainer(player);
        InserterAttachmentMenuProvider.open(pipe, this, player);
    }

    @Override
    public CompoundTag writeToNbt(CompoundTag tag) {
        tag.putByte("rm", (byte) redstoneMode.ordinal());
        tag.putByte("bw", (byte) blacklistWhitelist.ordinal());
        tag.putBoolean("exa", exactMode);
        tag.put("itemfilter", itemFilter.serializeNBT(pipe.getLevel().registryAccess()));
        tag.put("fluidfilter", fluidFilter.writeToNbt(pipe.getLevel().registryAccess()));
        CompoundTag overridesTag = new CompoundTag();
        for (int i = 0; i < MAX_FILTER_SLOTS; i++) {
            if (!tagOverrides[i].isEmpty()) {
                overridesTag.putString("s" + i, tagOverrides[i]);
            }
        }
        if (!overridesTag.isEmpty()) {
            tag.put("tagov", overridesTag);
        }
        return super.writeToNbt(tag);
    }

    public InserterAttachmentType getType() { return type; }
    public ItemStackHandler getItemFilter() { return itemFilter; }
    public FluidInventory getFluidFilter() { return fluidFilter; }

    public RedstoneMode getRedstoneMode() { return redstoneMode; }
    public void setRedstoneMode(RedstoneMode mode) {
        if (!type.getCanSetRedstoneMode()) return;
        this.redstoneMode = mode;
    }

    public BlacklistWhitelist getBlacklistWhitelist() { return blacklistWhitelist; }
    public void setBlacklistWhitelist(BlacklistWhitelist bw) {
        if (!type.getCanSetWhitelistBlacklist()) return;
        this.blacklistWhitelist = bw;
    }

    public boolean isExactMode() { return exactMode; }
    public void setExactMode(boolean exactMode) {
        if (!type.getCanSetExactMode()) return;
        this.exactMode = exactMode;
    }

    public String getTagOverride(int slot) {
        if (slot < 0 || slot >= MAX_FILTER_SLOTS) return "";
        return tagOverrides[slot];
    }

    public void setTagOverride(int slot, String value) {
        if (slot < 0 || slot >= MAX_FILTER_SLOTS) return;
        tagOverrides[slot] = value != null ? value : "";
    }

    public String[] getTagOverrides() {
        return tagOverrides;
    }
}

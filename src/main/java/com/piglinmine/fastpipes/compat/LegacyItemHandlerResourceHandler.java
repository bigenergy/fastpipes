package com.piglinmine.fastpipes.compat;

import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.transaction.SnapshotJournal;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;

/**
 * Adapter that exposes a legacy {@link IItemHandler} as a NeoForge 1.21.11
 * {@link ResourceHandler}{@code <ItemResource>}.
 *
 * <p>This is the reverse of {@link IItemHandler#of(ResourceHandler)}: it lets us continue using
 * existing {@code IItemHandler}-based code (filters, pipe item buffers, container wrappers) while
 * still registering for the new {@link net.neoforged.neoforge.capabilities.Capabilities.Item#BLOCK}
 * capability that adjacent machines query.
 *
 * <p>Transaction safety: the legacy {@code IItemHandler} has no native transaction concept, so we
 * use a {@link SnapshotJournal} to remember the pre-modification slot contents and roll back via
 * {@code IItemHandler.extractItem} / {@code insertItem} (or via {@link IItemHandlerModifiable}
 * where available). For our use case (pipes/terminals queried as item sinks), the wrapper is only
 * meaningful for {@code insert}; {@code extract} delegates similarly. Rollback uses the inverse
 * legacy call.
 */
public class LegacyItemHandlerResourceHandler extends SnapshotJournal<LegacyItemHandlerResourceHandler.Snapshot> implements ResourceHandler<ItemResource> {
    private final IItemHandler legacy;

    public LegacyItemHandlerResourceHandler(IItemHandler legacy) {
        this.legacy = legacy;
    }

    @Override
    public int size() {
        return legacy.getSlots();
    }

    @Override
    public ItemResource getResource(int index) {
        ItemStack stack = legacy.getStackInSlot(index);
        return stack.isEmpty() ? ItemResource.EMPTY : ItemResource.of(stack);
    }

    @Override
    public long getAmountAsLong(int index) {
        return legacy.getStackInSlot(index).getCount();
    }

    @Override
    public long getCapacityAsLong(int index, ItemResource resource) {
        int slotLimit = legacy.getSlotLimit(index);
        if (resource.isEmpty()) {
            return slotLimit;
        }
        return Math.min(slotLimit, resource.getMaxStackSize());
    }

    @Override
    public boolean isValid(int index, ItemResource resource) {
        if (resource.isEmpty()) return true;
        return legacy.isItemValid(index, resource.toStack(1));
    }

    @Override
    public int insert(int index, ItemResource resource, int amount, TransactionContext transaction) {
        if (resource.isEmpty() || amount <= 0) return 0;
        // Simulate to see how much fits, then commit only if the transaction is committed.
        ItemStack offered = resource.toStack(Math.min(amount, resource.getMaxStackSize()));
        ItemStack leftover = legacy.insertItem(index, offered, true);
        int accepted = offered.getCount() - leftover.getCount();
        if (accepted <= 0) return 0;
        updateSnapshots(transaction);
        legacy.insertItem(index, resource.toStack(accepted), false);
        return accepted;
    }

    @Override
    public int insert(ItemResource resource, int amount, TransactionContext transaction) {
        if (resource.isEmpty() || amount <= 0) return 0;
        // Multi-slot insertion: distribute across all slots
        int slots = legacy.getSlots();
        int totalInserted = 0;
        int remaining = amount;
        for (int i = 0; i < slots && remaining > 0; i++) {
            int inserted = insert(i, resource, remaining, transaction);
            totalInserted += inserted;
            remaining -= inserted;
        }
        return totalInserted;
    }

    @Override
    public int extract(int index, ItemResource resource, int amount, TransactionContext transaction) {
        if (resource.isEmpty() || amount <= 0) return 0;
        ItemStack inSlot = legacy.getStackInSlot(index);
        if (inSlot.isEmpty()) return 0;
        if (!ItemResource.of(inSlot).equals(resource)) return 0;
        ItemStack simulated = legacy.extractItem(index, amount, true);
        if (simulated.isEmpty()) return 0;
        updateSnapshots(transaction);
        ItemStack extracted = legacy.extractItem(index, amount, false);
        return extracted.getCount();
    }

    @Override
    public int extract(ItemResource resource, int amount, TransactionContext transaction) {
        if (resource.isEmpty() || amount <= 0) return 0;
        int slots = legacy.getSlots();
        int totalExtracted = 0;
        int remaining = amount;
        for (int i = 0; i < slots && remaining > 0; i++) {
            int extracted = extract(i, resource, remaining, transaction);
            totalExtracted += extracted;
            remaining -= extracted;
        }
        return totalExtracted;
    }

    @Override
    protected Snapshot createSnapshot() {
        int slots = legacy.getSlots();
        ItemStack[] snap = new ItemStack[slots];
        for (int i = 0; i < slots; i++) {
            snap[i] = legacy.getStackInSlot(i).copy();
        }
        return new Snapshot(snap);
    }

    @Override
    protected void revertToSnapshot(Snapshot snapshot) {
        // Forcefully restore each slot. We can't directly set slot contents through legacy
        // IItemHandler (only IItemHandlerModifiable allows that), so we extract everything
        // and re-insert the snapshot. This is best-effort: handlers that reject items they
        // previously contained will lose data on rollback. In practice our use sites do not
        // depend on transactional rollback semantics for these handlers.
        int slots = legacy.getSlots();
        if (legacy instanceof net.neoforged.neoforge.items.IItemHandlerModifiable mod) {
            for (int i = 0; i < slots && i < snapshot.stacks.length; i++) {
                mod.setStackInSlot(i, snapshot.stacks[i]);
            }
            return;
        }
        // Fallback: drain all and reinsert. Best-effort.
        for (int i = 0; i < slots; i++) {
            ItemStack cur = legacy.getStackInSlot(i);
            if (!cur.isEmpty()) {
                legacy.extractItem(i, cur.getCount(), false);
            }
        }
        for (int i = 0; i < slots && i < snapshot.stacks.length; i++) {
            if (!snapshot.stacks[i].isEmpty()) {
                legacy.insertItem(i, snapshot.stacks[i], false);
            }
        }
    }

    /** Per-slot snapshot used to roll back failed transactions. */
    static final class Snapshot {
        final ItemStack[] stacks;

        Snapshot(ItemStack[] stacks) {
            this.stacks = stacks;
        }
    }
}

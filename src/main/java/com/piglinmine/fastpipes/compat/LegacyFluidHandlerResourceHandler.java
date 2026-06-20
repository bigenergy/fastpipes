package com.piglinmine.fastpipes.compat;

import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import net.neoforged.neoforge.transfer.transaction.SnapshotJournal;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;

/**
 * Adapter exposing a legacy {@link IFluidHandler} as a NeoForge 1.21.11
 * {@link ResourceHandler}{@code <FluidResource>} for the new
 * {@link net.neoforged.neoforge.capabilities.Capabilities.Fluid#BLOCK} capability.
 *
 * <p>Transactions are simulated via {@link SnapshotJournal}: snapshots capture the entire tank
 * contents, and rollback drains-then-fills back to the captured state. Best-effort for
 * handlers that mutate beyond our visibility.
 */
public class LegacyFluidHandlerResourceHandler extends SnapshotJournal<LegacyFluidHandlerResourceHandler.Snapshot> implements ResourceHandler<FluidResource> {
    private final IFluidHandler legacy;

    public LegacyFluidHandlerResourceHandler(IFluidHandler legacy) {
        this.legacy = legacy;
    }

    @Override
    public int size() {
        return legacy.getTanks();
    }

    @Override
    public FluidResource getResource(int index) {
        FluidStack stack = legacy.getFluidInTank(index);
        return stack.isEmpty() ? FluidResource.EMPTY : FluidResource.of(stack);
    }

    @Override
    public long getAmountAsLong(int index) {
        return legacy.getFluidInTank(index).getAmount();
    }

    @Override
    public long getCapacityAsLong(int index, FluidResource resource) {
        return legacy.getTankCapacity(index);
    }

    @Override
    public boolean isValid(int index, FluidResource resource) {
        if (resource.isEmpty()) return true;
        return legacy.isFluidValid(index, resource.toStack(1));
    }

    @Override
    public int insert(int index, FluidResource resource, int amount, TransactionContext transaction) {
        // Legacy IFluidHandler doesn't have per-tank insert; just delegate to global.
        return insert(resource, amount, transaction);
    }

    @Override
    public int insert(FluidResource resource, int amount, TransactionContext transaction) {
        if (resource.isEmpty() || amount <= 0) return 0;
        FluidStack offered = resource.toStack(amount);
        int simulated = legacy.fill(offered, IFluidHandler.FluidAction.SIMULATE);
        if (simulated <= 0) return 0;
        updateSnapshots(transaction);
        return legacy.fill(resource.toStack(simulated), IFluidHandler.FluidAction.EXECUTE);
    }

    @Override
    public int extract(int index, FluidResource resource, int amount, TransactionContext transaction) {
        return extract(resource, amount, transaction);
    }

    @Override
    public int extract(FluidResource resource, int amount, TransactionContext transaction) {
        if (resource.isEmpty() || amount <= 0) return 0;
        FluidStack wanted = resource.toStack(amount);
        FluidStack simulated = legacy.drain(wanted, IFluidHandler.FluidAction.SIMULATE);
        if (simulated.isEmpty()) return 0;
        updateSnapshots(transaction);
        FluidStack drained = legacy.drain(wanted, IFluidHandler.FluidAction.EXECUTE);
        return drained.getAmount();
    }

    @Override
    protected Snapshot createSnapshot() {
        int tanks = legacy.getTanks();
        FluidStack[] snap = new FluidStack[tanks];
        for (int i = 0; i < tanks; i++) {
            snap[i] = legacy.getFluidInTank(i).copy();
        }
        return new Snapshot(snap);
    }

    @Override
    protected void revertToSnapshot(Snapshot snapshot) {
        // Best-effort rollback: drain all, then re-fill snapshot contents
        for (int i = 0; i < legacy.getTanks(); i++) {
            FluidStack cur = legacy.getFluidInTank(i);
            if (!cur.isEmpty()) {
                legacy.drain(cur, IFluidHandler.FluidAction.EXECUTE);
            }
        }
        for (FluidStack s : snapshot.stacks) {
            if (s != null && !s.isEmpty()) {
                legacy.fill(s, IFluidHandler.FluidAction.EXECUTE);
            }
        }
    }

    static final class Snapshot {
        final FluidStack[] stacks;

        Snapshot(FluidStack[] stacks) {
            this.stacks = stacks;
        }
    }
}

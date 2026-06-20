package com.piglinmine.fastpipes.compat;

import net.neoforged.neoforge.energy.IEnergyStorage;
import net.neoforged.neoforge.transfer.energy.EnergyHandler;
import net.neoforged.neoforge.transfer.transaction.SnapshotJournal;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;

/**
 * Adapter exposing a legacy {@link IEnergyStorage} as a NeoForge 1.21.11 {@link EnergyHandler}
 * for the new {@link net.neoforged.neoforge.capabilities.Capabilities.Energy#BLOCK} capability.
 *
 * <p>Transactions are simulated via a {@link SnapshotJournal} that records the delta inserted /
 * extracted and reverses it on rollback. This works for any well-behaved {@code IEnergyStorage}
 * that conserves energy across receive/extract round trips.
 */
public class LegacyEnergyHandlerAdapter extends SnapshotJournal<LegacyEnergyHandlerAdapter.Snapshot> implements EnergyHandler {
    private final IEnergyStorage legacy;

    public LegacyEnergyHandlerAdapter(IEnergyStorage legacy) {
        this.legacy = legacy;
    }

    @Override
    public long getAmountAsLong() {
        return legacy.getEnergyStored();
    }

    @Override
    public long getCapacityAsLong() {
        return legacy.getMaxEnergyStored();
    }

    @Override
    public int insert(int amount, TransactionContext transaction) {
        if (amount <= 0) return 0;
        int simulated = legacy.receiveEnergy(amount, true);
        if (simulated <= 0) return 0;
        updateSnapshots(transaction);
        return legacy.receiveEnergy(amount, false);
    }

    @Override
    public int extract(int amount, TransactionContext transaction) {
        if (amount <= 0) return 0;
        int simulated = legacy.extractEnergy(amount, true);
        if (simulated <= 0) return 0;
        updateSnapshots(transaction);
        return legacy.extractEnergy(amount, false);
    }

    @Override
    protected Snapshot createSnapshot() {
        return new Snapshot(legacy.getEnergyStored());
    }

    @Override
    protected void revertToSnapshot(Snapshot snapshot) {
        int current = legacy.getEnergyStored();
        if (current > snapshot.amount) {
            // Too much energy now — extract delta
            legacy.extractEnergy(current - snapshot.amount, false);
        } else if (current < snapshot.amount) {
            // Too little — push delta back
            legacy.receiveEnergy(snapshot.amount - current, false);
        }
    }

    static final class Snapshot {
        final int amount;

        Snapshot(int amount) {
            this.amount = amount;
        }
    }
}

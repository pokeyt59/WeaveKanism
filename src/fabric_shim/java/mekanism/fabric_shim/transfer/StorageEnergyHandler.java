package mekanism.fabric_shim.transfer;

import java.util.Objects;
import mekanism.fabric_shim.energy.IEnergyStorage;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import team.reborn.energy.api.EnergyStorage;

/**
 * Wraps an external team-reborn {@link EnergyStorage} as the shim (Forge-shaped)
 * {@link IEnergyStorage}, for Mekanism machines/cables pushing into or pulling from neighboring
 * Fabric energy blocks.
 *
 * <p>No unit math happens here — team-reborn units are FE-equivalent 1:1, and the J↔FE conversion
 * is done by Mekanism's own {@code ForgeStrictEnergyHandler} wrapping this (so the configured
 * conversion rate and its clamping semantics are reused, not reimplemented).
 *
 * <p><b>Semantics (transfer-bridge.md):</b> simulate = open a transaction, operate, abort;
 * execute = same but commit.
 */
public class StorageEnergyHandler implements IEnergyStorage {

    private final EnergyStorage storage;

    public StorageEnergyHandler(EnergyStorage storage) {
        this.storage = Objects.requireNonNull(storage);
    }

    @Override
    public int receiveEnergy(int toReceive, boolean simulate) {
        if (toReceive <= 0) {
            return 0;
        }
        try (Transaction tx = Transaction.openOuter()) {
            long inserted = storage.insert(toReceive, tx);
            if (!simulate) {
                tx.commit();
            }
            return (int) inserted;
        }
    }

    @Override
    public int extractEnergy(int toExtract, boolean simulate) {
        if (toExtract <= 0) {
            return 0;
        }
        try (Transaction tx = Transaction.openOuter()) {
            long extracted = storage.extract(toExtract, tx);
            if (!simulate) {
                tx.commit();
            }
            return (int) extracted;
        }
    }

    @Override
    public int getEnergyStored() {
        return (int) Math.min(storage.getAmount(), Integer.MAX_VALUE);
    }

    @Override
    public int getMaxEnergyStored() {
        return (int) Math.min(storage.getCapacity(), Integer.MAX_VALUE);
    }

    @Override
    public boolean canExtract() {
        return storage.supportsExtraction();
    }

    @Override
    public boolean canReceive() {
        return storage.supportsInsertion();
    }
}

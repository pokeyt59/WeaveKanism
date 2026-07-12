package mekanism.fabric_shim.transfer;

import java.util.List;
import java.util.Objects;
import mekanism.api.energy.IEnergyContainer;
import mekanism.fabric_shim.energy.IEnergyStorage;
import net.fabricmc.fabric.api.transfer.v1.storage.StoragePreconditions;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.fabricmc.fabric.api.transfer.v1.transaction.base.SnapshotParticipant;
import team.reborn.energy.api.EnergyStorage;

/**
 * Exposes a Mekanism tile's energy to Fabric consumers for one side, with full side-config
 * fidelity: operations route through the tile's own FE-shaped {@link IEnergyStorage} provider
 * (Mekanism's ForgeEnergyIntegration over the side-aware strict handler), so the configured J↔FE
 * conversion, its clamping, and per-side permissions are all Mekanism's own code. Rollback
 * snapshots the side's backing {@link IEnergyContainer}s and restores via {@code setEnergy}.
 *
 * <p>Amount/capacity views clamp to int (FE is int-shaped) — same clamping NeoForge FE consumers
 * see on huge Mekanism storage.
 *
 * <p>User-approved expose design (2026-07-11): operations via the permission-enforcing handler,
 * rollback via the containers.
 */
public class ProxiedEnergyStorage extends SnapshotParticipant<long[]> implements EnergyStorage {

    private final IEnergyStorage handler;
    private final List<IEnergyContainer> containers;

    public ProxiedEnergyStorage(IEnergyStorage handler, List<IEnergyContainer> containers) {
        this.handler = Objects.requireNonNull(handler);
        this.containers = List.copyOf(containers);
    }

    @Override
    public long insert(long maxAmount, TransactionContext transaction) {
        StoragePreconditions.notNegative(maxAmount);
        int toInsert = (int) Math.min(maxAmount, Integer.MAX_VALUE);
        if (toInsert <= 0) {
            return 0;
        }
        updateSnapshots(transaction);
        return handler.receiveEnergy(toInsert, false);
    }

    @Override
    public long extract(long maxAmount, TransactionContext transaction) {
        StoragePreconditions.notNegative(maxAmount);
        int toExtract = (int) Math.min(maxAmount, Integer.MAX_VALUE);
        if (toExtract <= 0) {
            return 0;
        }
        updateSnapshots(transaction);
        return handler.extractEnergy(toExtract, false);
    }

    @Override
    public boolean supportsInsertion() {
        return handler.canReceive();
    }

    @Override
    public boolean supportsExtraction() {
        return handler.canExtract();
    }

    @Override
    public long getAmount() {
        return handler.getEnergyStored();
    }

    @Override
    public long getCapacity() {
        return handler.getMaxEnergyStored();
    }

    @Override
    protected long[] createSnapshot() {
        long[] snapshot = new long[containers.size()];
        for (int i = 0; i < containers.size(); i++) {
            snapshot[i] = containers.get(i).getEnergy();
        }
        return snapshot;
    }

    @Override
    protected void readSnapshot(long[] snapshot) {
        for (int i = 0; i < containers.size(); i++) {
            containers.get(i).setEnergy(snapshot[i]);
        }
    }
}

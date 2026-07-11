package mekanism.fabric_shim.transfer;

import java.util.Objects;
import mekanism.api.Action;
import mekanism.api.AutomationType;
import mekanism.api.energy.IEnergyContainer;
import mekanism.api.energy.IEnergyConversion;
import net.fabricmc.fabric.api.transfer.v1.storage.StoragePreconditions;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.fabricmc.fabric.api.transfer.v1.transaction.base.SnapshotParticipant;
import team.reborn.energy.api.EnergyStorage;

/**
 * Exposes a Mekanism-owned {@link IEnergyContainer} (Joules) to Fabric consumers as a team-reborn
 * {@link EnergyStorage} (FE-equivalent units).
 *
 * <p><b>Design rule (transfer-bridge.md):</b> Mekanism→Fabric adapts only containers with setters;
 * simulation is the caller aborting the transaction, rollback via {@link SnapshotParticipant}.
 *
 * <p><b>Conversion:</b> all J↔unit math goes through the supplied {@link IEnergyConversion}
 * (wiring passes Mekanism's own {@code EnergyUnit.FORGE_ENERGY}, i.e. the configured rate — never a
 * hardcoded one). Boundary amounts are clamped the same way Mekanism's own
 * {@code ForgeEnergyIntegration} clamps them: convert, then convert back-and-forth so only amounts
 * that map to a whole number of external units may move — flooring always favors the other side,
 * so a round trip can never create or destroy energy.
 */
public class EnergyContainerStorage extends SnapshotParticipant<Long> implements EnergyStorage {

    private final IEnergyContainer container;
    private final IEnergyConversion converter;

    public EnergyContainerStorage(IEnergyContainer container, IEnergyConversion converter) {
        this.container = Objects.requireNonNull(container);
        this.converter = Objects.requireNonNull(converter);
    }

    @Override
    public long insert(long maxAmount, TransactionContext transaction) {
        StoragePreconditions.notNegative(maxAmount);
        long toInsert = converter.convertFrom(maxAmount);
        if (toInsert <= 0) {
            return 0;
        }
        if (!converter.isOneToOne()) {
            //Simulate first to learn how much the container takes, then clamp that to a whole
            // number of external units so the requester is never charged for a partial unit
            long simulatedRemainder = container.insert(toInsert, Action.SIMULATE, AutomationType.EXTERNAL);
            if (simulatedRemainder == toInsert) {
                return 0;
            }
            toInsert = convertToAndBack(toInsert - simulatedRemainder);
            if (toInsert <= 0) {
                return 0;
            }
        }
        //Snapshot before ANY mutation of the container in this transaction (restores on abort)
        updateSnapshots(transaction);
        long remainder = container.insert(toInsert, Action.EXECUTE, AutomationType.EXTERNAL);
        return converter.convertTo(toInsert - remainder);
    }

    @Override
    public long extract(long maxAmount, TransactionContext transaction) {
        StoragePreconditions.notNegative(maxAmount);
        long toExtract = converter.convertFrom(maxAmount);
        if (toExtract <= 0) {
            return 0;
        }
        if (!converter.isOneToOne()) {
            //Clamp what the container can yield to a whole number of external units so no
            // fraction of a unit is voided on the way out
            long simulatedExtracted = container.extract(toExtract, Action.SIMULATE, AutomationType.EXTERNAL);
            toExtract = convertToAndBack(simulatedExtracted);
            if (toExtract <= 0) {
                return 0;
            }
        }
        updateSnapshots(transaction);
        long extracted = container.extract(toExtract, Action.EXECUTE, AutomationType.EXTERNAL);
        return converter.convertTo(extracted);
    }

    /**
     * Long-widened mirror of {@code ForgeEnergyIntegration#convertToAndBack}: the largest number of
     * joules ≤ {@code joules} that maps to a whole number of external units.
     */
    private long convertToAndBack(long joules) {
        long units = converter.convertTo(joules);
        long result = converter.convertFrom(units);
        if (converter.getConversion() >= 1 && result % converter.getConversion() > 0) {
            return converter.convertFrom(units - 1);
        }
        return result;
    }

    @Override
    public long getAmount() {
        return converter.convertTo(container.getEnergy());
    }

    @Override
    public long getCapacity() {
        return converter.convertTo(container.getMaxEnergy());
    }

    @Override
    protected Long createSnapshot() {
        return container.getEnergy();
    }

    @Override
    protected void readSnapshot(Long snapshot) {
        container.setEnergy(snapshot);
    }
}

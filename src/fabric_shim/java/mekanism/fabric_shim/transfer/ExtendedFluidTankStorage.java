package mekanism.fabric_shim.transfer;

import java.util.Objects;
import mekanism.api.Action;
import mekanism.api.AutomationType;
import mekanism.api.fluid.IExtendedFluidTank;
import mekanism.fabric_shim.fluids.FluidStack;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.StoragePreconditions;
import net.fabricmc.fabric.api.transfer.v1.storage.base.SingleSlotStorage;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.fabricmc.fabric.api.transfer.v1.transaction.base.SnapshotParticipant;

/**
 * Exposes a Mekanism-owned {@link IExtendedFluidTank} to Fabric consumers as a
 * {@link SingleSlotStorage}{@code <FluidVariant>}.
 *
 * <p><b>Design rule (transfer-bridge.md):</b> the Mekanism→Fabric direction adapts ONLY
 * Mekanism-owned tanks (which have {@code setStackUnchecked}), because transactional rollback
 * requires restoring state exactly — a generic IFluidHandler cannot be rolled back. Simulation is
 * expressed by the caller aborting the transaction; this adapter always EXECUTEs against the tank
 * and relies on {@link SnapshotParticipant} snapshot/restore for aborts.
 *
 * <p>All boundary amounts are floored to whole mB (multiples of 81 droplets) — see TransferUnits.
 */
public class ExtendedFluidTankStorage extends SnapshotParticipant<FluidStack> implements SingleSlotStorage<FluidVariant> {

    private final IExtendedFluidTank tank;

    public ExtendedFluidTankStorage(IExtendedFluidTank tank) {
        this.tank = Objects.requireNonNull(tank);
    }

    @Override
    public long insert(FluidVariant resource, long maxAmount, TransactionContext transaction) {
        StoragePreconditions.notBlankNotNegative(resource, maxAmount);
        int mb = TransferUnits.dropletsToMbFloor(maxAmount);
        if (mb <= 0) {
            return 0;
        }
        FluidStack toInsert = new FluidStack(resource.getFluid().builtInRegistryHolder(), mb, resource.getComponents());
        //Snapshot before ANY mutation of the tank in this transaction (restores on abort)
        updateSnapshots(transaction);
        FluidStack remainder = tank.insert(toInsert, Action.EXECUTE, AutomationType.EXTERNAL);
        return TransferUnits.mbToDroplets(mb - remainder.getAmount());
    }

    @Override
    public long extract(FluidVariant resource, long maxAmount, TransactionContext transaction) {
        StoragePreconditions.notBlankNotNegative(resource, maxAmount);
        if (tank.isEmpty() || !variantMatchesTank(resource)) {
            return 0;
        }
        int mb = TransferUnits.dropletsToMbFloor(maxAmount);
        if (mb <= 0) {
            return 0;
        }
        updateSnapshots(transaction);
        FluidStack extracted = tank.extract(mb, Action.EXECUTE, AutomationType.EXTERNAL);
        return TransferUnits.mbToDroplets(extracted.getAmount());
    }

    private boolean variantMatchesTank(FluidVariant resource) {
        FluidStack stored = tank.getFluid();
        return stored.is(resource.getFluid()) && Objects.equals(stored.getComponentsPatch(), resource.getComponents());
    }

    @Override
    public boolean isResourceBlank() {
        return tank.isEmpty();
    }

    @Override
    public FluidVariant getResource() {
        FluidStack stored = tank.getFluid();
        if (stored.isEmpty()) {
            return FluidVariant.blank();
        }
        return FluidVariant.of(stored.getFluid(), stored.getComponentsPatch());
    }

    @Override
    public long getAmount() {
        return TransferUnits.mbToDroplets(tank.getFluidAmount());
    }

    @Override
    public long getCapacity() {
        return TransferUnits.mbToDroplets(tank.getCapacity());
    }

    @Override
    protected FluidStack createSnapshot() {
        return tank.getFluid().copy();
    }

    @Override
    protected void readSnapshot(FluidStack snapshot) {
        //Unchecked: restoring a snapshot must not be re-validated (the state was legal when taken)
        tank.setStackUnchecked(snapshot);
    }
}

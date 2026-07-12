package mekanism.fabric_shim.transfer;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import mekanism.api.fluid.IExtendedFluidHandler;
import mekanism.api.fluid.IExtendedFluidTank;
import mekanism.fabric_shim.fluids.FluidStack;
import mekanism.fabric_shim.fluids.capability.IFluidHandler.FluidAction;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.StoragePreconditions;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageView;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.fabricmc.fabric.api.transfer.v1.transaction.base.SnapshotParticipant;

/**
 * Exposes a Mekanism tile's fluids to Fabric consumers for one side, with full side-config
 * fidelity: every operation routes through the tile's own side-aware handler (the proxy it
 * registers for NeoForge-shaped capability queries), so per-side insert/extract permissions apply
 * exactly as on NeoForge. Rollback does NOT go through the handler — the side's backing
 * {@link IExtendedFluidTank}s are snapshotted and restored via their unchecked setters (the
 * handler only ever mutates those tanks, so the snapshot covers every mutation it can make).
 *
 * <p>This is the user-approved expose design (2026-07-11): "route through proxies" — operations
 * via the permission-enforcing handler, rollback via the containers. It is not the forbidden
 * generic-handler wrap: restoration never relies on inverse operations.
 */
public class ProxiedFluidStorage extends SnapshotParticipant<List<FluidStack>> implements Storage<FluidVariant> {

    private final IExtendedFluidHandler handler;
    private final List<IExtendedFluidTank> containers;

    public ProxiedFluidStorage(IExtendedFluidHandler handler, List<IExtendedFluidTank> containers) {
        this.handler = Objects.requireNonNull(handler);
        this.containers = List.copyOf(containers);
    }

    @Override
    public long insert(FluidVariant resource, long maxAmount, TransactionContext transaction) {
        StoragePreconditions.notBlankNotNegative(resource, maxAmount);
        int mb = TransferUnits.dropletsToMbFloor(maxAmount);
        if (mb <= 0) {
            return 0;
        }
        updateSnapshots(transaction);
        int filled = handler.fill(new FluidStack(resource.getFluid().builtInRegistryHolder(), mb, resource.getComponents()), FluidAction.EXECUTE);
        return TransferUnits.mbToDroplets(filled);
    }

    @Override
    public long extract(FluidVariant resource, long maxAmount, TransactionContext transaction) {
        StoragePreconditions.notBlankNotNegative(resource, maxAmount);
        int mb = TransferUnits.dropletsToMbFloor(maxAmount);
        if (mb <= 0) {
            return 0;
        }
        updateSnapshots(transaction);
        FluidStack drained = handler.drain(new FluidStack(resource.getFluid().builtInRegistryHolder(), mb, resource.getComponents()), FluidAction.EXECUTE);
        return TransferUnits.mbToDroplets(drained.getAmount());
    }

    @Override
    public Iterator<StorageView<FluidVariant>> iterator() {
        List<StorageView<FluidVariant>> views = new ArrayList<>(handler.getTanks());
        for (int tank = 0; tank < handler.getTanks(); tank++) {
            views.add(new TankView(tank));
        }
        return views.iterator();
    }

    @Override
    protected List<FluidStack> createSnapshot() {
        List<FluidStack> snapshot = new ArrayList<>(containers.size());
        for (IExtendedFluidTank container : containers) {
            snapshot.add(container.getFluid().copy());
        }
        return snapshot;
    }

    @Override
    protected void readSnapshot(List<FluidStack> snapshot) {
        for (int i = 0; i < containers.size(); i++) {
            //Unchecked: restoring a snapshot must not be re-validated (the state was legal when taken)
            containers.get(i).setStackUnchecked(snapshot.get(i));
        }
    }

    private class TankView implements StorageView<FluidVariant> {

        private final int tank;

        private TankView(int tank) {
            this.tank = tank;
        }

        @Override
        public long extract(FluidVariant resource, long maxAmount, TransactionContext transaction) {
            StoragePreconditions.notBlankNotNegative(resource, maxAmount);
            FluidStack stored = handler.getFluidInTank(tank);
            if (stored.isEmpty() || !stored.is(resource.getFluid()) || !Objects.equals(stored.getComponentsPatch(), resource.getComponents())) {
                return 0;
            }
            int mb = TransferUnits.dropletsToMbFloor(maxAmount);
            if (mb <= 0) {
                return 0;
            }
            updateSnapshots(transaction);
            FluidStack extracted = handler.extractFluid(tank, mb, mekanism.api.Action.EXECUTE);
            return TransferUnits.mbToDroplets(extracted.getAmount());
        }

        @Override
        public boolean isResourceBlank() {
            return handler.getFluidInTank(tank).isEmpty();
        }

        @Override
        public FluidVariant getResource() {
            FluidStack stored = handler.getFluidInTank(tank);
            return stored.isEmpty() ? FluidVariant.blank() : FluidVariant.of(stored.getFluid(), stored.getComponentsPatch());
        }

        @Override
        public long getAmount() {
            return TransferUnits.mbToDroplets(handler.getFluidInTank(tank).getAmount());
        }

        @Override
        public long getCapacity() {
            return TransferUnits.mbToDroplets(handler.getTankCapacity(tank));
        }
    }
}

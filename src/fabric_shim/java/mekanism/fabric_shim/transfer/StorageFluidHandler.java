package mekanism.fabric_shim.transfer;

import java.util.Objects;
import mekanism.fabric_shim.fluids.FluidStack;
import mekanism.fabric_shim.fluids.capability.IFluidHandler;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageView;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;

/**
 * Wraps an external Fabric {@code Storage<FluidVariant>} as a Mekanism-side {@link IFluidHandler},
 * for Mekanism machines/transmitters pushing into or pulling from neighboring Fabric storages.
 *
 * <p><b>Semantics (transfer-bridge.md):</b> simulate = open a transaction, perform the operation,
 * abort; execute = same but commit. Because an arbitrary external storage may accept/yield amounts
 * that are not whole mB, every operation runs an <i>alignment pass</i>: probe in a nested
 * transaction (aborted), floor the result to a multiple of 81 droplets, then perform the aligned
 * operation for real. If the storage still responds with a non-aligned amount, the transaction is
 * aborted and 0 is returned — the bridge never creates or destroys fluid.
 */
public class StorageFluidHandler implements IFluidHandler {

    private final Storage<FluidVariant> storage;

    public StorageFluidHandler(Storage<FluidVariant> storage) {
        this.storage = Objects.requireNonNull(storage);
    }

    @Override
    public int getTanks() {
        int count = 0;
        for (StorageView<FluidVariant> ignored : storage) {
            count++;
        }
        return count;
    }

    @Override
    public FluidStack getFluidInTank(int tank) {
        StorageView<FluidVariant> view = view(tank);
        if (view == null || view.isResourceBlank()) {
            return FluidStack.EMPTY;
        }
        return toStack(view.getResource(), TransferUnits.dropletsToMbFloor(view.getAmount()));
    }

    @Override
    public int getTankCapacity(int tank) {
        StorageView<FluidVariant> view = view(tank);
        return view == null ? 0 : TransferUnits.dropletsToMbFloor(view.getCapacity());
    }

    @Override
    public boolean isFluidValid(int tank, FluidStack stack) {
        //Fabric storages have no static filter query; optimistic like Fabric's own compat layers
        return true;
    }

    @Override
    public int fill(FluidStack resource, FluidAction action) {
        if (resource.isEmpty()) {
            return 0;
        }
        FluidVariant variant = toVariant(resource);
        long offer = TransferUnits.mbToDroplets(resource.getAmount());
        try (Transaction tx = Transaction.openOuter()) {
            //Alignment pass: probe what the storage would accept, then insert only the aligned amount
            long accepted;
            try (Transaction probe = tx.openNested()) {
                accepted = storage.insert(variant, offer, probe);
                probe.abort();
            }
            long aligned = TransferUnits.floorToMbAligned(accepted);
            if (aligned <= 0) {
                return 0;
            }
            long inserted = storage.insert(variant, aligned, tx);
            if (inserted != aligned) {
                //Storage responded inconsistently with its own probe — refuse rather than desync
                return 0;
            }
            if (action.execute()) {
                tx.commit();
            }
            return TransferUnits.dropletsToMbFloor(inserted);
        }
    }

    @Override
    public FluidStack drain(FluidStack resource, FluidAction action) {
        if (resource.isEmpty()) {
            return FluidStack.EMPTY;
        }
        return drain(toVariant(resource), resource.getAmount(), action);
    }

    @Override
    public FluidStack drain(int maxDrain, FluidAction action) {
        if (maxDrain <= 0) {
            return FluidStack.EMPTY;
        }
        //NeoForge drain(int) semantics: drain whatever fluid is first available
        FluidVariant target = null;
        for (StorageView<FluidVariant> view : storage) {
            if (!view.isResourceBlank()) {
                target = view.getResource();
                break;
            }
        }
        if (target == null) {
            return FluidStack.EMPTY;
        }
        return drain(target, maxDrain, action);
    }

    private FluidStack drain(FluidVariant variant, int maxDrainMb, FluidAction action) {
        long request = TransferUnits.mbToDroplets(maxDrainMb);
        try (Transaction tx = Transaction.openOuter()) {
            long available;
            try (Transaction probe = tx.openNested()) {
                available = storage.extract(variant, request, probe);
                probe.abort();
            }
            long aligned = TransferUnits.floorToMbAligned(available);
            if (aligned <= 0) {
                return FluidStack.EMPTY;
            }
            long extracted = storage.extract(variant, aligned, tx);
            if (extracted != aligned) {
                return FluidStack.EMPTY;
            }
            if (action.execute()) {
                tx.commit();
            }
            return toStack(variant, TransferUnits.dropletsToMbFloor(extracted));
        }
    }

    private StorageView<FluidVariant> view(int index) {
        int i = 0;
        for (StorageView<FluidVariant> view : storage) {
            if (i++ == index) {
                return view;
            }
        }
        return null;
    }

    private static FluidVariant toVariant(FluidStack stack) {
        return FluidVariant.of(stack.getFluid(), stack.getComponentsPatch());
    }

    private static FluidStack toStack(FluidVariant variant, int mb) {
        if (variant.isBlank() || mb <= 0) {
            return FluidStack.EMPTY;
        }
        return new FluidStack(variant.getFluid().builtInRegistryHolder(), mb, variant.getComponents());
    }
}

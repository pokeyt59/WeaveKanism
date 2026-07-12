package mekanism.fabric_shim.transfer;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import mekanism.api.inventory.IInventorySlot;
import mekanism.fabric_shim.items.IItemHandler;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.StoragePreconditions;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageView;
import net.fabricmc.fabric.api.transfer.v1.storage.base.SingleSlotStorage;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.fabricmc.fabric.api.transfer.v1.transaction.base.SnapshotParticipant;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;

/**
 * Exposes a Mekanism tile's inventory to Fabric consumers for one side, with full side-config
 * fidelity: every operation routes through the tile's own side-aware {@link IItemHandler} proxy,
 * so per-side insert/extract permissions apply exactly as on NeoForge. Rollback snapshots the
 * side's backing {@link IInventorySlot}s through their NBT round-trip — Mekanism slots
 * deserialize via their unchecked setter, so restore is never re-validated (the API interface has
 * no direct unchecked setter to call).
 *
 * <p>User-approved expose design (2026-07-11): operations via the permission-enforcing handler,
 * rollback via the containers.
 */
public class ProxiedItemStorage extends SnapshotParticipant<List<CompoundTag>> implements Storage<ItemVariant> {

    private final IItemHandler handler;
    private final List<IInventorySlot> containers;
    private final HolderLookup.Provider registries;

    public ProxiedItemStorage(IItemHandler handler, List<IInventorySlot> containers, HolderLookup.Provider registries) {
        this.handler = Objects.requireNonNull(handler);
        this.containers = List.copyOf(containers);
        this.registries = Objects.requireNonNull(registries);
    }

    @Override
    public long insert(ItemVariant resource, long maxAmount, TransactionContext transaction) {
        StoragePreconditions.notBlankNotNegative(resource, maxAmount);
        long remaining = maxAmount;
        for (int slot = 0; slot < handler.getSlots() && remaining > 0; slot++) {
            remaining -= insertIntoSlot(slot, resource, remaining, transaction);
        }
        return maxAmount - remaining;
    }

    @Override
    public long extract(ItemVariant resource, long maxAmount, TransactionContext transaction) {
        StoragePreconditions.notBlankNotNegative(resource, maxAmount);
        long extracted = 0;
        for (int slot = 0; slot < handler.getSlots() && extracted < maxAmount; slot++) {
            extracted += new SlotView(slot).extract(resource, maxAmount - extracted, transaction);
        }
        return extracted;
    }

    private long insertIntoSlot(int slot, ItemVariant resource, long maxAmount, TransactionContext transaction) {
        int toOffer = (int) Math.min(maxAmount, Integer.MAX_VALUE);
        if (toOffer <= 0) {
            return 0;
        }
        updateSnapshots(transaction);
        ItemStack remainder = handler.insertItem(slot, resource.toStack(toOffer), false);
        return toOffer - remainder.getCount();
    }

    @Override
    public Iterator<StorageView<ItemVariant>> iterator() {
        List<StorageView<ItemVariant>> views = new ArrayList<>(handler.getSlots());
        for (int slot = 0; slot < handler.getSlots(); slot++) {
            views.add(new SlotView(slot));
        }
        return views.iterator();
    }

    @Override
    protected List<CompoundTag> createSnapshot() {
        List<CompoundTag> snapshot = new ArrayList<>(containers.size());
        for (IInventorySlot container : containers) {
            snapshot.add(container.serializeNBT(registries));
        }
        return snapshot;
    }

    @Override
    protected void readSnapshot(List<CompoundTag> snapshot) {
        for (int i = 0; i < containers.size(); i++) {
            //Mekanism slots deserialize through their unchecked setter — restore is not re-validated
            containers.get(i).deserializeNBT(registries, snapshot.get(i));
        }
    }

    private class SlotView implements SingleSlotStorage<ItemVariant> {

        private final int slot;

        private SlotView(int slot) {
            this.slot = slot;
        }

        @Override
        public long insert(ItemVariant resource, long maxAmount, TransactionContext transaction) {
            StoragePreconditions.notBlankNotNegative(resource, maxAmount);
            return insertIntoSlot(slot, resource, maxAmount, transaction);
        }

        @Override
        public long extract(ItemVariant resource, long maxAmount, TransactionContext transaction) {
            StoragePreconditions.notBlankNotNegative(resource, maxAmount);
            ItemStack stored = handler.getStackInSlot(slot);
            if (stored.isEmpty() || !resource.matches(stored)) {
                return 0;
            }
            int toExtract = (int) Math.min(maxAmount, Integer.MAX_VALUE);
            if (toExtract <= 0) {
                return 0;
            }
            updateSnapshots(transaction);
            ItemStack extracted = handler.extractItem(slot, toExtract, false);
            return extracted.getCount();
        }

        @Override
        public boolean isResourceBlank() {
            return handler.getStackInSlot(slot).isEmpty();
        }

        @Override
        public ItemVariant getResource() {
            ItemStack stored = handler.getStackInSlot(slot);
            return stored.isEmpty() ? ItemVariant.blank() : ItemVariant.of(stored);
        }

        @Override
        public long getAmount() {
            return handler.getStackInSlot(slot).getCount();
        }

        @Override
        public long getCapacity() {
            return handler.getSlotLimit(slot);
        }
    }
}

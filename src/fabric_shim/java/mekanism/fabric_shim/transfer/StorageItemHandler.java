package mekanism.fabric_shim.transfer;

import java.util.Objects;
import mekanism.fabric_shim.items.IItemHandler;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageView;
import net.fabricmc.fabric.api.transfer.v1.storage.base.SingleSlotStorage;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.minecraft.world.item.ItemStack;

/**
 * Wraps an external Fabric {@code Storage<ItemVariant>} as a Mekanism-side {@link IItemHandler},
 * for Mekanism machines/transporters pushing into or pulling from neighboring Fabric inventories.
 *
 * <p><b>Semantics (transfer-bridge.md):</b> simulate = open a transaction, perform the operation,
 * abort; execute = same but commit. Item counts map 1:1, so no alignment pass is needed — the
 * insert/extract return values are the exact moved amounts, from which the exact NeoForge-shape
 * remainder is computed.
 *
 * <p>Per-slot inserts require the target view to be a {@link SingleSlotStorage} (true for Fabric's
 * own inventory wrappers); a view that cannot be targeted individually rejects the insert rather
 * than letting items land in a different slot than the caller addressed. Per-slot extraction works
 * on any storage via {@link StorageView#extract}.
 */
public class StorageItemHandler implements IItemHandler {

    private final Storage<ItemVariant> storage;

    public StorageItemHandler(Storage<ItemVariant> storage) {
        this.storage = Objects.requireNonNull(storage);
    }

    @Override
    public int getSlots() {
        int count = 0;
        for (StorageView<ItemVariant> ignored : storage) {
            count++;
        }
        return count;
    }

    @Override
    public ItemStack getStackInSlot(int slot) {
        StorageView<ItemVariant> view = view(slot);
        if (view == null || view.isResourceBlank()) {
            return ItemStack.EMPTY;
        }
        return view.getResource().toStack((int) Math.min(view.getAmount(), Integer.MAX_VALUE));
    }

    @Override
    public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
        if (stack.isEmpty()) {
            return ItemStack.EMPTY;
        }
        StorageView<ItemVariant> view = view(slot);
        if (!(view instanceof SingleSlotStorage<ItemVariant> slotStorage)) {
            //Cannot address this slot individually; refuse rather than re-route the items
            return stack;
        }
        ItemVariant variant = ItemVariant.of(stack);
        try (Transaction tx = Transaction.openOuter()) {
            long inserted = slotStorage.insert(variant, stack.getCount(), tx);
            if (inserted <= 0) {
                return stack;
            }
            if (!simulate) {
                tx.commit();
            }
            int remainder = stack.getCount() - (int) inserted;
            return remainder == 0 ? ItemStack.EMPTY : stack.copyWithCount(remainder);
        }
    }

    @Override
    public ItemStack extractItem(int slot, int amount, boolean simulate) {
        if (amount <= 0) {
            return ItemStack.EMPTY;
        }
        StorageView<ItemVariant> view = view(slot);
        if (view == null || view.isResourceBlank()) {
            return ItemStack.EMPTY;
        }
        ItemVariant variant = view.getResource();
        //NeoForge contract: the returned stack never exceeds its own max stack size
        int toExtract = Math.min(amount, variant.toStack().getMaxStackSize());
        try (Transaction tx = Transaction.openOuter()) {
            long extracted = view.extract(variant, toExtract, tx);
            if (extracted <= 0) {
                return ItemStack.EMPTY;
            }
            if (!simulate) {
                tx.commit();
            }
            return variant.toStack((int) extracted);
        }
    }

    @Override
    public int getSlotLimit(int slot) {
        StorageView<ItemVariant> view = view(slot);
        return view == null ? 0 : (int) Math.min(view.getCapacity(), Integer.MAX_VALUE);
    }

    @Override
    public boolean isItemValid(int slot, ItemStack stack) {
        //Fabric storages have no static filter query; optimistic like Fabric's own compat layers
        return true;
    }

    private StorageView<ItemVariant> view(int index) {
        int i = 0;
        for (StorageView<ItemVariant> view : storage) {
            if (i++ == index) {
                return view;
            }
        }
        return null;
    }
}

package mekanism.fabric_shim.transfer;

import java.util.Objects;
import java.util.function.Consumer;
import mekanism.api.Action;
import mekanism.api.AutomationType;
import mekanism.api.inventory.IInventorySlot;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.StoragePreconditions;
import net.fabricmc.fabric.api.transfer.v1.storage.base.SingleSlotStorage;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.fabricmc.fabric.api.transfer.v1.transaction.base.SnapshotParticipant;
import net.minecraft.world.item.ItemStack;

/**
 * Exposes a Mekanism-owned {@link IInventorySlot} to Fabric consumers as a
 * {@link SingleSlotStorage}{@code <ItemVariant>}.
 *
 * <p><b>Design rule (transfer-bridge.md):</b> the Mekanism→Fabric direction adapts ONLY containers
 * whose state can be restored exactly on abort. {@code IInventorySlot} itself only declares the
 * validating {@code setStack}, so the unchecked setter is taken as a parameter — wiring sites pass
 * {@code BasicInventorySlot::setStackUnchecked} (the same idiom Mekanism uses for its own
 * {@code InventoryContainerSlot}); a slot without an unchecked setter cannot be adapted. Item
 * counts map 1:1 (no unit conversion), so only the simulate/rollback invariants apply.
 */
public class InventorySlotStorage extends SnapshotParticipant<ItemStack> implements SingleSlotStorage<ItemVariant> {

    private final IInventorySlot slot;
    private final Consumer<ItemStack> uncheckedSetter;

    public InventorySlotStorage(IInventorySlot slot, Consumer<ItemStack> uncheckedSetter) {
        this.slot = Objects.requireNonNull(slot);
        this.uncheckedSetter = Objects.requireNonNull(uncheckedSetter);
    }

    @Override
    public long insert(ItemVariant resource, long maxAmount, TransactionContext transaction) {
        StoragePreconditions.notBlankNotNegative(resource, maxAmount);
        int toOffer = (int) Math.min(maxAmount, Integer.MAX_VALUE);
        if (toOffer <= 0) {
            return 0;
        }
        //Snapshot before ANY mutation of the slot in this transaction (restores on abort)
        updateSnapshots(transaction);
        ItemStack remainder = slot.insertItem(resource.toStack(toOffer), Action.EXECUTE, AutomationType.EXTERNAL);
        return toOffer - remainder.getCount();
    }

    @Override
    public long extract(ItemVariant resource, long maxAmount, TransactionContext transaction) {
        StoragePreconditions.notBlankNotNegative(resource, maxAmount);
        ItemStack current = slot.getStack();
        if (current.isEmpty() || !resource.matches(current)) {
            return 0;
        }
        int toExtract = (int) Math.min(maxAmount, Integer.MAX_VALUE);
        if (toExtract <= 0) {
            return 0;
        }
        updateSnapshots(transaction);
        ItemStack extracted = slot.extractItem(toExtract, Action.EXECUTE, AutomationType.EXTERNAL);
        return extracted.getCount();
    }

    @Override
    public boolean isResourceBlank() {
        return slot.isEmpty();
    }

    @Override
    public ItemVariant getResource() {
        ItemStack current = slot.getStack();
        return current.isEmpty() ? ItemVariant.blank() : ItemVariant.of(current);
    }

    @Override
    public long getAmount() {
        return slot.getCount();
    }

    @Override
    public long getCapacity() {
        return slot.getLimit(slot.getStack());
    }

    @Override
    protected ItemStack createSnapshot() {
        return slot.getStack().copy();
    }

    @Override
    protected void readSnapshot(ItemStack snapshot) {
        //Unchecked: restoring a snapshot must not be re-validated (the state was legal when taken)
        uncheckedSetter.accept(snapshot);
    }
}

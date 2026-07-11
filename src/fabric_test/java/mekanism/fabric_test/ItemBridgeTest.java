package mekanism.fabric_test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Iterator;
import java.util.List;
import mekanism.fabric_shim.transfer.InventorySlotStorage;
import mekanism.fabric_shim.transfer.StorageItemHandler;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageView;
import net.fabricmc.fabric.api.transfer.v1.storage.base.SingleVariantStorage;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * TRANSFER-BRIDGE SEMANTICS TESTS — item leg (see fabric-port/design/transfer-bridge.md and
 * FluidBridgeTest). Item counts map 1:1, so these encode simulate purity, rollback exactness, and
 * exact-remainder accounting (never create or destroy items).
 */
class ItemBridgeTest {

    @BeforeAll
    static void bootstrap() {
        McBootstrap.ensure();
    }

    private static SingleVariantStorage<ItemVariant> itemStorage(long capacity) {
        return new SingleVariantStorage<>() {
            @Override
            protected ItemVariant getBlankVariant() {
                return ItemVariant.blank();
            }

            @Override
            protected long getCapacity(ItemVariant variant) {
                return capacity;
            }
        };
    }

    // --- Mekanism slot exposed to Fabric (InventorySlotStorage) ---

    private static InventorySlotStorage storageOf(TestInventorySlot slot) {
        return new InventorySlotStorage(slot, slot::setStackUnchecked);
    }

    @Test
    @DisplayName("insert returns the exact accepted count against the slot limit")
    void insertPartialAgainstLimit() {
        TestInventorySlot slot = new TestInventorySlot(64);
        slot.setStack(new ItemStack(Items.STONE, 60));
        InventorySlotStorage storage = storageOf(slot);
        try (Transaction tx = Transaction.openOuter()) {
            long inserted = storage.insert(ItemVariant.of(Items.STONE), 10, tx);
            assertEquals(4, inserted);
            tx.commit();
        }
        assertEquals(64, slot.getCount());
    }

    @Test
    @DisplayName("aborted transaction restores the slot exactly (simulate purity)")
    void abortRestoresSlot() {
        TestInventorySlot slot = new TestInventorySlot(64);
        InventorySlotStorage storage = storageOf(slot);
        try (Transaction tx = Transaction.openOuter()) {
            long inserted = storage.insert(ItemVariant.of(Items.STONE), 32, tx);
            assertEquals(32, inserted);
            assertEquals(32, slot.getCount(), "mutation is visible inside the transaction");
            tx.abort();
        }
        assertTrue(slot.isEmpty(), "abort must restore pre-transaction state exactly");
    }

    @Test
    @DisplayName("inner commit still rolls back when the outer transaction aborts")
    void nestedInnerCommitOuterAbort() {
        TestInventorySlot slot = new TestInventorySlot(64);
        InventorySlotStorage storage = storageOf(slot);
        try (Transaction outer = Transaction.openOuter()) {
            try (Transaction inner = outer.openNested()) {
                storage.insert(ItemVariant.of(Items.STONE), 16, inner);
                inner.commit();
            }
            assertEquals(16, slot.getCount());
            outer.abort();
        }
        assertTrue(slot.isEmpty(), "nested commit is provisional until the outer commit");
    }

    @Test
    @DisplayName("extract returns 0 on item mismatch and never mutates")
    void extractMismatchReturnsZero() {
        TestInventorySlot slot = new TestInventorySlot(64);
        slot.setStack(new ItemStack(Items.STONE, 20));
        InventorySlotStorage storage = storageOf(slot);
        try (Transaction tx = Transaction.openOuter()) {
            assertEquals(0, storage.extract(ItemVariant.of(Items.DIRT), 10, tx));
            tx.commit();
        }
        assertEquals(20, slot.getCount());
    }

    @Test
    @DisplayName("view resource/amount/capacity reflect the slot")
    void viewAmounts() {
        TestInventorySlot slot = new TestInventorySlot(64);
        slot.setStack(new ItemStack(Items.STONE, 60));
        InventorySlotStorage storage = storageOf(slot);
        assertTrue(storage.getResource().matches(new ItemStack(Items.STONE)));
        assertEquals(60, storage.getAmount());
        assertEquals(64, storage.getCapacity());
    }

    // --- External Fabric storage consumed by Mekanism (StorageItemHandler) ---

    @Test
    @DisplayName("insertItem: SIMULATE does not mutate, EXECUTE commits, remainder exact")
    void handlerSimulatePurity() {
        SingleVariantStorage<ItemVariant> target = itemStorage(64);
        StorageItemHandler handler = new StorageItemHandler(target);

        ItemStack remainder = handler.insertItem(0, new ItemStack(Items.STONE, 32), true);
        assertTrue(remainder.isEmpty());
        assertEquals(0, target.getAmount(), "simulate must not mutate the target");

        remainder = handler.insertItem(0, new ItemStack(Items.STONE, 32), false);
        assertTrue(remainder.isEmpty());
        assertEquals(32, target.getAmount());
    }

    @Test
    @DisplayName("insertItem returns the exact remainder when the target fills")
    void handlerInsertPartialRemainder() {
        SingleVariantStorage<ItemVariant> target = itemStorage(10);
        StorageItemHandler handler = new StorageItemHandler(target);

        ItemStack remainder = handler.insertItem(0, new ItemStack(Items.STONE, 64), false);
        assertEquals(54, remainder.getCount());
        assertEquals(10, target.getAmount(), "items in + remainder must equal items offered");
    }

    @Test
    @DisplayName("extractItem is exact, caps at max stack size, and simulate does not mutate")
    void handlerExtractCapsAtMaxStackSize() {
        SingleVariantStorage<ItemVariant> source = itemStorage(200);
        source.variant = ItemVariant.of(Items.STONE);
        source.amount = 100;
        StorageItemHandler handler = new StorageItemHandler(source);

        ItemStack simulated = handler.extractItem(0, 100, true);
        assertEquals(64, simulated.getCount(), "returned stack never exceeds its max stack size");
        assertEquals(100, source.getAmount(), "simulate must not mutate the source");

        ItemStack extracted = handler.extractItem(0, 100, false);
        assertEquals(64, extracted.getCount());
        assertEquals(36, source.getAmount());
    }

    @Test
    @DisplayName("insertItem refuses a slot that cannot be targeted individually")
    void handlerRejectsUntargetableSlot() {
        SingleVariantStorage<ItemVariant> backing = itemStorage(64);
        //A storage whose view is deliberately NOT a SingleSlotStorage: per-slot inserts cannot be
        // addressed honestly, so the handler must reject rather than re-route
        Storage<ItemVariant> opaque = new Storage<>() {
            @Override
            public long insert(ItemVariant resource, long maxAmount, TransactionContext transaction) {
                return backing.insert(resource, maxAmount, transaction);
            }

            @Override
            public long extract(ItemVariant resource, long maxAmount, TransactionContext transaction) {
                return backing.extract(resource, maxAmount, transaction);
            }

            @Override
            public Iterator<StorageView<ItemVariant>> iterator() {
                StorageView<ItemVariant> hidden = new StorageView<>() {
                    @Override
                    public long extract(ItemVariant resource, long maxAmount, TransactionContext transaction) {
                        return backing.extract(resource, maxAmount, transaction);
                    }

                    @Override
                    public boolean isResourceBlank() {
                        return backing.isResourceBlank();
                    }

                    @Override
                    public ItemVariant getResource() {
                        return backing.getResource();
                    }

                    @Override
                    public long getAmount() {
                        return backing.getAmount();
                    }

                    @Override
                    public long getCapacity() {
                        return backing.getCapacity();
                    }
                };
                return List.of(hidden).iterator();
            }
        };
        StorageItemHandler handler = new StorageItemHandler(opaque);

        ItemStack offered = new ItemStack(Items.STONE, 8);
        ItemStack remainder = handler.insertItem(0, offered, false);
        assertEquals(8, remainder.getCount(), "untargetable slot must reject the full stack");
        assertEquals(0, backing.getAmount(), "nothing may move through an untargetable slot");
        //Extraction still works per-view on any storage
        source(backing);
        ItemStack extracted = handler.extractItem(0, 4, false);
        assertEquals(4, extracted.getCount());
        assertEquals(12, backing.getAmount());
    }

    private static void source(SingleVariantStorage<ItemVariant> storage) {
        storage.variant = ItemVariant.of(Items.STONE);
        storage.amount = 16;
    }
}

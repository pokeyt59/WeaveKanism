package mekanism.fabric_test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import mekanism.api.Action;
import mekanism.api.AutomationType;
import mekanism.api.fluid.IExtendedFluidHandler;
import mekanism.fabric_shim.energy.IEnergyStorage;
import mekanism.fabric_shim.fluids.FluidStack;
import mekanism.fabric_shim.items.IItemHandler;
import mekanism.fabric_shim.transfer.ProxiedEnergyStorage;
import mekanism.fabric_shim.transfer.ProxiedFluidStorage;
import mekanism.fabric_shim.transfer.ProxiedItemStorage;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.minecraft.core.RegistryAccess;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.material.Fluids;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * TRANSFER-BRIDGE SEMANTICS TESTS — expose direction (user-approved "route through proxies"
 * design, 2026-07-11). The Proxied*Storage adapters must (a) let the tile's own side-aware proxy
 * enforce per-side insert/extract permissions — the side-config fidelity NeoForge users get — and
 * (b) roll back via the backing containers, never via inverse operations.
 */
class ProxiedStorageTest {

    private static RegistryAccess registries;

    @BeforeAll
    static void bootstrap() {
        McBootstrap.ensure();
        registries = RegistryAccess.fromRegistryOfRegistries(net.minecraft.core.registries.BuiltInRegistries.REGISTRY);
    }

    /** Side-aware proxy double: per-tank abstract subset, interface defaults do the rest. */
    private record SidedFluidProxy(List<TestFluidTank> tanks, boolean allowInsert, boolean allowExtract) implements IExtendedFluidHandler {

        @Override
        public int getTanks() {
            return tanks.size();
        }

        @Override
        public FluidStack getFluidInTank(int tank) {
            return tanks.get(tank).getFluid();
        }

        @Override
        public void setFluidInTank(int tank, FluidStack stack) {
            tanks.get(tank).setStack(stack);
        }

        @Override
        public int getTankCapacity(int tank) {
            return tanks.get(tank).getCapacity();
        }

        @Override
        public boolean isFluidValid(int tank, FluidStack stack) {
            return true;
        }

        @Override
        public FluidStack insertFluid(int tank, FluidStack stack, Action action) {
            return allowInsert ? tanks.get(tank).insert(stack, action, AutomationType.EXTERNAL) : stack;
        }

        @Override
        public FluidStack extractFluid(int tank, int amount, Action action) {
            return allowExtract ? tanks.get(tank).extract(amount, action, AutomationType.EXTERNAL) : FluidStack.EMPTY;
        }
    }

    @Test
    @DisplayName("fluid insert routes through the proxy and lands in the container")
    void fluidInsertThroughProxy() {
        TestFluidTank tank = new TestFluidTank(10_000);
        ProxiedFluidStorage storage = new ProxiedFluidStorage(new SidedFluidProxy(List.of(tank), true, true), List.of(tank));
        try (Transaction tx = Transaction.openOuter()) {
            assertEquals(8_100, storage.insert(FluidVariant.of(Fluids.WATER), 8_100, tx));
            tx.commit();
        }
        assertEquals(100, tank.getFluidAmount());
    }

    @Test
    @DisplayName("side config fidelity: an input-blocked side accepts nothing")
    void fluidInsertBlockedBySide() {
        TestFluidTank tank = new TestFluidTank(10_000);
        ProxiedFluidStorage storage = new ProxiedFluidStorage(new SidedFluidProxy(List.of(tank), false, true), List.of(tank));
        try (Transaction tx = Transaction.openOuter()) {
            assertEquals(0, storage.insert(FluidVariant.of(Fluids.WATER), 8_100, tx));
            tx.commit();
        }
        assertTrue(tank.isEmpty(), "the proxy's side rules must be enforced");
    }

    @Test
    @DisplayName("side config fidelity: an output-blocked side yields nothing")
    void fluidExtractBlockedBySide() {
        TestFluidTank tank = new TestFluidTank(10_000);
        tank.setStack(new FluidStack(Fluids.WATER, 500));
        ProxiedFluidStorage storage = new ProxiedFluidStorage(new SidedFluidProxy(List.of(tank), true, false), List.of(tank));
        try (Transaction tx = Transaction.openOuter()) {
            assertEquals(0, storage.extract(FluidVariant.of(Fluids.WATER), 8_100, tx));
            tx.commit();
        }
        assertEquals(500, tank.getFluidAmount());
    }

    @Test
    @DisplayName("abort restores the container even though operations went through the proxy")
    void fluidAbortRestoresViaContainer() {
        TestFluidTank tank = new TestFluidTank(10_000);
        ProxiedFluidStorage storage = new ProxiedFluidStorage(new SidedFluidProxy(List.of(tank), true, true), List.of(tank));
        try (Transaction tx = Transaction.openOuter()) {
            storage.insert(FluidVariant.of(Fluids.WATER), 8_100, tx);
            assertEquals(100, tank.getFluidAmount(), "mutation is visible inside the transaction");
            tx.abort();
        }
        assertTrue(tank.isEmpty(), "rollback goes via the container's unchecked setter");
    }

    @Test
    @DisplayName("fluid views convert x81 and per-view extraction honors the proxy")
    void fluidViews() {
        TestFluidTank tank = new TestFluidTank(10_000);
        tank.setStack(new FluidStack(Fluids.WATER, 500));
        ProxiedFluidStorage storage = new ProxiedFluidStorage(new SidedFluidProxy(List.of(tank), true, true), List.of(tank));
        var view = storage.iterator().next();
        assertEquals(40_500, view.getAmount());
        assertEquals(810_000, view.getCapacity());
        try (Transaction tx = Transaction.openOuter()) {
            assertEquals(8_100, view.extract(FluidVariant.of(Fluids.WATER), 8_130, tx), "floors to whole mB");
            tx.commit();
        }
        assertEquals(400, tank.getFluidAmount());
    }

    /** Side-aware item proxy double over test slots. */
    private record SidedItemProxy(List<TestInventorySlot> slots, boolean allowInsert, boolean allowExtract) implements IItemHandler {

        @Override
        public int getSlots() {
            return slots.size();
        }

        @Override
        public ItemStack getStackInSlot(int slot) {
            return slots.get(slot).getStack();
        }

        @Override
        public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
            return allowInsert ? slots.get(slot).insertItem(stack, Action.get(!simulate), AutomationType.EXTERNAL) : stack;
        }

        @Override
        public ItemStack extractItem(int slot, int amount, boolean simulate) {
            return allowExtract ? slots.get(slot).extractItem(amount, Action.get(!simulate), AutomationType.EXTERNAL) : ItemStack.EMPTY;
        }

        @Override
        public int getSlotLimit(int slot) {
            return slots.get(slot).getLimit(ItemStack.EMPTY);
        }

        @Override
        public boolean isItemValid(int slot, ItemStack stack) {
            return true;
        }
    }

    @Test
    @DisplayName("item insert honors the proxy's side rules")
    void itemInsertBlockedBySide() {
        TestInventorySlot slot = new TestInventorySlot(64);
        ProxiedItemStorage storage = new ProxiedItemStorage(new SidedItemProxy(List.of(slot), false, true), List.of(slot), registries);
        try (Transaction tx = Transaction.openOuter()) {
            assertEquals(0, storage.insert(ItemVariant.of(Items.STONE), 32, tx));
            tx.commit();
        }
        assertTrue(slot.isEmpty());
    }

    @Test
    @DisplayName("item abort restores the slot via its NBT round-trip (unchecked restore)")
    void itemAbortRestoresViaNbt() {
        TestInventorySlot slot = new TestInventorySlot(64);
        ProxiedItemStorage storage = new ProxiedItemStorage(new SidedItemProxy(List.of(slot), true, true), List.of(slot), registries);
        try (Transaction tx = Transaction.openOuter()) {
            assertEquals(32, storage.insert(ItemVariant.of(Items.STONE), 32, tx));
            assertEquals(32, slot.getCount(), "mutation is visible inside the transaction");
            tx.abort();
        }
        assertTrue(slot.isEmpty(), "rollback restores the serialized snapshot");
    }

    @Test
    @DisplayName("item extract through the proxy is exact")
    void itemExtractThroughProxy() {
        TestInventorySlot slot = new TestInventorySlot(64);
        slot.setStack(new ItemStack(Items.STONE, 20));
        ProxiedItemStorage storage = new ProxiedItemStorage(new SidedItemProxy(List.of(slot), true, true), List.of(slot), registries);
        try (Transaction tx = Transaction.openOuter()) {
            assertEquals(8, storage.extract(ItemVariant.of(Items.STONE), 8, tx));
            tx.commit();
        }
        assertEquals(12, slot.getCount());
    }

    /** FE-shaped energy proxy double over a test container (1:1 for simplicity). */
    private record SidedEnergyProxy(TestEnergyContainer container, boolean allowInsert, boolean allowExtract) implements IEnergyStorage {

        @Override
        public int receiveEnergy(int toReceive, boolean simulate) {
            if (!allowInsert) {
                return 0;
            }
            long remainder = container.insert(toReceive, Action.get(!simulate), AutomationType.EXTERNAL);
            return toReceive - (int) remainder;
        }

        @Override
        public int extractEnergy(int toExtract, boolean simulate) {
            return allowExtract ? (int) container.extract(toExtract, Action.get(!simulate), AutomationType.EXTERNAL) : 0;
        }

        @Override
        public int getEnergyStored() {
            return (int) Math.min(container.getEnergy(), Integer.MAX_VALUE);
        }

        @Override
        public int getMaxEnergyStored() {
            return (int) Math.min(container.getMaxEnergy(), Integer.MAX_VALUE);
        }

        @Override
        public boolean canExtract() {
            return allowExtract;
        }

        @Override
        public boolean canReceive() {
            return allowInsert;
        }
    }

    @Test
    @DisplayName("energy insert routes through the proxy; abort restores the container")
    void energyInsertAndAbort() {
        TestEnergyContainer container = new TestEnergyContainer(1000);
        ProxiedEnergyStorage storage = new ProxiedEnergyStorage(new SidedEnergyProxy(container, true, true), List.of(container));
        try (Transaction tx = Transaction.openOuter()) {
            assertEquals(100, storage.insert(100, tx));
            assertEquals(100, container.getEnergy());
            tx.abort();
        }
        assertEquals(0, container.getEnergy(), "rollback restores via setEnergy");

        try (Transaction tx = Transaction.openOuter()) {
            assertEquals(100, storage.insert(100, tx));
            tx.commit();
        }
        assertEquals(100, container.getEnergy());
    }

    @Test
    @DisplayName("energy side rules pass through: blocked insertion reports and accepts nothing")
    void energyBlockedBySide() {
        TestEnergyContainer container = new TestEnergyContainer(1000);
        ProxiedEnergyStorage storage = new ProxiedEnergyStorage(new SidedEnergyProxy(container, false, true), List.of(container));
        assertFalse(storage.supportsInsertion());
        try (Transaction tx = Transaction.openOuter()) {
            assertEquals(0, storage.insert(100, tx));
            tx.commit();
        }
        assertEquals(0, container.getEnergy());
    }
}

package mekanism.fabric_test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import mekanism.fabric_shim.fluids.FluidStack;
import mekanism.fabric_shim.fluids.capability.IFluidHandler.FluidAction;
import mekanism.fabric_shim.transfer.ExtendedFluidTankStorage;
import mekanism.fabric_shim.transfer.StorageFluidHandler;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.base.SingleVariantStorage;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.minecraft.world.level.material.Fluids;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * TRANSFER-BRIDGE SEMANTICS TESTS (see fabric-port/design/transfer-bridge.md). These encode the
 * Phase 2 invariants: simulate purity, transactional rollback exactness, and the
 * never-create-never-destroy fluid rule under mB↔droplet conversion. All Phase 2 adapter work must
 * keep this suite green; extend it (don't weaken it) for item/energy adapters.
 */
class FluidBridgeTest {

    @BeforeAll
    static void bootstrap() {
        McBootstrap.ensure();
    }

    private static SingleVariantStorage<FluidVariant> fabricTank(long capacityDroplets) {
        return new SingleVariantStorage<>() {
            @Override
            protected FluidVariant getBlankVariant() {
                return FluidVariant.blank();
            }

            @Override
            protected long getCapacity(FluidVariant variant) {
                return capacityDroplets;
            }
        };
    }

    // --- Mekanism tank exposed to Fabric (ExtendedFluidTankStorage) ---

    @Test
    @DisplayName("insert floors to whole mB: offering 100 droplets stores exactly 1 mB (81)")
    void insertFloorsToWholeMb() {
        TestFluidTank tank = new TestFluidTank(10_000);
        ExtendedFluidTankStorage storage = new ExtendedFluidTankStorage(tank);
        try (Transaction tx = Transaction.openOuter()) {
            long inserted = storage.insert(FluidVariant.of(Fluids.WATER), 100, tx);
            assertEquals(81, inserted);
            tx.commit();
        }
        assertEquals(1, tank.getFluidAmount());
    }

    @Test
    @DisplayName("aborted transaction restores the tank exactly (simulate purity)")
    void abortRestoresTank() {
        TestFluidTank tank = new TestFluidTank(10_000);
        ExtendedFluidTankStorage storage = new ExtendedFluidTankStorage(tank);
        try (Transaction tx = Transaction.openOuter()) {
            long inserted = storage.insert(FluidVariant.of(Fluids.WATER), 8_100, tx);
            assertEquals(8_100, inserted);
            assertEquals(100, tank.getFluidAmount(), "mutation is visible inside the transaction");
            tx.abort();
        }
        assertTrue(tank.isEmpty(), "abort must restore pre-transaction state exactly");
    }

    @Test
    @DisplayName("inner commit still rolls back when the outer transaction aborts")
    void nestedInnerCommitOuterAbort() {
        TestFluidTank tank = new TestFluidTank(10_000);
        ExtendedFluidTankStorage storage = new ExtendedFluidTankStorage(tank);
        try (Transaction outer = Transaction.openOuter()) {
            try (Transaction inner = outer.openNested()) {
                storage.insert(FluidVariant.of(Fluids.WATER), 81_000, inner);
                inner.commit();
            }
            assertEquals(1000, tank.getFluidAmount());
            outer.abort();
        }
        assertTrue(tank.isEmpty(), "nested commit is provisional until the outer commit");
    }

    @Test
    @DisplayName("extract returns 0 on fluid mismatch and never mutates")
    void extractMismatchReturnsZero() {
        TestFluidTank tank = new TestFluidTank(10_000);
        tank.setStack(new FluidStack(Fluids.WATER, 500));
        ExtendedFluidTankStorage storage = new ExtendedFluidTankStorage(tank);
        try (Transaction tx = Transaction.openOuter()) {
            assertEquals(0, storage.extract(FluidVariant.of(Fluids.LAVA), 81_000, tx));
            tx.commit();
        }
        assertEquals(500, tank.getFluidAmount());
    }

    @Test
    @DisplayName("view amounts/capacity are exact ×81 conversions")
    void viewAmountsExact() {
        TestFluidTank tank = new TestFluidTank(10_000);
        tank.setStack(new FluidStack(Fluids.WATER, 500));
        ExtendedFluidTankStorage storage = new ExtendedFluidTankStorage(tank);
        assertEquals(40_500, storage.getAmount());
        assertEquals(810_000, storage.getCapacity());
    }

    // --- External Fabric storage consumed by Mekanism (StorageFluidHandler) ---

    @Test
    @DisplayName("fill: SIMULATE does not mutate, EXECUTE commits")
    void handlerSimulatePurity() {
        SingleVariantStorage<FluidVariant> target = fabricTank(81_000);
        StorageFluidHandler handler = new StorageFluidHandler(target);

        int simulated = handler.fill(new FluidStack(Fluids.WATER, 1000), FluidAction.SIMULATE);
        assertEquals(1000, simulated);
        assertEquals(0, target.getAmount(), "simulate must not mutate the target");

        int executed = handler.fill(new FluidStack(Fluids.WATER, 1000), FluidAction.EXECUTE);
        assertEquals(1000, executed);
        assertEquals(81_000, target.getAmount());
    }

    @Test
    @DisplayName("fill never creates fluid: a 100-droplet target accepts exactly 1 mB (81 droplets)")
    void handlerAlignmentOnQuirkyCapacity() {
        SingleVariantStorage<FluidVariant> target = fabricTank(100);
        StorageFluidHandler handler = new StorageFluidHandler(target);

        int filled = handler.fill(new FluidStack(Fluids.WATER, 10), FluidAction.EXECUTE);
        assertEquals(1, filled);
        assertEquals(81, target.getAmount(), "only mB-aligned droplet amounts may move");
    }

    @Test
    @DisplayName("drain is exact and mB-aligned")
    void handlerDrain() {
        SingleVariantStorage<FluidVariant> source = fabricTank(81_000);
        source.variant = FluidVariant.of(Fluids.WATER);
        source.amount = 40_500; // 500 mB
        StorageFluidHandler handler = new StorageFluidHandler(source);

        FluidStack drained = handler.drain(200, FluidAction.EXECUTE);
        assertTrue(drained.is(Fluids.WATER));
        assertEquals(200, drained.getAmount());
        assertEquals(24_300, source.getAmount());
    }

    @Test
    @DisplayName("drain never destroys fluid: 100 available droplets yield exactly 1 mB, 19 remain")
    void handlerDrainQuirkyAmount() {
        SingleVariantStorage<FluidVariant> source = fabricTank(81_000);
        source.variant = FluidVariant.of(Fluids.WATER);
        source.amount = 100;
        StorageFluidHandler handler = new StorageFluidHandler(source);

        FluidStack drained = handler.drain(10, FluidAction.EXECUTE);
        assertEquals(1, drained.getAmount());
        assertEquals(19, source.getAmount(), "unaligned remainder stays in the source");
    }

    @Test
    @DisplayName("sub-mB requests move nothing")
    void handlerSubMbMovesNothing() {
        SingleVariantStorage<FluidVariant> source = fabricTank(81_000);
        source.variant = FluidVariant.of(Fluids.WATER);
        source.amount = 80; // less than 1 mB
        StorageFluidHandler handler = new StorageFluidHandler(source);

        assertTrue(handler.drain(10, FluidAction.EXECUTE).isEmpty());
        assertEquals(80, source.getAmount());
    }
}

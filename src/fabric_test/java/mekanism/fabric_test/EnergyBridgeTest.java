package mekanism.fabric_test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import mekanism.api.energy.IEnergyConversion;
import mekanism.fabric_shim.transfer.EnergyContainerStorage;
import mekanism.fabric_shim.transfer.StorageEnergyHandler;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import team.reborn.energy.api.base.SimpleEnergyStorage;

/**
 * TRANSFER-BRIDGE SEMANTICS TESTS — energy leg (see fabric-port/design/transfer-bridge.md and
 * FluidBridgeTest). Encodes simulate purity, rollback exactness, and the never-create-never-destroy
 * rule under J↔unit conversion: only amounts mapping to a whole number of external units may move,
 * flooring always against the requester (mirrors Mekanism's own ForgeEnergyIntegration clamping).
 *
 * <p>Rates are FIXED test conversions (2.5 J/unit — the shape of Mekanism's default FE rate — and
 * 1:1); production wiring passes Mekanism's config-backed {@code EnergyUnit.FORGE_ENERGY}.
 */
class EnergyBridgeTest {

    /** J per external unit, fixed for tests; production uses the config-backed EnergyUnit. */
    private record FixedConversion(double getConversion) implements IEnergyConversion {

        @Override
        public boolean isEnabled() {
            return true;
        }
    }

    private static final IEnergyConversion RATE_2_5 = new FixedConversion(2.5);
    private static final IEnergyConversion RATE_1 = new FixedConversion(1);

    @BeforeAll
    static void bootstrap() {
        McBootstrap.ensure();
    }

    // --- Mekanism container exposed to Fabric (EnergyContainerStorage) ---

    @Test
    @DisplayName("insert moves only whole external units: 5 units at 2.5 J/unit stores exactly 10 J for 4 units")
    void insertClampsToWholeUnits() {
        TestEnergyContainer container = new TestEnergyContainer(1000);
        EnergyContainerStorage storage = new EnergyContainerStorage(container, RATE_2_5);
        try (Transaction tx = Transaction.openOuter()) {
            long accepted = storage.insert(5, tx);
            assertEquals(4, accepted, "5 units floor to 12 J, which clamps to 10 J = exactly 4 units");
            tx.commit();
        }
        assertEquals(10, container.getEnergy(), "the caller was charged 4 units = exactly 10 J");
    }

    @Test
    @DisplayName("sub-unit insert moves nothing (no free energy)")
    void subUnitInsertMovesNothing() {
        TestEnergyContainer container = new TestEnergyContainer(1000);
        EnergyContainerStorage storage = new EnergyContainerStorage(container, RATE_2_5);
        try (Transaction tx = Transaction.openOuter()) {
            assertEquals(0, storage.insert(1, tx), "1 unit = 2.5 J floors below a whole unit round-trip");
            tx.commit();
        }
        assertEquals(0, container.getEnergy());
    }

    @Test
    @DisplayName("aborted transaction restores the container exactly (simulate purity)")
    void abortRestoresContainer() {
        TestEnergyContainer container = new TestEnergyContainer(1000);
        EnergyContainerStorage storage = new EnergyContainerStorage(container, RATE_2_5);
        try (Transaction tx = Transaction.openOuter()) {
            long accepted = storage.insert(5, tx);
            assertEquals(4, accepted);
            assertEquals(10, container.getEnergy(), "mutation is visible inside the transaction");
            tx.abort();
        }
        assertEquals(0, container.getEnergy(), "abort must restore pre-transaction state exactly");
    }

    @Test
    @DisplayName("extract clamps quirky joule amounts: 11 J stored yields exactly 4 units (10 J), 1 J remains")
    void extractClampsQuirkyJoules() {
        TestEnergyContainer container = new TestEnergyContainer(1000);
        container.setEnergy(11);
        EnergyContainerStorage storage = new EnergyContainerStorage(container, RATE_2_5);
        try (Transaction tx = Transaction.openOuter()) {
            long extracted = storage.extract(100, tx);
            assertEquals(4, extracted, "11 J may only yield the whole-unit-mapped 10 J");
            tx.commit();
        }
        assertEquals(1, container.getEnergy(), "the unaligned remainder stays in the container");
    }

    @Test
    @DisplayName("view amount/capacity convert exactly")
    void viewAmountsConvert() {
        TestEnergyContainer container = new TestEnergyContainer(25);
        container.setEnergy(5);
        EnergyContainerStorage storage = new EnergyContainerStorage(container, RATE_2_5);
        assertEquals(2, storage.getAmount());
        assertEquals(10, storage.getCapacity());
    }

    @Test
    @DisplayName("1:1 conversion passes amounts through unchanged")
    void oneToOnePassthrough() {
        TestEnergyContainer container = new TestEnergyContainer(1000);
        EnergyContainerStorage storage = new EnergyContainerStorage(container, RATE_1);
        try (Transaction tx = Transaction.openOuter()) {
            assertEquals(5, storage.insert(5, tx));
            assertEquals(3, storage.extract(3, tx));
            tx.commit();
        }
        assertEquals(2, container.getEnergy());
    }

    // --- External Fabric energy storage consumed by Mekanism (StorageEnergyHandler) ---

    @Test
    @DisplayName("receiveEnergy: SIMULATE does not mutate, EXECUTE commits")
    void handlerSimulatePurity() {
        SimpleEnergyStorage target = new SimpleEnergyStorage(1000, 1000, 1000);
        StorageEnergyHandler handler = new StorageEnergyHandler(target);

        assertEquals(100, handler.receiveEnergy(100, true));
        assertEquals(0, target.getAmount(), "simulate must not mutate the target");

        assertEquals(100, handler.receiveEnergy(100, false));
        assertEquals(100, target.getAmount());
    }

    @Test
    @DisplayName("extractEnergy is exact and simulate-pure")
    void handlerExtract() {
        SimpleEnergyStorage source = new SimpleEnergyStorage(1000, 1000, 1000);
        source.amount = 50;
        StorageEnergyHandler handler = new StorageEnergyHandler(source);

        assertEquals(30, handler.extractEnergy(30, true));
        assertEquals(50, source.getAmount(), "simulate must not mutate the source");

        assertEquals(30, handler.extractEnergy(30, false));
        assertEquals(20, source.getAmount());
    }

    @Test
    @DisplayName("insertion/extraction support flags pass through")
    void handlerSupportFlags() {
        StorageEnergyHandler noInsert = new StorageEnergyHandler(new SimpleEnergyStorage(1000, 0, 1000));
        assertFalse(noInsert.canReceive());
        assertTrue(noInsert.canExtract());
        assertEquals(0, noInsert.receiveEnergy(10, false));
    }

    @Test
    @DisplayName("round trip conserves energy: units charged in equal units yielded out, container nets zero")
    void roundTripConservation() {
        TestEnergyContainer container = new TestEnergyContainer(1000);
        EnergyContainerStorage storage = new EnergyContainerStorage(container, RATE_2_5);
        long unitsIn;
        long unitsOut;
        try (Transaction tx = Transaction.openOuter()) {
            unitsIn = storage.insert(5, tx);
            tx.commit();
        }
        try (Transaction tx = Transaction.openOuter()) {
            unitsOut = storage.extract(100, tx);
            tx.commit();
        }
        assertEquals(unitsIn, unitsOut, "everything charged must come back out");
        assertEquals(0, container.getEnergy(), "no joules may be created or stranded");
    }
}

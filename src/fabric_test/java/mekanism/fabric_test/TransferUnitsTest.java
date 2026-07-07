package mekanism.fabric_test;

import static org.junit.jupiter.api.Assertions.assertEquals;

import mekanism.fabric_shim.transfer.TransferUnits;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class TransferUnitsTest {

    @Test
    @DisplayName("1000 mB is exactly one bucket (81000 droplets)")
    void bucketIsExact() {
        assertEquals(81_000L, TransferUnits.mbToDroplets(1000));
        assertEquals(1000, TransferUnits.dropletsToMbFloor(81_000L));
    }

    @Test
    @DisplayName("droplets→mB floors: 80 droplets is 0 mB, 161 droplets is 1 mB")
    void flooring() {
        assertEquals(0, TransferUnits.dropletsToMbFloor(80));
        assertEquals(1, TransferUnits.dropletsToMbFloor(81));
        assertEquals(1, TransferUnits.dropletsToMbFloor(161));
        assertEquals(2, TransferUnits.dropletsToMbFloor(162));
    }

    @Test
    @DisplayName("round trip mB→droplets→mB is lossless for any int amount")
    void roundTripLossless() {
        for (int mb : new int[]{0, 1, 999, 1000, 64_000, Integer.MAX_VALUE}) {
            assertEquals(mb, TransferUnits.dropletsToMbFloor(TransferUnits.mbToDroplets(mb)));
        }
    }

    @Test
    @DisplayName("droplets→mB clamps to int range instead of overflowing")
    void clampsToIntRange() {
        assertEquals(Integer.MAX_VALUE, TransferUnits.dropletsToMbFloor(Long.MAX_VALUE));
    }

    @Test
    @DisplayName("alignment floors to multiples of 81")
    void alignment() {
        assertEquals(0L, TransferUnits.floorToMbAligned(80));
        assertEquals(81L, TransferUnits.floorToMbAligned(81));
        assertEquals(81L, TransferUnits.floorToMbAligned(100));
        assertEquals(81_000L, TransferUnits.floorToMbAligned(81_037L));
    }
}

package mekanism.fabric_shim.transfer;

/**
 * Millibucket ↔ droplet conversion for the Fabric transfer boundary. INVARIANTS (see
 * fabric-port/design/transfer-bridge.md — do not change without updating the tests):
 *
 * <ul>
 * <li>1 mB = exactly 81 droplets (1000 mB = 81000 droplets = FluidConstants.BUCKET).</li>
 * <li>Mekanism NEVER stores droplets — every externally visible droplet amount is a multiple of
 *     81, so conversion is lossless in both directions.</li>
 * <li>Droplets→mB always floors (round against the requester); fractional-droplet remainders are
 *     never accepted or reported.</li>
 * </ul>
 */
public final class TransferUnits {

    public static final long DROPLETS_PER_MB = 81L;

    private TransferUnits() {
    }

    /**
     * mB → droplets. Cannot overflow: int mB × 81 always fits in a long.
     */
    public static long mbToDroplets(int mb) {
        return mb * DROPLETS_PER_MB;
    }

    /**
     * Droplets → whole mB, flooring, clamped to int range (fluid amounts are ints upstream).
     */
    public static int dropletsToMbFloor(long droplets) {
        return (int) Math.min(droplets / DROPLETS_PER_MB, Integer.MAX_VALUE);
    }

    /**
     * Largest multiple of 81 that is {@code <= droplets} — the only droplet amounts the bridge is
     * allowed to move.
     */
    public static long floorToMbAligned(long droplets) {
        return droplets - droplets % DROPLETS_PER_MB;
    }
}

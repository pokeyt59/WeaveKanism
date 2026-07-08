package mekanism.fabric_shim.inject;

/**
 * NeoForge patches vanilla {@code BlockEntity} with a no-arg {@code invalidateCapabilities()} (a tile
 * invalidates its own capabilities when its state changes). Injected onto BlockEntity + BlockEntityMixin;
 * a no-op for 1f (Phase 2 turns it into cache eviction on the API lookups).
 */
public interface MekBlockEntityExt {

    default void invalidateCapabilities() {
    }
}

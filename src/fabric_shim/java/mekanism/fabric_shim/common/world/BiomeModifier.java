package mekanism.fabric_shim.common.world;

/**
 * Marker stand-in for NeoForge's {@code BiomeModifier}. Worldgen modification on Fabric goes through
 * {@code fabric-biome-api}'s {@code BiomeModifications} (Phase 3); this exists so
 * {@code DatapackDeferredRegister} type parameters compile.
 */
public interface BiomeModifier {
}

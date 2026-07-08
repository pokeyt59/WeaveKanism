package mekanism.fabric_shim.common.world;

/**
 * Marker stand-in for NeoForge's {@code StructureModifier}. Structure modification on Fabric is Phase
 * 3; this exists so {@code DatapackDeferredRegister} type parameters compile.
 */
public interface StructureModifier {
}

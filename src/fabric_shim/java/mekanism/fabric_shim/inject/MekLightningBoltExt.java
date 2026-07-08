package mekanism.fabric_shim.inject;

/**
 * NeoForge patches {@code LightningBolt} with {@code setDamage(float)} (Mekanism's SPS sets it to 0 to
 * make the bolt visual-only). Injected + LightningBoltMixin; a no-op for 1f — the SPS bolt is cosmetic
 * and lightning damage handling is Phase 3.
 */
public interface MekLightningBoltExt {

    default void setDamage(float damage) {
    }
}

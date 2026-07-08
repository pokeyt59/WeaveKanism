package mekanism.fabric_shim.inject;

import java.util.Set;
import mekanism.fabric_shim.common.EffectCure;
import mekanism.fabric_shim.common.EffectCures;

/**
 * NeoForge patches {@code MobEffectInstance} with {@code getCures()} (which cures can remove it).
 * Injected onto MobEffectInstance + MobEffectInstanceMixin; returns the default cure set for 1f
 * (per-effect cures are Phase 3).
 */
public interface MekMobEffectInstanceExt {

    default Set<EffectCure> getCures() {
        return EffectCures.DEFAULT_CURES;
    }
}

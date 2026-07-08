package mekanism.fabric_shim.common;

import java.util.Set;

/**
 * Stand-in for NeoForge's {@code EffectCures} constants. Compile surface; effect-cure semantics are
 * Phase 3 (nothing consults these tokens yet).
 */
public final class EffectCures {

    public static final EffectCure MILK = EffectCure.get("milk");
    public static final EffectCure HONEY = EffectCure.get("honey");
    public static final EffectCure PROTECTED_BY_TOTEM = EffectCure.get("protected_by_totem");
    public static final Set<EffectCure> DEFAULT_CURES = Set.of(MILK, PROTECTED_BY_TOTEM);

    private EffectCures() {
    }
}

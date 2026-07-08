package mekanism.fabric_shim.common;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Stand-in for NeoForge's {@code EffectCure} (interned by name), the token identifying how a mob
 * effect can be cured. Same surface as upstream.
 */
public final class EffectCure {

    private static final Map<String, EffectCure> CURES = new ConcurrentHashMap<>();

    private final String name;

    private EffectCure(String name) {
        this.name = name;
    }

    public static EffectCure get(String name) {
        return CURES.computeIfAbsent(Objects.requireNonNull(name), EffectCure::new);
    }

    public String name() {
        return this.name;
    }

    @Override
    public String toString() {
        return "EffectCure[" + this.name + ']';
    }
}

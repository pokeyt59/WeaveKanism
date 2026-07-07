package mekanism.fabric_shim.common;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Same surface as net.neoforged.neoforge.common.SoundAction (interned by name).
 */
public final class SoundAction {

    private static final Map<String, SoundAction> ACTIONS = new ConcurrentHashMap<>();

    private final String name;

    private SoundAction(String name) {
        this.name = name;
    }

    public static SoundAction get(String name) {
        return ACTIONS.computeIfAbsent(Objects.requireNonNull(name), SoundAction::new);
    }

    public String name() {
        return name;
    }

    @Override
    public String toString() {
        return "SoundAction[" + name + ']';
    }
}

package mekanism.fabric_shim.client.settings;

import com.mojang.blaze3d.platform.InputConstants;
import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;
import net.minecraft.client.KeyMapping;

/**
 * Side store backing the injected {@code MekKeyMappingExt} accessors — the state NeoForge's
 * KeyMapping patch keeps in added fields (conflict context + modifier), keyed by mapping identity.
 * Defaults mirror NeoForge: {@link KeyConflictContext#UNIVERSAL} and {@link KeyModifier#NONE}.
 * All access is client-thread only (registration + input handling).
 */
public final class KeyMappingHooks {

    private static final Map<KeyMapping, State> STATES = Collections.synchronizedMap(new WeakHashMap<>());

    private KeyMappingHooks() {
    }

    private static final class State {

        IKeyConflictContext conflictContext = KeyConflictContext.UNIVERSAL;
        KeyModifier modifier = KeyModifier.NONE;
    }

    private static State state(KeyMapping mapping) {
        return STATES.computeIfAbsent(mapping, k -> new State());
    }

    public static IKeyConflictContext getConflictContext(KeyMapping mapping) {
        return state(mapping).conflictContext;
    }

    public static void setConflictContext(KeyMapping mapping, IKeyConflictContext context) {
        state(mapping).conflictContext = context;
    }

    public static KeyModifier getModifier(KeyMapping mapping) {
        return state(mapping).modifier;
    }

    public static void setModifierAndCode(KeyMapping mapping, KeyModifier modifier, InputConstants.Key key) {
        state(mapping).modifier = modifier == null ? KeyModifier.NONE : modifier;
        mapping.setKey(key);
    }
}

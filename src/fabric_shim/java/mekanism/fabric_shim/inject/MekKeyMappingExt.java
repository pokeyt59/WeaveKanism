package mekanism.fabric_shim.inject;

import com.mojang.blaze3d.platform.InputConstants;
import mekanism.fabric_shim.client.settings.IKeyConflictContext;
import mekanism.fabric_shim.client.settings.KeyMappingHooks;
import mekanism.fabric_shim.client.settings.KeyModifier;
import net.minecraft.client.KeyMapping;

/**
 * NeoForge's KeyMapping patch methods (conflict context + modifier), interface-injected onto
 * {@link KeyMapping} and backed by the {@link KeyMappingHooks} side store. The patched
 * constructors can't be injected — subclass ctors (MekKeyBinding) are hand-edited to call the
 * vanilla ctor and then {@code setKeyConflictContext}/{@code setKeyModifierAndCode}.
 *
 * <p>Scope note: Mekanism's own key handling consults these directly (MekKeyBinding/MekKeyHandler
 * guard on {@code isConflictContextAndModifierActive}), which preserves modifier/context behavior
 * for its keybinds. Vanilla's dispatch and the controls screen are NOT patched — conflict
 * highlighting there is cosmetic and stays absent (deviation row).
 */
public interface MekKeyMappingExt {

    /** NeoForge's accessor for the currently-bound key (vanilla's field is private; AW'd). */
    default InputConstants.Key getKey() {
        return ((KeyMapping) this).key;
    }

    default IKeyConflictContext getKeyConflictContext() {
        return KeyMappingHooks.getConflictContext((KeyMapping) this);
    }

    default void setKeyConflictContext(IKeyConflictContext context) {
        KeyMappingHooks.setConflictContext((KeyMapping) this, context);
    }

    default KeyModifier getKeyModifier() {
        return KeyMappingHooks.getModifier((KeyMapping) this);
    }

    default void setKeyModifierAndCode(KeyModifier modifier, InputConstants.Key key) {
        KeyMappingHooks.setModifierAndCode((KeyMapping) this, modifier, key);
    }

    default boolean isConflictContextAndModifierActive() {
        return getKeyConflictContext().isActive() && getKeyModifier().isActive(getKeyConflictContext());
    }

    /** NeoForge's modifier/context-aware key test (vanilla's matches has no modifier gate). */
    default boolean isActiveAndMatches(InputConstants.Key keyCode) {
        return keyCode != InputConstants.UNKNOWN && keyCode.equals(getKey()) && isConflictContextAndModifierActive();
    }
}

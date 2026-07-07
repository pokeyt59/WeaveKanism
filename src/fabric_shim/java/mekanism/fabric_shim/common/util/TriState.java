package mekanism.fabric_shim.common.util;

/**
 * Same surface as net.neoforged.neoforge.common.util.TriState.
 */
public enum TriState {
    TRUE,
    DEFAULT,
    FALSE;

    public boolean isTrue() {
        return this == TRUE;
    }

    public boolean isFalse() {
        return this == FALSE;
    }

    public boolean isDefault() {
        return this == DEFAULT;
    }
}

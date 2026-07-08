package mekanism.fabric_shim.client.key;

import net.minecraft.client.KeyMapping;

/**
 * Server-safe stand-in for {@code mekanism.client.key.MekKeyHandler}. Common code queries key state
 * through here (tooltips / mode display); on a server nothing is pressed. Real keybinding state is
 * Phase 4.
 */
public final class MekKeyHandler {

    private MekKeyHandler() {
    }

    public static boolean isKeyPressed(KeyMapping keyBinding) {
        return false;
    }

    public static boolean isRadialPressed() {
        return false;
    }
}

package mekanism.fabric_shim.client.key;

import net.minecraft.client.KeyMapping;
import org.jetbrains.annotations.Nullable;

/**
 * Server-safe stand-in for {@code mekanism.client.key.MekanismKeyHandler}. Common code references the
 * key mappings as opaque tokens passed to {@link MekKeyHandler#isKeyPressed}; they stay {@code null}
 * on the server (never constructed, so no client {@code KeyMapping} registration runs). Phase 4.
 */
public final class MekanismKeyHandler {

    @Nullable
    public static final KeyMapping boostKey = null;
    @Nullable
    public static final KeyMapping descriptionKey = null;
    @Nullable
    public static final KeyMapping detailsKey = null;

    private MekanismKeyHandler() {
    }
}

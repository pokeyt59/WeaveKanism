package mekanism.fabric_shim.client.render.hud;

/**
 * Server-safe stand-in for {@code mekanism.client.render.hud.MekanismStatusOverlay}. Common code pokes
 * {@code INSTANCE.setTimer()} to flash the mode-change HUD; a no-op on the server. Phase 4 renders it.
 */
public final class MekanismStatusOverlay {

    public static final MekanismStatusOverlay INSTANCE = new MekanismStatusOverlay();

    private MekanismStatusOverlay() {
    }

    public void setTimer() {
    }
}

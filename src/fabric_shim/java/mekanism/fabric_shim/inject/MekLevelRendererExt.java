package mekanism.fabric_shim.inject;

import net.minecraft.client.renderer.LevelRenderer;

/**
 * NeoForge's LevelRenderer patch surface: public accessor for the private tick counter (AW'd).
 * Injected onto {@link LevelRenderer} + LevelRendererMixin; Mekanism uses it to phase item-render
 * animations (energy cube core).
 */
public interface MekLevelRendererExt {

    default int getTicks() {
        return ((LevelRenderer) this).ticks;
    }
}

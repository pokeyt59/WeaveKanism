package mekanism.fabric_shim.client.render;

import mekanism.common.lib.effect.BoltEffect;

/**
 * Server-safe stand-in for {@code mekanism.client.render.RenderTickHandler}. Common code only queues a
 * lightning bolt to render ({@link #renderBolt}); a no-op on the server. Phase 4 renders it.
 */
public final class RenderTickHandler {

    private RenderTickHandler() {
    }

    public static void renderBolt(Object renderer, BoltEffect bolt) {
    }

    public static void clearQueued() {
    }
}

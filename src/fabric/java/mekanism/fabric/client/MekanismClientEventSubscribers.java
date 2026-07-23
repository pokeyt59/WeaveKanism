package mekanism.fabric.client;

import mekanism.client.ClientRegistration;
import mekanism.client.MekanismClient;
import mekanism.client.render.MekanismRenderer;
import mekanism.client.render.MekanismShaders;
import mekanism.client.sound.SoundHandler;
import mekanism.fabric_shim.common.NeoForge;
import mekanism.fabric_shim.internal.ShimBuses;

/**
 * Explicit registration of Mekanism's client-dist {@code @EventBusSubscriber} classes (the client
 * counterpart of MekanismEventSubscribers — FML's annotation scan on NeoForge, a hand-kept list
 * here; <b>new upstream client {@code @EventBusSubscriber} classes must be added here</b>).
 * Routed per bus because the shim MOD bus enforces the IModBusEvent marker:
 * ClientRegistration/MekanismShaders/MekanismRenderer/SoundHandler carry only mod-bus statics;
 * MekanismClient's one static listener (respawn clone) is a game-bus event.
 */
public final class MekanismClientEventSubscribers {

    private MekanismClientEventSubscribers() {
    }

    public static void registerClient() {
        ShimBuses.MOD_BUS.register(ClientRegistration.class);
        ShimBuses.MOD_BUS.register(MekanismShaders.class);
        ShimBuses.MOD_BUS.register(MekanismRenderer.class);
        ShimBuses.MOD_BUS.register(SoundHandler.class);
        NeoForge.EVENT_BUS.register(MekanismClient.class);
    }
}

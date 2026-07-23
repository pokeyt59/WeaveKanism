package mekanism.fabric.client;

import mekanism.client.MekanismClient;
import mekanism.fabric.MekanismFabric;
import mekanism.fabric_shim.client.event.EntityRenderersEvent;
import mekanism.fabric_shim.client.event.ModelEvent;
import mekanism.fabric_shim.client.event.RegisterColorHandlersEvent;
import mekanism.fabric_shim.client.event.RegisterGuiLayersEvent;
import mekanism.fabric_shim.client.event.RegisterItemDecorationsEvent;
import mekanism.fabric_shim.client.event.RegisterKeyMappingsEvent;
import mekanism.fabric_shim.client.event.RegisterMenuScreensEvent;
import mekanism.fabric_shim.client.event.RegisterParticleProvidersEvent;
import mekanism.fabric_shim.client.extensions.RegisterClientExtensionsEvent;
import mekanism.fabric_shim.client.gui.GuiLayerHooks;
import mekanism.fabric_shim.fml.event.lifecycle.FMLClientSetupEvent;
import mekanism.fabric_shim.internal.ShimBuses;
import mekanism.fabric_shim.network.handling.IPayloadContext;
import mekanism.fabric_shim.network.registration.PendingClientReceivers;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientConfigurationNetworking;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.entity.player.Player;

/**
 * Fabric client bootstrap (Phase 4). Mirrors what NeoForge's client mod loading does: construct
 * the client {@code @Mod} class, register the client {@code @EventBusSubscriber} classes, then
 * post the client mod-bus registration events. Fabric runs client entry points inside the
 * Minecraft constructor, before the render/particle/model subsystems consume any of these
 * registrations, so posting them here in one documented sequence is equivalent to NeoForge firing
 * each from its vanilla init site — every consumer is Mekanism-registered data read later at
 * runtime. Events that need live runtime state post elsewhere: RegisterShadersEvent from
 * GameRendererMixin (shader reload tail), EntityRenderersEvent.AddLayers from
 * EntityRenderDispatcherMixin (renderer rebuild tail); atlas/sound-engine/model-bake events are
 * step 7b/7c wiring.
 * <p>
 * Also drains the clientbound payload handlers that {@code PayloadRegistrar} parked during common
 * init into live {@link ClientPlayNetworking}/{@link ClientConfigurationNetworking} receivers —
 * Fabric runs both on the game thread, matching the NeoForge main-thread handler default the
 * server side (ServerPlayNetworking) already relies on.
 */
public class MekanismFabricClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        //Client @Mod construction (registers the config-screen extension point on the shared container)
        new MekanismClient(MekanismFabric.modContainer);
        MekanismClientEventSubscribers.registerClient();

        //Client mod-bus registration events. The shims apply straight into the Fabric/vanilla
        // registries (or Hooks stores read at runtime), so posting order only has to respect
        // Mekanism-internal dependencies — FMLClientSetup populates the custom-model map and
        // game-bus listeners before anything bakes or ticks.
        ShimBuses.MOD_BUS.post(new RegisterKeyMappingsEvent());
        ShimBuses.MOD_BUS.post(new FMLClientSetupEvent());
        ShimBuses.MOD_BUS.post(new EntityRenderersEvent.RegisterLayerDefinitions());
        ShimBuses.MOD_BUS.post(new EntityRenderersEvent.RegisterRenderers());
        ShimBuses.MOD_BUS.post(new RegisterMenuScreensEvent());
        ShimBuses.MOD_BUS.post(new RegisterParticleProvidersEvent());
        ShimBuses.MOD_BUS.post(new RegisterColorHandlersEvent.Block());
        ShimBuses.MOD_BUS.post(new RegisterColorHandlersEvent.Item());
        ShimBuses.MOD_BUS.post(new RegisterItemDecorationsEvent());
        ShimBuses.MOD_BUS.post(new RegisterClientExtensionsEvent());
        ShimBuses.MOD_BUS.post(new RegisterGuiLayersEvent());
        ShimBuses.MOD_BUS.post(new ModelEvent.RegisterGeometryLoaders());
        ShimBuses.MOD_BUS.post(new ModelEvent.RegisterAdditional());
        //RegisterClientReloadListenersEvent posts from MinecraftMixin instead: Mekanism's handler
        // constructs a TextureAtlas, and here the GL context does not exist yet (verified crash)

        //HUD overlays collected by the RegisterGuiLayersEvent shim, dispatched in registration
        // order (vanilla-relative ordering is a PORTING.md deviation)
        HudRenderCallback.EVENT.register((graphics, delta) -> {
            for (GuiLayerHooks.NamedLayer layer : GuiLayerHooks.layers()) {
                layer.layer().render(graphics, delta);
            }
        });

        for (PendingClientReceivers.Entry<?> entry : PendingClientReceivers.clientboundPlay()) {
            registerPlayReceiver(entry);
        }
        for (PendingClientReceivers.Entry<?> entry : PendingClientReceivers.clientboundConfig()) {
            registerConfigReceiver(entry);
        }
        MekanismFabric.LOGGER.info("Mekanism Fabric client bootstrap complete ({} play + {} configuration receivers)",
              PendingClientReceivers.clientboundPlay().size(), PendingClientReceivers.clientboundConfig().size());
    }

    private static <T extends CustomPacketPayload> void registerPlayReceiver(PendingClientReceivers.Entry<T> entry) {
        ClientPlayNetworking.registerGlobalReceiver(entry.type(), (payload, context) ->
              entry.handler().handle(payload, new ClientPlayContext(context)));
    }

    private static <T extends CustomPacketPayload> void registerConfigReceiver(PendingClientReceivers.Entry<T> entry) {
        ClientConfigurationNetworking.registerGlobalReceiver(entry.type(), (payload, context) ->
              entry.handler().handle(payload, new ClientConfigContext(context)));
    }

    private record ClientPlayContext(ClientPlayNetworking.Context context) implements IPayloadContext {

        @Override
        public Player player() {
            return context.player();
        }

        @Override
        public void disconnect(Component reason) {
            context.player().connection.getConnection().disconnect(reason);
        }
    }

    private record ClientConfigContext(ClientConfigurationNetworking.Context context) implements IPayloadContext {

        /** Null during the configuration phase; no Mekanism configuration handler reads it. */
        @Override
        public Player player() {
            return context.client().player;
        }

        @Override
        public void disconnect(Component reason) {
            //No Mekanism client handler disconnects, and the config listener exposes no public
            // connection accessor — log instead of reaching through protected state
            MekanismFabric.LOGGER.warn("Ignoring disconnect request during configuration phase: {}", reason.getString());
        }
    }
}

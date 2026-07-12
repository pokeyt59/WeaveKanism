package mekanism.fabric.client;

import mekanism.fabric.MekanismFabric;
import mekanism.fabric_shim.network.handling.IPayloadContext;
import mekanism.fabric_shim.network.registration.PendingClientReceivers;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientConfigurationNetworking;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.entity.player.Player;

/**
 * Fabric client bootstrap (Phase 4). First slice: drain the clientbound payload handlers that
 * {@code PayloadRegistrar} parked during common init into live
 * {@link ClientPlayNetworking}/{@link ClientConfigurationNetworking} receivers — Fabric runs both
 * on the game thread, matching the NeoForge main-thread handler default the server side
 * (ServerPlayNetworking) already relies on. {@code mekanism.client.ClientRegistration}'s mod-bus
 * registrations (models, screens, keybinds, renderers) move here with the Phase 4 compile grind
 * (see PORTING.md).
 */
public class MekanismFabricClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
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

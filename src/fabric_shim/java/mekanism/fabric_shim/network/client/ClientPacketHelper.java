package mekanism.fabric_shim.network.client;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

/**
 * Client-only sink for {@code PacketDistributor.sendToServer}. Isolated in its own class (touching
 * {@code ClientPlayNetworking}, which pulls in client-only Minecraft classes) so the common
 * {@link mekanism.fabric_shim.network.PacketDistributor} loads on a dedicated server; this class is
 * loaded only when a serverbound send actually runs, which only happens on the client.
 */
public final class ClientPacketHelper {

    private ClientPacketHelper() {
    }

    public static void sendToServer(CustomPacketPayload payload) {
        ClientPlayNetworking.send(payload);
    }
}

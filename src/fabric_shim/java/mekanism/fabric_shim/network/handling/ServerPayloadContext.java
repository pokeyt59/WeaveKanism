package mekanism.fabric_shim.network.handling;

import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;

/**
 * Server-side {@link IPayloadContext} backing a client-to-server play packet, wrapping Fabric's
 * {@link ServerPlayNetworking.Context}.
 */
public final class ServerPayloadContext implements IPayloadContext {

    private final ServerPlayNetworking.Context context;

    public ServerPayloadContext(ServerPlayNetworking.Context context) {
        this.context = context;
    }

    @Override
    public Player player() {
        return context.player();
    }

    @Override
    public void disconnect(Component reason) {
        context.player().connection.disconnect(reason);
    }
}

package mekanism.fabric_shim.network.handling;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;

/**
 * Stand-in for NeoForge's {@code IPayloadContext} carrying the slice Mekanism's packet handlers use:
 * the {@link #player()} the packet is being handled for and {@link #disconnect(Component)}. Concrete
 * implementations wrap Fabric's per-side networking context (server side today via
 * {@link ServerPayloadContext}; the client side arrives with the Phase 4 client entry point).
 */
public interface IPayloadContext {

    /**
     * The player this packet is handled for: the sending player on the server, or the local player on
     * the client.
     */
    Player player();

    /**
     * Disconnects the connection this packet arrived on (used to kick a client that sent a malformed
     * packet).
     */
    void disconnect(Component reason);
}

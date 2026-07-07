package mekanism.fabric_shim.event.server;

import net.minecraft.server.MinecraftServer;

/**
 * Fired when the server begins an orderly shutdown. Mapped from Fabric's
 * ServerLifecycleEvents.SERVER_STOPPING.
 */
public class ServerStoppingEvent extends ServerLifecycleEvent {

    public ServerStoppingEvent(MinecraftServer server) {
        super(server);
    }
}

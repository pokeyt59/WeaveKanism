package mekanism.fabric_shim.event.server;

import net.minecraft.server.MinecraftServer;

/**
 * Fired when the server is fully started and ready to play. Mapped from Fabric's
 * ServerLifecycleEvents.SERVER_STARTED.
 */
public class ServerStartedEvent extends ServerLifecycleEvent {

    public ServerStartedEvent(MinecraftServer server) {
        super(server);
    }
}

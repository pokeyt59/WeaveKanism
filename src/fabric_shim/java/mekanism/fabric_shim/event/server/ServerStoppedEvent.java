package mekanism.fabric_shim.event.server;

import net.minecraft.server.MinecraftServer;

/**
 * Fired after the server has completely shut down. Mapped from Fabric's
 * ServerLifecycleEvents.SERVER_STOPPED.
 */
public class ServerStoppedEvent extends ServerLifecycleEvent {

    public ServerStoppedEvent(MinecraftServer server) {
        super(server);
    }
}

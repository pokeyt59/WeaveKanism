package mekanism.fabric_shim.event.server;

import net.minecraft.server.MinecraftServer;

/**
 * Fired before the server loads any levels. Mapped from Fabric's
 * ServerLifecycleEvents.SERVER_STARTING.
 */
public class ServerAboutToStartEvent extends ServerLifecycleEvent {

    public ServerAboutToStartEvent(MinecraftServer server) {
        super(server);
    }
}

package mekanism.fabric_shim.event.server;

import net.minecraft.server.MinecraftServer;

/**
 * On NeoForge this fires after levels are loaded but before the server is marked ready; Fabric has
 * no hook between those points, so the glue posts it immediately before {@link ServerStartedEvent}
 * from ServerLifecycleEvents.SERVER_STARTED — preserving the "levels are loaded" invariant
 * listeners rely on.
 */
public class ServerStartingEvent extends ServerLifecycleEvent {

    public ServerStartingEvent(MinecraftServer server) {
        super(server);
    }
}

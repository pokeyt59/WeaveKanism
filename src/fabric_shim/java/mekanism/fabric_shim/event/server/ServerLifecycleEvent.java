package mekanism.fabric_shim.event.server;

import net.minecraft.server.MinecraftServer;
import net.neoforged.bus.api.Event;

/**
 * Base of the server lifecycle event family (stand-in for
 * net.neoforged.neoforge.event.server.ServerLifecycleEvent). Posted on the game bus by the shim's
 * Fabric lifecycle glue (see ShimGameEvents).
 */
public abstract class ServerLifecycleEvent extends Event {

    protected final MinecraftServer server;

    public ServerLifecycleEvent(MinecraftServer server) {
        this.server = server;
    }

    public MinecraftServer getServer() {
        return server;
    }
}

package mekanism.fabric_shim.fml;

import mekanism.fabric_shim.server.ServerLifecycleHooks;
import net.minecraft.server.MinecraftServer;

/**
 * Stand-in for net.neoforged.fml.util.thread.EffectiveSide. NeoForge decides by thread group; on
 * Fabric we approximate: on a dedicated server everything is SERVER, otherwise the server thread
 * (integrated server) is SERVER and any other thread is CLIENT.
 */
public final class EffectiveSide {

    private EffectiveSide() {
    }

    public static LogicalSide get() {
        if (FMLEnvironment.dist.isDedicatedServer()) {
            return LogicalSide.SERVER;
        }
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        return server != null && server.isSameThread() ? LogicalSide.SERVER : LogicalSide.CLIENT;
    }
}

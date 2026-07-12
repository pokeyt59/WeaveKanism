package mekanism.fabric_shim.internal;

import mekanism.fabric_shim.network.event.RegisterConfigurationTasksEvent;
import net.fabricmc.fabric.api.networking.v1.ServerConfigurationConnectionEvents;
import net.minecraft.server.network.ConfigurationTask;

/**
 * Phase 3 configuration-phase glue. NeoForge fires {@code RegisterConfigurationTasksEvent} (mod bus)
 * per connecting client and appends the collected tasks to the vanilla configuration-task queue.
 * Fabric's {@code CONFIGURE} callback is the same moment — vanilla configuration about to run, the
 * channel handshake already complete — and the interface-injected
 * {@code FabricServerConfigurationNetworkHandler#addTask} appends to the same vanilla queue, so
 * self-finishing tasks like Mekanism's {@code SyncAllSecurityData} (send payload, then vanilla
 * {@code finishCurrentTask}) drive exactly like upstream.
 *
 * <p>Deliberately NOT {@code BEFORE_CONFIGURE}: tasks queued there run under Fabric's own
 * early-task poller and must complete via Fabric's {@code completeTask}; a task calling vanilla
 * {@code finishCurrentTask} from that phase would desync the state machine.
 *
 * <p>Deviation (PORTING.md): this also fires for clients without the mod (a NeoForge server
 * rejects them during negotiation instead). Harmless for self-finishing tasks — the unknown
 * payload is discarded client-side and the client joins without the synced data.
 */
public final class ShimConfigurationTasks {

    private ShimConfigurationTasks() {
    }

    public static void init() {
        ServerConfigurationConnectionEvents.CONFIGURE.register((handler, server) -> {
            RegisterConfigurationTasksEvent event = new RegisterConfigurationTasksEvent(handler);
            ShimBuses.MOD_BUS.post(event);
            for (ConfigurationTask task : event.getConfigurationTasks()) {
                handler.addTask(task);
            }
        });
    }
}

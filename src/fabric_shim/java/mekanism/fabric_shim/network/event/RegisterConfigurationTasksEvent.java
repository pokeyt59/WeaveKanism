package mekanism.fabric_shim.network.event;

import java.util.ArrayDeque;
import java.util.Queue;
import mekanism.fabric_shim.fml.event.IModBusEvent;
import net.minecraft.network.protocol.configuration.ServerConfigurationPacketListener;
import net.minecraft.server.network.ConfigurationTask;
import net.neoforged.bus.api.Event;

/**
 * Stand-in for NeoForge's {@code RegisterConfigurationTasksEvent}. Same surface, but NOT yet posted:
 * on NeoForge this fires per connecting client during the configuration phase. The Fabric equivalent
 * (driving tasks off {@code ServerConfigurationConnectionEvents}) is Phase 3 — until then Mekanism's
 * listener registers but never runs, so {@code SyncAllSecurityData} is not sent during config.
 * Tracked in the hook-wiring checklist.
 */
public class RegisterConfigurationTasksEvent extends Event implements IModBusEvent {

    private final ServerConfigurationPacketListener listener;
    private final Queue<ConfigurationTask> tasks = new ArrayDeque<>();

    public RegisterConfigurationTasksEvent(ServerConfigurationPacketListener listener) {
        this.listener = listener;
    }

    public void register(ConfigurationTask task) {
        this.tasks.add(task);
    }

    public Queue<ConfigurationTask> getConfigurationTasks() {
        return this.tasks;
    }

    public ServerConfigurationPacketListener getListener() {
        return this.listener;
    }
}

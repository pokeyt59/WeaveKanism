package mekanism.fabric_shim.network.event;

import java.util.ArrayDeque;
import java.util.Queue;
import mekanism.fabric_shim.fml.event.IModBusEvent;
import net.minecraft.network.protocol.configuration.ServerConfigurationPacketListener;
import net.minecraft.server.network.ConfigurationTask;
import net.neoforged.bus.api.Event;

/**
 * Stand-in for NeoForge's {@code RegisterConfigurationTasksEvent}. Same surface; posted per
 * connecting client from {@code ShimConfigurationTasks} (Fabric's
 * {@code ServerConfigurationConnectionEvents.CONFIGURE}), with the collected tasks appended to the
 * vanilla configuration-task queue — so {@code SyncAllSecurityData} runs during config like on
 * NeoForge. Note the post happens on the connection's netty thread, same as upstream.
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

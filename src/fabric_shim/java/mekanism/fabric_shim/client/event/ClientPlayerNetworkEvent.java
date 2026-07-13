package mekanism.fabric_shim.client.event;

import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.Connection;
import net.neoforged.bus.api.Event;

/**
 * Stand-in for NeoForge's {@code ClientPlayerNetworkEvent} (game bus). LoggingIn/LoggingOut bridge
 * from Fabric's ClientPlayConnectionEvents JOIN/DISCONNECT; Clone fires on respawn/dimension change.
 * Only the slice Mekanism reads (getConnection + Clone's old/new player). Fresh implementation.
 */
public abstract class ClientPlayerNetworkEvent extends Event {

    private final Connection connection;

    protected ClientPlayerNetworkEvent(Connection connection) {
        this.connection = connection;
    }

    public Connection getConnection() {
        return connection;
    }

    public static class LoggingIn extends ClientPlayerNetworkEvent {

        public LoggingIn(Connection connection) {
            super(connection);
        }
    }

    public static class LoggingOut extends ClientPlayerNetworkEvent {

        public LoggingOut(Connection connection) {
            super(connection);
        }
    }

    public static class Clone extends ClientPlayerNetworkEvent {

        private final LocalPlayer oldPlayer;
        private final LocalPlayer newPlayer;

        public Clone(LocalPlayer oldPlayer, LocalPlayer newPlayer, Connection connection) {
            super(connection);
            this.oldPlayer = oldPlayer;
            this.newPlayer = newPlayer;
        }

        public LocalPlayer getOldPlayer() {
            return oldPlayer;
        }

        public LocalPlayer getNewPlayer() {
            return newPlayer;
        }
    }
}

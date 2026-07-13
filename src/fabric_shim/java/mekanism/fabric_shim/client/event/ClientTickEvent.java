package mekanism.fabric_shim.client.event;

import net.neoforged.bus.api.Event;

/**
 * Stand-in for NeoForge's {@code ClientTickEvent} (game bus). Pre/Post are markers Mekanism uses to
 * hang start/end-of-client-tick work; posted from ClientTickEvents.START/END_CLIENT_TICK at the
 * step-3b bridge wiring. Fresh implementation.
 */
public abstract class ClientTickEvent extends Event {

    public static class Pre extends ClientTickEvent {
    }

    public static class Post extends ClientTickEvent {
    }
}

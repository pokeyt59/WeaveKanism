package mekanism.fabric_shim.event.tick;

import java.util.function.BooleanSupplier;
import net.minecraft.server.MinecraftServer;
import net.neoforged.bus.api.Event;

/**
 * Stand-in for NeoForge's {@code ServerTickEvent}. Compile-only: firing (off Fabric's
 * {@code ServerTickEvents.END_SERVER_TICK}) is Phase 3 — see the hook-wiring checklist.
 */
public abstract class ServerTickEvent extends Event {

    private final BooleanSupplier hasTime;
    private final MinecraftServer server;

    protected ServerTickEvent(BooleanSupplier hasTime, MinecraftServer server) {
        this.hasTime = hasTime;
        this.server = server;
    }

    public boolean hasTime() {
        return this.hasTime.getAsBoolean();
    }

    public MinecraftServer getServer() {
        return this.server;
    }

    public static class Pre extends ServerTickEvent {

        public Pre(BooleanSupplier haveTime, MinecraftServer server) {
            super(haveTime, server);
        }
    }

    public static class Post extends ServerTickEvent {

        public Post(BooleanSupplier haveTime, MinecraftServer server) {
            super(haveTime, server);
        }
    }
}

package mekanism.fabric_shim.registries.datamaps;

import java.util.function.Consumer;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.neoforged.bus.api.Event;
import org.jetbrains.annotations.ApiStatus;

/**
 * Fired on the game bus when data map values have been (re)loaded (stand-in for NeoForge's
 * DataMapsUpdatedEvent; same surface). Posted by the shim data-map loader (Phase 3).
 */
public class DataMapsUpdatedEvent extends Event {

    private final Registry<?> registry;
    private final UpdateCause cause;

    @ApiStatus.Internal
    public DataMapsUpdatedEvent(Registry<?> registry, UpdateCause cause) {
        this.registry = registry;
        this.cause = cause;
    }

    public ResourceKey<? extends Registry<?>> getRegistryKey() {
        return registry.key();
    }

    public Registry<?> getRegistry() {
        return registry;
    }

    @SuppressWarnings("unchecked")
    public <T> void ifRegistry(ResourceKey<Registry<T>> type, Consumer<Registry<T>> consumer) {
        //ResourceKeys are interned; identity comparison mirrors NeoForge
        if (getRegistryKey() == type) {
            consumer.accept((Registry<T>) registry);
        }
    }

    public UpdateCause getCause() {
        return cause;
    }

    public enum UpdateCause {
        CLIENT_SYNC,
        SERVER_RELOAD
    }
}

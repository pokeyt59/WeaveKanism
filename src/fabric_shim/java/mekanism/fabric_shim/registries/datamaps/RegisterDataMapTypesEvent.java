package mekanism.fabric_shim.registries.datamaps;

import mekanism.fabric_shim.fml.event.IModBusEvent;
import net.neoforged.bus.api.Event;
import org.jetbrains.annotations.ApiStatus;

/**
 * Fired on the mod bus to collect data map types (stand-in for NeoForge's
 * RegisterDataMapTypesEvent; same surface). Registered types land in {@link DataMaps}.
 */
public class RegisterDataMapTypesEvent extends Event implements IModBusEvent {

    @ApiStatus.Internal
    public RegisterDataMapTypesEvent() {
    }

    public <T, R> void register(DataMapType<R, T> type) {
        DataMaps.registerType(type);
    }
}

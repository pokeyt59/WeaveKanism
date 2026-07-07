package mekanism.fabric_shim.event;

import net.minecraft.core.RegistryAccess;
import net.neoforged.bus.api.Event;

/**
 * Fired when tags are (re)bound on either side (stand-in for
 * net.neoforged.neoforge.event.TagsUpdatedEvent). Mapped from Fabric's
 * CommonLifecycleEvents.TAGS_LOADED.
 */
public class TagsUpdatedEvent extends Event {

    private final RegistryAccess registryAccess;
    private final UpdateCause updateCause;
    private final boolean integratedServer;

    public TagsUpdatedEvent(RegistryAccess registryAccess, boolean fromClientPacket, boolean isIntegratedServerConnection) {
        this.registryAccess = registryAccess;
        this.updateCause = fromClientPacket ? UpdateCause.CLIENT_PACKET_RECEIVED : UpdateCause.SERVER_DATA_LOAD;
        this.integratedServer = isIntegratedServerConnection;
    }

    public RegistryAccess getRegistryAccess() {
        return registryAccess;
    }

    public UpdateCause getUpdateCause() {
        return updateCause;
    }

    public boolean shouldUpdateStaticData() {
        return updateCause == UpdateCause.SERVER_DATA_LOAD || !integratedServer;
    }

    public enum UpdateCause {
        SERVER_DATA_LOAD,
        CLIENT_PACKET_RECEIVED
    }
}

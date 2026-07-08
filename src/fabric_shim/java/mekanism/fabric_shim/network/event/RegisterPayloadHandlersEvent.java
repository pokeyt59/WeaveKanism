package mekanism.fabric_shim.network.event;

import mekanism.fabric_shim.fml.event.IModBusEvent;
import mekanism.fabric_shim.network.registration.PayloadRegistrar;
import net.neoforged.bus.api.Event;

/**
 * Mod-bus event handing out a {@link PayloadRegistrar}; same surface as NeoForge's
 * {@code RegisterPayloadHandlersEvent}. Posted once from the Fabric bootstrap during mod init, so
 * Mekanism's {@code BasePacketHandler} registers its channels/codecs at the correct time.
 */
public class RegisterPayloadHandlersEvent extends Event implements IModBusEvent {

    public PayloadRegistrar registrar(String version) {
        return new PayloadRegistrar(version);
    }
}

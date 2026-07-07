package mekanism.fabric_shim.common.world.chunk;

import java.util.function.Consumer;
import mekanism.fabric_shim.fml.event.IModBusEvent;
import net.neoforged.bus.api.Event;

/**
 * Same surface as net.neoforged.neoforge.common.world.chunk.RegisterTicketControllersEvent. The
 * Fabric bootstrap posts it once after registration (see ShimChunkManager).
 */
public class RegisterTicketControllersEvent extends Event implements IModBusEvent {

    private final Consumer<TicketController> registrar;

    public RegisterTicketControllersEvent(Consumer<TicketController> registrar) {
        this.registrar = registrar;
    }

    public void register(TicketController controller) {
        registrar.accept(controller);
    }
}

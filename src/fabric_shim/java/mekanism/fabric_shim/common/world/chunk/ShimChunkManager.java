package mekanism.fabric_shim.common.world.chunk;

import java.util.LinkedHashMap;
import java.util.Map;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.IEventBus;

/**
 * Holds registered {@link TicketController}s (the slice of NeoForge's ForcedChunkManager the shim
 * needs). {@link #fireRegistration} is called once by the Fabric bootstrap after registry events.
 */
public final class ShimChunkManager {

    private static final Map<ResourceLocation, TicketController> CONTROLLERS = new LinkedHashMap<>();

    private ShimChunkManager() {
    }

    public static void fireRegistration(IEventBus modBus) {
        modBus.post(new RegisterTicketControllersEvent(controller -> {
            if (CONTROLLERS.putIfAbsent(controller.id(), controller) != null) {
                throw new IllegalArgumentException("Duplicate ticket controller registration: " + controller.id());
            }
        }));
    }

    public static Map<ResourceLocation, TicketController> getControllers() {
        return Map.copyOf(CONTROLLERS);
    }
}

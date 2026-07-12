package mekanism.fabric_shim.common.world.chunk;

import java.util.LinkedHashMap;
import java.util.Map;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerWorldEvents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.neoforged.bus.api.IEventBus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Holds registered {@link TicketController}s and drives the load-time half of NeoForge's
 * ForcedChunkManager contract: when a level loads, each controller's
 * {@link LoadingValidationCallback} runs against the persisted tickets (pruning owners whose
 * blocks/entities are gone), then whatever survives is re-applied as region tickets —
 * {@link ForcedChunksSavedData} is the persistent source of truth, the tickets themselves are not
 * persistent. {@link #fireRegistration} is called once by the Fabric bootstrap after registry
 * events; {@link #init} once during bootstrap.
 */
public final class ShimChunkManager {

    private static final Logger LOGGER = LoggerFactory.getLogger("MekanismShim");
    private static final Map<ResourceLocation, TicketController> CONTROLLERS = new LinkedHashMap<>();

    private ShimChunkManager() {
    }

    public static void init() {
        ServerWorldEvents.LOAD.register((server, level) -> onLevelLoad(level));
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

    private static void onLevelLoad(ServerLevel level) {
        ForcedChunksSavedData data = ForcedChunksSavedData.get(level);
        if (data.isEmpty()) {
            return;
        }
        //Validation before application, NeoForge's order: callbacks see (and prune) the persisted
        // tickets before any of them become region tickets. forceChunk calls made from inside a
        // callback apply immediately and dedup against the bulk application below.
        for (ResourceLocation id : data.controllerIds()) {
            TicketController controller = CONTROLLERS.get(id);
            if (controller == null) {
                LOGGER.warn("Level {} has forced chunks for unregistered ticket controller {}; leaving them unapplied but persisted",
                      level.dimension().location(), id);
            } else if (controller.callback() != null) {
                controller.callback().validateTickets(level, data.createHelper(level, id));
            }
        }
        data.applyAll(level);
    }
}

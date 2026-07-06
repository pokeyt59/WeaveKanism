package mekanism.fabric_shim.event.entity;

import net.minecraft.world.entity.Entity;
import net.neoforged.bus.api.Event;

/**
 * Base class for entity events (stand-in for NeoForge's EntityEvent; same surface).
 */
public class EntityEvent extends Event {

    private final Entity entity;

    public EntityEvent(Entity entity) {
        this.entity = entity;
    }

    public Entity getEntity() {
        return entity;
    }
}

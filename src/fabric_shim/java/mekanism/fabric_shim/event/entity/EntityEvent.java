package mekanism.fabric_shim.event.entity;

import net.minecraft.world.entity.Entity;
import net.neoforged.bus.api.Event;

/**
 * Base class for entity events (stand-in for NeoForge's EntityEvent; same surface). Abstract like
 * NeoForge's — the bus requires abstract event classes (LivingEvent, EntityTickEvent) to have
 * abstract superclasses.
 */
public abstract class EntityEvent extends Event {

    private final Entity entity;

    protected EntityEvent(Entity entity) {
        this.entity = entity;
    }

    public Entity getEntity() {
        return entity;
    }
}

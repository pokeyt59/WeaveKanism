package mekanism.fabric_shim.event.tick;

import mekanism.fabric_shim.event.entity.EntityEvent;
import net.minecraft.world.entity.Entity;
import net.neoforged.bus.api.ICancellableEvent;

/**
 * Stand-in for NeoForge's {@code EntityTickEvent}. Compile-only; firing is Phase 3.
 */
public abstract class EntityTickEvent extends EntityEvent {

    protected EntityTickEvent(Entity entity) {
        super(entity);
    }

    public static class Pre extends EntityTickEvent implements ICancellableEvent {

        public Pre(Entity entity) {
            super(entity);
        }
    }

    public static class Post extends EntityTickEvent {

        public Post(Entity entity) {
            super(entity);
        }
    }
}

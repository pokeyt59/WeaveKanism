package mekanism.fabric_shim.event.entity;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.ICancellableEvent;

/**
 * Stand-in for NeoForge's {@code EntityJoinLevelEvent}. Compile-only; firing (off Fabric's
 * {@code ServerEntityEvents.ENTITY_LOAD}) is Phase 3.
 */
public class EntityJoinLevelEvent extends EntityEvent implements ICancellableEvent {

    private final Level level;
    private final boolean loadedFromDisk;

    public EntityJoinLevelEvent(Entity entity, Level level) {
        this(entity, level, false);
    }

    public EntityJoinLevelEvent(Entity entity, Level level, boolean loadedFromDisk) {
        super(entity);
        this.level = level;
        this.loadedFromDisk = loadedFromDisk;
    }

    public Level getLevel() {
        return this.level;
    }

    public boolean loadedFromDisk() {
        return this.loadedFromDisk;
    }
}

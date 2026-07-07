package mekanism.fabric_shim.event.level;

import net.minecraft.world.level.LevelAccessor;
import net.neoforged.bus.api.Event;

/**
 * Level lifecycle event family (stand-in for net.neoforged.neoforge.event.level.LevelEvent).
 * Load/Unload are mapped from Fabric's ServerWorldEvents; client-side level load/unload posting
 * lands with the Phase 4 client glue.
 */
public abstract class LevelEvent extends Event {

    private final LevelAccessor level;

    public LevelEvent(LevelAccessor level) {
        this.level = level;
    }

    public LevelAccessor getLevel() {
        return level;
    }

    public static class Load extends LevelEvent {

        public Load(LevelAccessor level) {
            super(level);
        }
    }

    public static class Unload extends LevelEvent {

        public Unload(LevelAccessor level) {
            super(level);
        }
    }

    public static class Save extends LevelEvent {

        public Save(LevelAccessor level) {
            super(level);
        }
    }
}

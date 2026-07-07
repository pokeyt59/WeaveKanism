package mekanism.fabric_shim.event.level;

import net.minecraft.world.level.Level;
import net.minecraft.world.level.Explosion;
import net.neoforged.bus.api.Event;
import net.neoforged.bus.api.ICancellableEvent;

/**
 * Same surface (the slice Mekanism uses) as net.neoforged.neoforge.event.level.ExplosionEvent.
 */
public abstract class ExplosionEvent extends Event {

    private final Level level;
    private final Explosion explosion;

    public ExplosionEvent(Level level, Explosion explosion) {
        this.level = level;
        this.explosion = explosion;
    }

    public Level getLevel() {
        return level;
    }

    public Explosion getExplosion() {
        return explosion;
    }

    public static class Start extends ExplosionEvent implements ICancellableEvent {

        public Start(Level level, Explosion explosion) {
            super(level, explosion);
        }
    }
}

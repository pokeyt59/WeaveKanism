package mekanism.fabric_shim.client.event.sound;

import net.minecraft.client.sounds.SoundEngine;
import net.neoforged.bus.api.Event;

/**
 * Same surface as NeoForge's {@code SoundEvent} base: carries the {@link SoundEngine}. Client-only.
 * Subclasses: {@link SoundEngineLoadEvent} (mod bus, engine (re)load) and {@link PlaySoundEvent}
 * (game bus, per-sound). Fresh implementation.
 */
public abstract class SoundEvent extends Event {

    private final SoundEngine engine;

    protected SoundEvent(SoundEngine engine) {
        this.engine = engine;
    }

    public SoundEngine getEngine() {
        return engine;
    }
}

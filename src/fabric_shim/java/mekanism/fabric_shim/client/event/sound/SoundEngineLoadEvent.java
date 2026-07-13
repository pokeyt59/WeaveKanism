package mekanism.fabric_shim.client.event.sound;

import mekanism.fabric_shim.fml.event.IModBusEvent;
import net.minecraft.client.sounds.SoundEngine;

/**
 * Stand-in for NeoForge's {@code SoundEngineLoadEvent}: fired on the mod bus when the {@link SoundEngine}
 * is constructed or reloaded, from a SoundEngine ctor/reload tail mixin at wiring time (step 3b/7).
 * SoundHandler grabs the engine here. Fresh implementation.
 */
public class SoundEngineLoadEvent extends SoundEvent implements IModBusEvent {

    public SoundEngineLoadEvent(SoundEngine engine) {
        super(engine);
    }
}

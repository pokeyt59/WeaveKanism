package mekanism.fabric_shim.client.event.sound;

import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.client.sounds.SoundEngine;
import org.jetbrains.annotations.Nullable;

/**
 * Stand-in for NeoForge's {@code PlaySoundEvent}: fired on the game bus before the sound engine plays a
 * sound, letting the result sound be swapped or suppressed (null). Posted from a SoundEngine#play mixin
 * at wiring time (step 3b). The result sound starts as the original. Fresh implementation.
 */
public class PlaySoundEvent extends SoundEvent {

    private final String name;
    private final SoundInstance originalSound;
    @Nullable
    private SoundInstance sound;

    public PlaySoundEvent(SoundEngine engine, SoundInstance sound) {
        super(engine);
        this.originalSound = sound;
        this.name = sound.getLocation().getPath();
        this.sound = sound;
    }

    public String getName() {
        return name;
    }

    public SoundInstance getOriginalSound() {
        return originalSound;
    }

    @Nullable
    public SoundInstance getSound() {
        return sound;
    }

    public void setSound(@Nullable SoundInstance sound) {
        this.sound = sound;
    }
}

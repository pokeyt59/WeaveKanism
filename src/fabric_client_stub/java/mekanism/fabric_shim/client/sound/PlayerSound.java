package mekanism.fabric_shim.client.sound;

/**
 * Server-safe stand-in for {@code mekanism.client.sound.PlayerSound}; only its nested {@link SoundType}
 * enum is reached by common code (as an opaque token passed to the sound handler).
 */
public final class PlayerSound {

    private PlayerSound() {
    }

    public enum SoundType {
        JETPACK,
        SCUBA_MASK,
        GRAVITATIONAL_MODULATOR
    }
}

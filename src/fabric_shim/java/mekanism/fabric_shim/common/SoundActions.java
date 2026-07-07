package mekanism.fabric_shim.common;

/**
 * Same constants as net.neoforged.neoforge.common.SoundActions.
 */
public final class SoundActions {

    private SoundActions() {
    }

    public static final SoundAction BUCKET_FILL = SoundAction.get("bucket_fill");
    public static final SoundAction BUCKET_EMPTY = SoundAction.get("bucket_empty");
    public static final SoundAction FLUID_VAPORIZE = SoundAction.get("fluid_vaporize");
    public static final SoundAction CAULDRON_DRIP = SoundAction.get("cauldron_drip");
}

package mekanism.fabric.mixin;

import mekanism.fabric_shim.inject.MekHolderExt;
import net.minecraft.core.Holder;
import org.spongepowered.asm.mixin.Mixin;

/**
 * Interface-target mixin adding {@code getData(DataMapType)} to vanilla {@link Holder} (same pattern
 * as DataComponentHolderMixin).
 */
@Mixin(Holder.class)
public interface HolderMixin extends MekHolderExt {
}

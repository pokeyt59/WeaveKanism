package mekanism.fabric.mixin;

import mekanism.fabric_shim.inject.MekHolderLookupProviderExt;
import net.minecraft.core.HolderLookup;
import org.spongepowered.asm.mixin.Mixin;

/** Interface-target mixin adding holder()/holderOrThrow() to vanilla HolderLookup.Provider. */
@Mixin(HolderLookup.Provider.class)
public interface HolderLookupProviderMixin extends MekHolderLookupProviderExt {
}

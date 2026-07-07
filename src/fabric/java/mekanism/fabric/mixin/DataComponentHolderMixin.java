package mekanism.fabric.mixin;

import mekanism.fabric_shim.inject.MekDataComponentHolderExt;
import net.minecraft.core.component.DataComponentHolder;
import org.spongepowered.asm.mixin.Mixin;

/**
 * Interface-target mixin: adds MekDataComponentHolderExt as a superinterface of the vanilla
 * DataComponentHolder interface so its default methods resolve for receivers typed as the
 * interface. Runtime dispatch through the merged superinterface is VERIFIED by
 * DataComponentBridgeTest#interfaceReceiverDispatch (src/fabric_test) — keep that test green.
 */
@Mixin(DataComponentHolder.class)
public interface DataComponentHolderMixin extends MekDataComponentHolderExt {
}

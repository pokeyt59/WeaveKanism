package mekanism.fabric.mixin;

import mekanism.fabric_shim.inject.MekFluidStateExt;
import net.minecraft.world.level.material.FluidState;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(FluidState.class)
public abstract class FluidStateMixin implements MekFluidStateExt {
}

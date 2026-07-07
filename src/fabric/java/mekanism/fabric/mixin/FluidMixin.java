package mekanism.fabric.mixin;

import mekanism.fabric_shim.inject.MekFluidExt;
import net.minecraft.world.level.material.Fluid;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(Fluid.class)
public abstract class FluidMixin implements MekFluidExt {
}

package mekanism.fabric_shim.inject;

import mekanism.fabric_shim.fluids.FluidType;
import mekanism.fabric_shim.fluids.FluidTypes;
import net.minecraft.world.level.material.Fluid;

/**
 * NeoForge patches vanilla {@link Fluid} with {@code getFluidType()}. Reproduced here via Loom
 * interface injection + {@code FluidMixin}, so upstream {@code fluid.getFluidType()} call sites
 * stay textually intact. Mekanism's own fluids override this (see
 * {@code mekanism.fabric_shim.fluids.BaseFlowingFluid}); everything else resolves through
 * {@link FluidTypes#resolve(Fluid)}.
 */
public interface MekFluidExt {

    default FluidType getFluidType() {
        return FluidTypes.resolve((Fluid) this);
    }
}

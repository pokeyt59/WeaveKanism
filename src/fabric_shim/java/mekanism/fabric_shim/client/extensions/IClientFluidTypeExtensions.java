package mekanism.fabric_shim.client.extensions;

import mekanism.fabric_shim.fluids.FluidStack;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;

/**
 * Compile-surface stand-in for NeoForge's client fluid-type extensions. Ported {@code src/main} only
 * reaches this behind a {@code FMLEnvironment.dist.isClient()} guard (fluid tint lookup); the real
 * per-fluid client extensions (textures, tint, overlays) get registered in Phase 4, at which point
 * {@link #of} will resolve to them instead of the neutral default.
 */
public interface IClientFluidTypeExtensions {

    IClientFluidTypeExtensions DEFAULT = new IClientFluidTypeExtensions() {
    };

    static IClientFluidTypeExtensions of(Fluid fluid) {
        return DEFAULT;
    }

    static IClientFluidTypeExtensions of(FluidState state) {
        return DEFAULT;
    }

    default int getTintColor() {
        return 0xFFFFFFFF;
    }

    default int getTintColor(FluidStack stack) {
        return getTintColor();
    }
}

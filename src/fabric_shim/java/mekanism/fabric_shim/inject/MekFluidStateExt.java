package mekanism.fabric_shim.inject;

import mekanism.fabric_shim.fluids.FluidType;
import mekanism.fabric_shim.fluids.FluidTypes;
import net.minecraft.world.level.material.FluidState;

/**
 * NeoForge patches vanilla {@link FluidState} with {@code getFluidType()}. Reproduced here via Loom
 * interface injection + {@code FluidStateMixin}; delegates to the state's fluid so it agrees with the
 * {@link MekFluidExt} accessor.
 */
public interface MekFluidStateExt {

    default FluidType getFluidType() {
        return FluidTypes.resolve(((FluidState) this).getType());
    }

    default java.util.stream.Stream<net.minecraft.tags.TagKey<net.minecraft.world.level.material.Fluid>> getTags() {
        return ((FluidState) this).getType().builtInRegistryHolder().tags();
    }
}

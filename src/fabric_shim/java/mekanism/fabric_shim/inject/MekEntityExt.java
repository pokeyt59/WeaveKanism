package mekanism.fabric_shim.inject;

import mekanism.fabric_shim.capabilities.EntityCapability;
import mekanism.fabric_shim.fluids.FluidType;
import mekanism.fabric_shim.fluids.FluidTypes;
import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.Nullable;

/**
 * NeoForge patches vanilla {@link Entity} with fluid-type accessors. Only {@code getMaxHeightFluidType()}
 * is touched by ported {@code src/main} (mekasuit hydrostatic repulsor). Reproduced via Loom interface
 * injection + {@code EntityMixin}.
 *
 * <p>Deviation (Phase 3/4): returns the empty type rather than computing the fluid the entity is
 * submerged in — the movement/submersion hooks that would populate it are not wired yet, so the swim
 * boost stays inert. Tracked in PORTING.md's shim-deviations table.
 */
public interface MekEntityExt {

    default FluidType getMaxHeightFluidType() {
        return FluidTypes.EMPTY.value();
    }

    //NeoForge IEntityExtension capability accessors, delegating to the shim EntityCapability token.
    default <T, C> T getCapability(EntityCapability<T, C> capability, C context) {
        return capability.getCapability((Entity) this, context);
    }

    default <T> T getCapability(EntityCapability<T, @Nullable Void> capability) {
        return capability.getCapability((Entity) this, null);
    }
}

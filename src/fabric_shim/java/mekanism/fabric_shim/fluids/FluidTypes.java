package mekanism.fabric_shim.fluids;

import mekanism.fabric_shim.common.SoundActions;
import net.minecraft.core.Holder;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;

/**
 * The built-in {@link FluidType}s the Fabric port needs (vanilla water/lava/empty plus a generic
 * fallback) and the {@link Fluid} &rarr; {@link FluidType} resolver behind the injected
 * {@code getFluidType()} accessors.
 *
 * <p>These stand in for NeoForge's {@code NeoForgeMod.WATER_TYPE / LAVA_TYPE / EMPTY_TYPE}, which
 * {@code NeoForgeMod} re-exports so upstream identity checks (e.g. the electrolytic breathing unit
 * comparing against {@code WATER_TYPE.value()}) keep working. Held directly rather than registered:
 * FluidTypes are consulted by reference, never serialized by registry id.
 */
public final class FluidTypes {

    public static final Holder<FluidType> WATER = Holder.direct(new FluidType(FluidType.Properties.create()
          .descriptionId("fluid_type.minecraft.water")
          .sound(SoundActions.BUCKET_FILL, SoundEvents.BUCKET_FILL)
          .sound(SoundActions.BUCKET_EMPTY, SoundEvents.BUCKET_EMPTY)
          .density(1000).viscosity(1000).temperature(300)));

    public static final Holder<FluidType> LAVA = Holder.direct(new FluidType(FluidType.Properties.create()
          .descriptionId("fluid_type.minecraft.lava")
          .sound(SoundActions.BUCKET_FILL, SoundEvents.BUCKET_FILL_LAVA)
          .sound(SoundActions.BUCKET_EMPTY, SoundEvents.BUCKET_EMPTY_LAVA)
          .density(3000).viscosity(6000).temperature(1300).lightLevel(15)));

    public static final Holder<FluidType> EMPTY = Holder.direct(new FluidType(FluidType.Properties.create()
          .descriptionId("fluid_type.minecraft.empty").density(0).viscosity(0), true));

    /** Generic fallback for fluids the port doesn't otherwise know about (best-effort, water-like). */
    public static final Holder<FluidType> DEFAULT = Holder.direct(new FluidType(FluidType.Properties.create()
          .descriptionId("fluid_type.unknown")));

    private FluidTypes() {
    }

    /**
     * Resolves the {@link FluidType} for any fluid. Mekanism's own fluids answer directly through
     * {@link BaseFlowingFluid#getFluidType()}; vanilla water/lava/empty map to the built-ins above;
     * anything else falls back to {@link #DEFAULT}.
     */
    public static FluidType resolve(Fluid fluid) {
        if (fluid instanceof BaseFlowingFluid mekFluid) {
            return mekFluid.getFluidType();
        }
        if (fluid == Fluids.WATER || fluid == Fluids.FLOWING_WATER) {
            return WATER.value();
        }
        if (fluid == Fluids.LAVA || fluid == Fluids.FLOWING_LAVA) {
            return LAVA.value();
        }
        if (fluid == Fluids.EMPTY) {
            return EMPTY.value();
        }
        return DEFAULT.value();
    }
}

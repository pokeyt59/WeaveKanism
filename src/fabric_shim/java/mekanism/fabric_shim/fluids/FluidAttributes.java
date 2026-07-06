package mekanism.fabric_shim.fluids;

import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariantAttributes;
import net.minecraft.Util;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.material.Fluid;

/**
 * Internal to the Fabric port: replacements for NeoForge's {@code Fluid#getFluidType()} description
 * accessors (FluidType does not exist on Fabric). Hand-edited call sites route here.
 *
 * <p>Names resolve through Fabric's fluid variant attributes, so vanilla and other Fabric mods'
 * fluids display correctly. Mekanism's own fluids get attribute handlers registered when
 * FluidDeferredRegister ports (Phase 2), pointing at the same {@code fluid_type.<ns>.<path>} lang
 * keys upstream's datagen generates.
 */
public final class FluidAttributes {

    private FluidAttributes() {
    }

    public static Component getDescription(Fluid fluid) {
        return FluidVariantAttributes.getName(FluidVariant.of(fluid));
    }

    public static Component getDescription(FluidStack stack) {
        return FluidVariantAttributes.getName(FluidVariant.of(stack.getFluid(), stack.getComponentsPatch()));
    }

    /**
     * Translation key matching NeoForge FluidType's default description id, which is what upstream
     * lang files provide entries for on Mekanism's own fluids.
     */
    public static String getDescriptionId(Fluid fluid) {
        return Util.makeDescriptionId("fluid_type", BuiltInRegistries.FLUID.getKey(fluid));
    }
}

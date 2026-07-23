package mekanism.fabric_shim.client.model;

import mekanism.fabric_shim.client.extensions.IClientFluidTypeExtensions;
import net.minecraft.client.color.item.ItemColor;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.ItemStack;

/**
 * Stand-in for NeoForge's {@code DynamicFluidContainerModel} (the {@code neoforge:fluid_container}
 * bucket-model loader) — only the {@link Colors} slice Mekanism touches: ClientRegistrationUtil
 * registers it as the ItemColor for every Mekanism fluid bucket. Layer 1 is the fluid overlay,
 * tinted with the fluid's registered client-extension tint (per-fluid constant for Mekanism
 * chemicals, so the contained-FluidStack refinement NeoForge does is not needed). The model loader
 * itself is not shimmed — bucket models come from baked JSON until step 4-loader. Fresh
 * implementation.
 */
public class DynamicFluidContainerModel {

    public static class Colors implements ItemColor {

        @Override
        public int getColor(ItemStack stack, int tintIndex) {
            if (tintIndex != 1 || !(stack.getItem() instanceof BucketItem bucket)) {
                return -1;
            }
            return IClientFluidTypeExtensions.of(bucket.content).getTintColor();
        }
    }
}

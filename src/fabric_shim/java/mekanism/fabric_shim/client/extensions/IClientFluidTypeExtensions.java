package mekanism.fabric_shim.client.extensions;

import com.mojang.blaze3d.shaders.FogShape;
import mekanism.fabric_shim.fluids.FluidStack;
import mekanism.fabric_shim.fluids.FluidType;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.FogRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

/**
 * Same surface as NeoForge's client fluid-type extensions (the 1.21.1 slice Mekanism implements:
 * textures, tint, fog). Instances register through {@code RegisterClientExtensionsEvent} into
 * {@link ClientExtensionsHooks}; {@link #of} resolves per fluid type with water-textured neutral
 * defaults for everything unregistered. The Fabric FluidRenderHandler bridge (Phase 4 model step)
 * reads these to drive fluid rendering.
 */
public interface IClientFluidTypeExtensions {

    IClientFluidTypeExtensions DEFAULT = new IClientFluidTypeExtensions() {
    };

    static IClientFluidTypeExtensions of(Fluid fluid) {
        return of(fluid.getFluidType());
    }

    static IClientFluidTypeExtensions of(FluidState state) {
        return of(state.getType());
    }

    static IClientFluidTypeExtensions of(FluidType fluidType) {
        return ClientExtensionsHooks.FLUID_TYPE_EXTENSIONS.getOrDefault(fluidType, DEFAULT);
    }

    default int getTintColor() {
        return 0xFFFFFFFF;
    }

    default int getTintColor(FluidStack stack) {
        return getTintColor();
    }

    default ResourceLocation getStillTexture() {
        return ResourceLocation.withDefaultNamespace("block/water_still");
    }

    default ResourceLocation getFlowingTexture() {
        return ResourceLocation.withDefaultNamespace("block/water_flow");
    }

    default ResourceLocation getStillTexture(FluidStack stack) {
        return getStillTexture();
    }

    default ResourceLocation getFlowingTexture(FluidStack stack) {
        return getFlowingTexture();
    }

    @Nullable
    default ResourceLocation getOverlayTexture() {
        return null;
    }

    @Nullable
    default ResourceLocation getRenderOverlayTexture(Minecraft mc) {
        return null;
    }

    default Vector3f modifyFogColor(Camera camera, float partialTick, ClientLevel level, int renderDistance, float darkenWorldAmount, Vector3f fluidFogColor) {
        return fluidFogColor;
    }

    default void modifyFogRender(Camera camera, FogRenderer.FogMode mode, float renderDistance, float partialTick, float nearDistance, float farDistance,
          FogShape shape) {
    }
}

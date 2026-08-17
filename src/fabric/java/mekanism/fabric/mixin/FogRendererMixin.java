package mekanism.fabric.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import mekanism.fabric_shim.client.event.ViewportEvent;
import mekanism.fabric_shim.common.NeoForge;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.FogRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Fires the two {@code ViewportEvent} fog hooks where NeoForge patches them: ComputeFogColor just
 * before the computed color feeds RenderSystem.clearColor (listeners recolor by writing back), and
 * RenderFog just before the fog planes upload (a canceling listener's near/far replace vanilla's —
 * Mekanism's vision-enhancement fog widening). FogData is AW'd accessible.
 */
@Mixin(FogRenderer.class)
public abstract class FogRendererMixin {

    @Shadow
    private static float fogRed;
    @Shadow
    private static float fogGreen;
    @Shadow
    private static float fogBlue;

    @Inject(method = "setupColor", at = @At(value = "INVOKE",
          target = "Lcom/mojang/blaze3d/systems/RenderSystem;clearColor(FFFF)V"))
    private static void mek$computeFogColor(Camera camera, float partialTick, ClientLevel level, int renderDistanceChunks, float darkenWorldAmount,
          CallbackInfo ci) {
        ViewportEvent.ComputeFogColor event = new ViewportEvent.ComputeFogColor(camera, fogRed, fogGreen, fogBlue);
        NeoForge.EVENT_BUS.post(event);
        fogRed = event.getRed();
        fogGreen = event.getGreen();
        fogBlue = event.getBlue();
    }

    @Inject(method = "setupFog", at = @At(value = "INVOKE",
          target = "Lcom/mojang/blaze3d/systems/RenderSystem;setShaderFogStart(F)V"))
    private static void mek$renderFog(Camera camera, FogRenderer.FogMode fogMode, float renderDistance, boolean isFoggy, float partialTick,
          CallbackInfo ci, @Local FogRenderer.FogData fogData) {
        ViewportEvent.RenderFog event = new ViewportEvent.RenderFog(camera.getFluidInCamera(), camera, fogData.start, fogData.end);
        NeoForge.EVENT_BUS.post(event);
        if (event.isCanceled()) {
            fogData.start = event.getNearPlaneDistance();
            fogData.end = event.getFarPlaneDistance();
        }
    }
}

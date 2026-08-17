package mekanism.fabric.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import mekanism.fabric_shim.client.event.RenderArmEvent;
import mekanism.fabric_shim.common.NeoForge;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.entity.HumanoidArm;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Fires {@code RenderArmEvent} before the vanilla first-person empty arm draws, where NeoForge
 * patches it; a canceling listener (mekasuit arm) replaces the vanilla arm entirely.
 */
@Mixin(ItemInHandRenderer.class)
public abstract class ItemInHandRendererMixin {

    @Shadow
    @Final
    private Minecraft minecraft;

    @Inject(method = "renderPlayerArm", at = @At("HEAD"), cancellable = true)
    private void mek$renderArm(PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, float equippedProgress, float swingProgress,
          HumanoidArm arm, CallbackInfo ci) {
        if (minecraft.player != null) {
            RenderArmEvent event = new RenderArmEvent(minecraft.player, arm, poseStack, bufferSource, packedLight);
            NeoForge.EVENT_BUS.post(event);
            if (event.isCanceled()) {
                ci.cancel();
            }
        }
    }
}

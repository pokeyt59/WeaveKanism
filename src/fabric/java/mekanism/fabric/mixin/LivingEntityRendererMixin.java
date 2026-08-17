package mekanism.fabric.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import mekanism.fabric_shim.client.event.RenderLivingEvent;
import mekanism.fabric_shim.common.NeoForge;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Fires {@code RenderLivingEvent.Pre/Post} around every living-entity render, where NeoForge
 * patches them (Mekanism hides humanoid head/hat parts under mekasuit helmets and restores them
 * after). A canceling Pre listener suppresses the render.
 */
@Mixin(LivingEntityRenderer.class)
public abstract class LivingEntityRendererMixin<T extends LivingEntity, M extends EntityModel<T>> {

    @SuppressWarnings({"unchecked", "rawtypes"})
    @Inject(method = "render(Lnet/minecraft/world/entity/LivingEntity;FFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V",
          at = @At("HEAD"), cancellable = true)
    private void mek$renderLivingPre(T entity, float entityYaw, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight,
          CallbackInfo ci) {
        RenderLivingEvent.Pre<T, M> event = new RenderLivingEvent.Pre<>(entity, (LivingEntityRenderer) (Object) this);
        NeoForge.EVENT_BUS.post(event);
        if (event.isCanceled()) {
            ci.cancel();
        }
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    @Inject(method = "render(Lnet/minecraft/world/entity/LivingEntity;FFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V",
          at = @At("RETURN"))
    private void mek$renderLivingPost(T entity, float entityYaw, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight,
          CallbackInfo ci) {
        NeoForge.EVENT_BUS.post(new RenderLivingEvent.Post<>(entity, (LivingEntityRenderer) (Object) this));
    }
}

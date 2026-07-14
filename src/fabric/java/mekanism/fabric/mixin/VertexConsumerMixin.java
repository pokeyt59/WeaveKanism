package mekanism.fabric.mixin;

import com.mojang.blaze3d.vertex.VertexConsumer;
import mekanism.fabric_shim.inject.MekVertexConsumerExt;
import org.spongepowered.asm.mixin.Mixin;

/**
 * Runtime target for the {@link MekVertexConsumerExt} interface injection onto vanilla VertexConsumer
 * (compile via fabric.mod.json loom:injected_interfaces; runtime needs this interface-target mixin, like
 * KeyMappingMixin). Client-only. Carries the misc + readAlpha-putBulkData defaults.
 */
@Mixin(VertexConsumer.class)
public interface VertexConsumerMixin extends MekVertexConsumerExt {
}

package mekanism.fabric_shim.inject;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormatElement;
import net.minecraft.client.renderer.block.model.BakedQuad;

/**
 * NeoForge's VertexConsumer additions, interface-injected onto {@link VertexConsumer} (class_4588;
 * fabric.mod.json loom:injected_interfaces + VertexConsumerMixin runtime target):
 * <ul>
 *   <li>{@code misc(VertexFormatElement, int...)} — write an arbitrary vertex element; default no-op
 *       (Mekanism's QuadBakingVertexConsumer / Quad.BakedQuadUnpacker override it).</li>
 *   <li>{@code putBulkData(..., readAlpha)} — the readAlpha overload delegates to vanilla's 8-arg
 *       putBulkData (which drives this consumer's per-vertex methods); readAlpha is unused on the port.</li>
 * </ul>
 * Fresh implementation.
 */
public interface MekVertexConsumerExt {

    default VertexConsumer misc(VertexFormatElement element, int... rawData) {
        return (VertexConsumer) this;
    }

    default void putBulkData(PoseStack.Pose pose, BakedQuad quad, float red, float green, float blue, float alpha, int packedLight, int packedOverlay, boolean readAlpha) {
        ((VertexConsumer) this).putBulkData(pose, quad, red, green, blue, alpha, packedLight, packedOverlay);
    }
}

package mekanism.fabric_shim.client.model;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.blaze3d.vertex.VertexFormatElement;

/**
 * The vertex-format offset constants from net.neoforged.neoforge.client.model.IQuadTransformer — the int
 * offsets into a BakedQuad's packed int[] (vanilla BLOCK format) that Outlines reads positions with and
 * QuadBakingVertexConsumer writes every element with. Offsets are the format's byte offsets divided by 4
 * (int units), -1 for elements the BLOCK format lacks (UV1/overlay). Fresh implementation.
 */
public interface IQuadTransformer {

    int STRIDE = DefaultVertexFormat.BLOCK.getVertexSize() / 4;
    int POSITION = findOffset(VertexFormatElement.POSITION);
    int COLOR = findOffset(VertexFormatElement.COLOR);
    int UV0 = findOffset(VertexFormatElement.UV0);
    int UV1 = findOffset(VertexFormatElement.UV1);
    int UV2 = findOffset(VertexFormatElement.UV2);
    int NORMAL = findOffset(VertexFormatElement.NORMAL);

    private static int findOffset(VertexFormatElement element) {
        VertexFormat format = DefaultVertexFormat.BLOCK;
        return format.contains(element) ? format.getOffset(element) / 4 : -1;
    }
}

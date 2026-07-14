package mekanism.fabric_shim.client.model;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;

/**
 * The vertex-format offset constants from net.neoforged.neoforge.client.model.IQuadTransformer that
 * Outlines uses to read positions out of a BakedQuad's packed int[] (vanilla BLOCK format: position is
 * the first element, so POSITION = 0; STRIDE is the format's int size). Only the slice Mekanism
 * references. Fresh implementation.
 */
public interface IQuadTransformer {

    int STRIDE = DefaultVertexFormat.BLOCK.getVertexSize() / 4;
    int POSITION = 0;
}

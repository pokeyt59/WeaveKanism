package mekanism.fabric_shim.inject;

import mekanism.fabric_shim.client.model.MekBakedQuadHooks;
import net.minecraft.client.renderer.block.model.BakedQuad;

/**
 * NeoForge's {@code BakedQuad#hasAmbientOcclusion}, interface-injected onto vanilla BakedQuad (class_777;
 * fabric.mod.json loom:injected_interfaces + BakedQuadMixin runtime target). Reads the flag from the
 * {@link MekBakedQuadHooks} side store (QuadBakingVertexConsumer populates it; default true for quads
 * baked elsewhere). Fresh implementation.
 */
public interface MekBakedQuadExt {

    default boolean hasAmbientOcclusion() {
        return MekBakedQuadHooks.hasAmbientOcclusion((BakedQuad) this);
    }
}

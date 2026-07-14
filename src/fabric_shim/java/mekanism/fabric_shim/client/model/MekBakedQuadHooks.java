package mekanism.fabric_shim.client.model;

import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;
import net.minecraft.client.renderer.block.model.BakedQuad;

/**
 * Side store for the NeoForge {@code BakedQuad#hasAmbientOcclusion} flag, which vanilla BakedQuad does not
 * carry (its ctor takes only shade). QuadBakingVertexConsumer records it here when it bakes a quad; the
 * {@link mekanism.fabric_shim.inject.MekBakedQuadExt} injected accessor reads it back, defaulting to true
 * for quads baked elsewhere (vanilla models use AO). Weak keys so baked quads can be collected.
 */
public final class MekBakedQuadHooks {

    private static final Map<BakedQuad, Boolean> AMBIENT_OCCLUSION = Collections.synchronizedMap(new WeakHashMap<>());

    private MekBakedQuadHooks() {
    }

    public static void setAmbientOcclusion(BakedQuad quad, boolean hasAmbientOcclusion) {
        AMBIENT_OCCLUSION.put(quad, hasAmbientOcclusion);
    }

    public static boolean hasAmbientOcclusion(BakedQuad quad) {
        return AMBIENT_OCCLUSION.getOrDefault(quad, Boolean.TRUE);
    }
}

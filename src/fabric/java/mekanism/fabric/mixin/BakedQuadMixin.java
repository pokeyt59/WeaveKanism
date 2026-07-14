package mekanism.fabric.mixin;

import mekanism.fabric_shim.inject.MekBakedQuadExt;
import net.minecraft.client.renderer.block.model.BakedQuad;
import org.spongepowered.asm.mixin.Mixin;

/**
 * Runtime target for the {@link MekBakedQuadExt} interface injection onto vanilla BakedQuad (compile via
 * fabric.mod.json loom:injected_interfaces; runtime needs this interface-target mixin, like KeyMappingMixin).
 * Client-only. Carries the hasAmbientOcclusion accessor (backed by MekBakedQuadHooks).
 */
@Mixin(BakedQuad.class)
public interface BakedQuadMixin extends MekBakedQuadExt {
}

package mekanism.fabric.mixin;

import mekanism.fabric_shim.inject.MekBakedModelExt;
import net.minecraft.client.resources.model.BakedModel;
import org.spongepowered.asm.mixin.Mixin;

/**
 * Runtime target for the {@link MekBakedModelExt} interface injection onto vanilla BakedModel (compile is
 * covered by fabric.mod.json loom:injected_interfaces; the runtime needs this interface-target mixin, as
 * with KeyMappingMixin). Client-only. The default methods carry NeoForge's data-aware BakedModel surface.
 */
@Mixin(BakedModel.class)
public interface BakedModelMixin extends MekBakedModelExt {
}

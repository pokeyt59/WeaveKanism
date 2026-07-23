package mekanism.fabric.mixin;

import mekanism.fabric_shim.inject.MekModelBakerExt;
import net.minecraft.client.resources.model.ModelBaker;
import org.spongepowered.asm.mixin.Mixin;

/**
 * Interface-target mixin adding NeoForge's bake/getTopLevelModel overloads to vanilla
 * {@link ModelBaker} (same pattern as HolderMixin).
 */
@Mixin(ModelBaker.class)
public interface ModelBakerMixin extends MekModelBakerExt {
}

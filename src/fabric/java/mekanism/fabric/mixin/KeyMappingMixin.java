package mekanism.fabric.mixin;

import mekanism.fabric_shim.inject.MekKeyMappingExt;
import net.minecraft.client.KeyMapping;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(KeyMapping.class)
public interface KeyMappingMixin extends MekKeyMappingExt {
}

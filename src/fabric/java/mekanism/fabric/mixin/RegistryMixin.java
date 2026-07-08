package mekanism.fabric.mixin;

import mekanism.fabric_shim.inject.MekRegistryExt;
import net.minecraft.core.Registry;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(Registry.class)
public interface RegistryMixin extends MekRegistryExt {
}

package mekanism.fabric.mixin;

import mekanism.fabric_shim.inject.MekDataComponentMapBuilderExt;
import net.minecraft.core.component.DataComponentMap;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(DataComponentMap.Builder.class)
public abstract class DataComponentMapBuilderMixin implements MekDataComponentMapBuilderExt {
}

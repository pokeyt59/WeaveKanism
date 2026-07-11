package mekanism.fabric.mixin;

import mekanism.fabric_shim.inject.MekCreativeModeTabBuilderExt;
import net.minecraft.world.item.CreativeModeTab;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(CreativeModeTab.Builder.class)
public abstract class CreativeModeTabBuilderMixin implements MekCreativeModeTabBuilderExt {
}

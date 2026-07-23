package mekanism.fabric.mixin;

import mekanism.fabric_shim.inject.MekItemOverridesExt;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(ItemOverrides.class)
public abstract class ItemOverridesMixin implements MekItemOverridesExt {
}

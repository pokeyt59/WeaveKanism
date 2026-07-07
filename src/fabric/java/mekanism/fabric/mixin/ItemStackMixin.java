package mekanism.fabric.mixin;

import mekanism.fabric_shim.inject.MekItemStackExt;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(ItemStack.class)
public abstract class ItemStackMixin implements MekItemStackExt {
}

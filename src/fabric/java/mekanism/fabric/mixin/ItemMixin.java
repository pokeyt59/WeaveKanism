package mekanism.fabric.mixin;

import mekanism.fabric_shim.inject.MekItemExt;
import net.minecraft.world.item.Item;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(Item.class)
public abstract class ItemMixin implements MekItemExt {
}

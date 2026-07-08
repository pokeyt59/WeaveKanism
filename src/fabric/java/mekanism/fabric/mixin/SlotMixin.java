package mekanism.fabric.mixin;

import mekanism.fabric_shim.inject.MekSlotExt;
import net.minecraft.world.inventory.Slot;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(Slot.class)
public abstract class SlotMixin implements MekSlotExt {
}

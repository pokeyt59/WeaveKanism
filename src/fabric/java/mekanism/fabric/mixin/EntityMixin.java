package mekanism.fabric.mixin;

import mekanism.fabric_shim.inject.MekAttachmentExt;
import mekanism.fabric_shim.inject.MekEntityExt;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(Entity.class)
public abstract class EntityMixin implements MekEntityExt, MekAttachmentExt {
}

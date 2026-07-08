package mekanism.fabric.mixin;

import mekanism.fabric_shim.inject.MekEntityTypeExt;
import net.minecraft.world.entity.EntityType;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(EntityType.class)
public abstract class EntityTypeMixin implements MekEntityTypeExt {
}

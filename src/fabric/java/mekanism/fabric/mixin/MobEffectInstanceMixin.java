package mekanism.fabric.mixin;

import mekanism.fabric_shim.inject.MekMobEffectInstanceExt;
import net.minecraft.world.effect.MobEffectInstance;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(MobEffectInstance.class)
public abstract class MobEffectInstanceMixin implements MekMobEffectInstanceExt {
}

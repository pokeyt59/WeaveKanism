package mekanism.fabric.mixin;

import mekanism.fabric_shim.inject.MekLightningBoltExt;
import net.minecraft.world.entity.LightningBolt;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(LightningBolt.class)
public abstract class LightningBoltMixin implements MekLightningBoltExt {
}

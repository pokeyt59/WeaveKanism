package mekanism.fabric.mixin;

import mekanism.fabric_shim.inject.MekAttachmentExt;
import mekanism.fabric_shim.inject.MekLevelExt;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(Level.class)
public abstract class LevelMixin implements MekLevelExt, MekAttachmentExt {
}

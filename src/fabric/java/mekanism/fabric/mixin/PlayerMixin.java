package mekanism.fabric.mixin;

import mekanism.fabric_shim.inject.MekPlayerExt;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(Player.class)
public abstract class PlayerMixin implements MekPlayerExt {
}

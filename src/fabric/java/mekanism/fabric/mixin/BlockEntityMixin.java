package mekanism.fabric.mixin;

import mekanism.fabric_shim.inject.MekBlockEntityExt;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(BlockEntity.class)
public abstract class BlockEntityMixin implements MekBlockEntityExt {
}

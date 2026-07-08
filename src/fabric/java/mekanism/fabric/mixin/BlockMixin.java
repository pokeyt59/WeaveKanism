package mekanism.fabric.mixin;

import mekanism.fabric_shim.inject.MekBlockExt;
import net.minecraft.world.level.block.Block;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(Block.class)
public abstract class BlockMixin implements MekBlockExt {
}

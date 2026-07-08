package mekanism.fabric.mixin;

import mekanism.fabric_shim.inject.MekBlockStateExt;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(BlockState.class)
public abstract class BlockStateMixin implements MekBlockStateExt {
}

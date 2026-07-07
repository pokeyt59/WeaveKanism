package mekanism.fabric.mixin;

import mekanism.fabric_shim.inject.MekDataComponentInputExt;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.spongepowered.asm.mixin.Mixin;

/**
 * Interface-target mixin (see DataComponentHolderMixin javadoc for the runtime-dispatch caveat).
 */
@Mixin(BlockEntity.DataComponentInput.class)
public interface DataComponentInputMixin extends MekDataComponentInputExt {
}

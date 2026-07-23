package mekanism.fabric.mixin;

import mekanism.fabric_shim.inject.MekLevelRendererExt;
import net.minecraft.client.renderer.LevelRenderer;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(LevelRenderer.class)
public abstract class LevelRendererMixin implements MekLevelRendererExt {
}

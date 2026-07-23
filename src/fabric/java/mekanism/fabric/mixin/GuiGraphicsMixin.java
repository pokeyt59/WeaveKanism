package mekanism.fabric.mixin;

import mekanism.fabric_shim.inject.MekGuiGraphicsExt;
import net.minecraft.client.gui.GuiGraphics;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(GuiGraphics.class)
public abstract class GuiGraphicsMixin implements MekGuiGraphicsExt {
}

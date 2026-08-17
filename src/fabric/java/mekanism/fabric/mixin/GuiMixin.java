package mekanism.fabric.mixin;

import mekanism.fabric_shim.client.event.RenderGuiLayerEvent;
import mekanism.fabric_shim.client.gui.VanillaGuiLayers;
import mekanism.fabric_shim.common.NeoForge;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Fires {@code RenderGuiLayerEvent.Pre} for the crosshair layer — the only vanilla layer Mekanism
 * gates (hidden while the radial selector shows its back button). NeoForge wraps every layer; the
 * port hooks just the consumed one.
 */
@Mixin(Gui.class)
public abstract class GuiMixin {

    @Inject(method = "renderCrosshair", at = @At("HEAD"), cancellable = true)
    private void mek$preCrosshair(GuiGraphics guiGraphics, DeltaTracker deltaTracker, CallbackInfo ci) {
        RenderGuiLayerEvent.Pre event = new RenderGuiLayerEvent.Pre(VanillaGuiLayers.CROSSHAIR);
        NeoForge.EVENT_BUS.post(event);
        if (event.isCanceled()) {
            ci.cancel();
        }
    }
}

package mekanism.fabric.mixin;

import mekanism.fabric_shim.client.event.InputEvent;
import mekanism.fabric_shim.common.NeoForge;
import net.minecraft.client.Minecraft;
import net.minecraft.client.MouseHandler;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Fires {@code InputEvent.MouseScrollingEvent} for in-game scrolling (no screen open — NeoForge's
 * gate) before vanilla processes it; cancellation swallows the scroll (Mekanism's shift-scroll
 * mode switching). The raw y offset is enough for the one consumer, which only reads sign and
 * non-zero-ness.
 */
@Mixin(MouseHandler.class)
public abstract class MouseHandlerMixin {

    @Shadow
    @Final
    private Minecraft minecraft;

    @Inject(method = "onScroll", at = @At("HEAD"), cancellable = true)
    private void mek$onScroll(long window, double xOffset, double yOffset, CallbackInfo ci) {
        if (minecraft.screen == null && minecraft.player != null) {
            InputEvent.MouseScrollingEvent event = new InputEvent.MouseScrollingEvent(yOffset);
            NeoForge.EVENT_BUS.post(event);
            if (event.isCanceled()) {
                ci.cancel();
            }
        }
    }
}

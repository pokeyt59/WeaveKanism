package mekanism.fabric.mixin;

import java.util.Map;
import mekanism.fabric_shim.client.event.RegisterShadersEvent;
import mekanism.fabric_shim.internal.ShimBuses;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.server.packs.resources.ResourceProvider;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Posts {@link RegisterShadersEvent} at the tail of every shader (re)load — where NeoForge fires
 * it — and applies the collected registrations the way NeoForge's patch does: the handler-built
 * instances go into the shader map (closing anything replaced) and each onLoaded callback runs.
 * Vanilla's own reload close-and-clear handles end-of-life on subsequent reloads.
 */
@Mixin(GameRenderer.class)
public abstract class GameRendererMixin {

    @Shadow
    @Final
    private Map<String, ShaderInstance> shaders;

    @Inject(method = "reloadShaders", at = @At("RETURN"))
    private void mek$registerModShaders(ResourceProvider resourceProvider, CallbackInfo ci) {
        RegisterShadersEvent event = new RegisterShadersEvent(resourceProvider);
        ShimBuses.MOD_BUS.post(event);
        for (RegisterShadersEvent.Registration registration : event.getRegistrations()) {
            ShaderInstance previous = this.shaders.put(registration.instance().getName(), registration.instance());
            if (previous != null) {
                previous.close();
            }
            registration.onLoaded().accept(registration.instance());
        }
    }
}

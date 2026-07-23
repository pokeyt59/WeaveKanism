package mekanism.fabric.mixin;

import mekanism.fabric_shim.client.event.RegisterClientReloadListenersEvent;
import mekanism.fabric_shim.internal.ShimBuses;
import net.minecraft.client.Minecraft;
import net.minecraft.client.main.GameConfig;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Posts {@link RegisterClientReloadListenersEvent} from inside the Minecraft constructor — after
 * RenderSystem.initRenderer and the textureManager assignment (Mekanism's handler constructs a
 * TextureAtlas, which queries GL, and reads getTextureManager()), and before the bulk of vanilla's
 * reload listeners register and the first resource reload runs. Fabric client entry points fire
 * too early for this event (pre-GL — verified native crash in glGetInteger); this anchor matches
 * where NeoForge's patched constructor fires it.
 */
@Mixin(Minecraft.class)
public abstract class MinecraftMixin {

    @Inject(method = "<init>", at = @At(value = "INVOKE",
          target = "Lnet/minecraft/server/packs/resources/ReloadableResourceManager;registerReloadListener(Lnet/minecraft/server/packs/resources/PreparableReloadListener;)V",
          ordinal = 1))
    private void mek$postRegisterClientReloadListeners(GameConfig gameConfig, CallbackInfo ci) {
        ShimBuses.MOD_BUS.post(new RegisterClientReloadListenersEvent());
    }
}

package mekanism.fabric.mixin;

import mekanism.fabric_shim.client.event.TextureAtlasStitchedEvent;
import mekanism.fabric_shim.internal.ShimBuses;
import net.minecraft.client.renderer.texture.SpriteLoader;
import net.minecraft.client.renderer.texture.TextureAtlas;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Posts {@code TextureAtlasStitchedEvent} (mod bus) after an atlas uploads, where NeoForge fires
 * it — MekanismRenderer caches its sprite lookups and transmitter models clear per-stitch caches.
 */
@Mixin(TextureAtlas.class)
public abstract class TextureAtlasMixin {

    @Inject(method = "upload", at = @At("RETURN"))
    private void mek$postStitched(SpriteLoader.Preparations preparations, CallbackInfo ci) {
        ShimBuses.MOD_BUS.post(new TextureAtlasStitchedEvent((TextureAtlas) (Object) this));
    }
}
